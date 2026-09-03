#!/usr/bin/env python3
"""JVM-vs-native measurement harness.

Writes raw per-trial rows to CSV so the published tables can be regenerated, and so anyone
can check the spread rather than trusting a median. Standard library only; needs k6 on PATH
for the load phase.

  ./benchmark/run.py all
  ./benchmark/run.py startup --trials 20
  ./benchmark/run.py load --rates 50,200,500

Measures host process RSS. That is not cgroup accounting, so these numbers describe a
process on this machine, not a container under a memory limit.
"""

import argparse
import csv
import hashlib
import http.client
import json
import os
import platform
import shutil
import socket
import statistics
import subprocess
import sys
import threading
import time
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
HEALTH_PATH = "/actuator/health"
API_PATH = "/api/tags"
READY_TIMEOUT = 120.0
POLL_INTERVAL = 0.002
RSS_SAMPLE_INTERVAL = 0.02
SEED_ARTICLES = 20
# Trimmed off each end of the measured load window: VU ramp at the start, gracefulStop at the end.
STEADY_LEAD_IN = 3.0
STEADY_LEAD_OUT = 6.0


class TrialFailed(RuntimeError):
    """A trial that cannot be measured honestly. Never silently degraded into a zero."""


def log(msg):
    print(msg, file=sys.stderr, flush=True)


def run(cmd, **kwargs):
    return subprocess.run(cmd, cwd=ROOT, check=True, text=True, capture_output=True, **kwargs)


def free_port():
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]


def rss_kb(pid):
    """Resident set size in KiB. Returns None when it cannot be read, never a plausible zero."""
    try:
        out = subprocess.run(
            ["ps", "-o", "rss=", "-p", str(pid)], capture_output=True, text=True, timeout=1
        )
        return int(out.stdout.strip())
    except (ValueError, subprocess.SubprocessError):
        return None


def require_rss(pid, what):
    value = rss_kb(pid)
    if value is None:
        raise TrialFailed(f"could not read RSS for {what}")
    return value


class RssSampler(threading.Thread):
    """Samples RSS in the background so peak and plateau are observable, not inferred.

    The stop flag is deliberately not named _stop: Thread uses that name internally on
    Python 3.12 and earlier, and shadowing it breaks join().
    """

    def __init__(self, pid, interval=RSS_SAMPLE_INTERVAL):
        super().__init__(daemon=True)
        self.pid = pid
        self.interval = interval
        self.samples = []
        self._done = threading.Event()

    def run(self):
        while not self._done.is_set():
            value = rss_kb(self.pid)
            if value is None:
                break
            self.samples.append((time.monotonic(), value))
            self._done.wait(self.interval)

    def stop(self):
        self._done.set()
        self.join(timeout=5)
        if self.is_alive():
            raise TrialFailed("RSS sampler did not stop; refusing to reuse the machine state")

    def between(self, start, end):
        return [value for at, value in self.samples if start <= at <= end]

    @property
    def peak(self):
        return max((v for _, v in self.samples), default=None)


def request(port, method, path, payload=None, token=None, timeout=10.0):
    conn = http.client.HTTPConnection("127.0.0.1", port, timeout=timeout)
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    try:
        conn.request(method, path, json.dumps(payload) if payload is not None else None, headers)
        response = conn.getresponse()
        return response.status, response.read()
    finally:
        conn.close()


def probe(port, path):
    """True when the endpoint answers 2xx."""
    try:
        status, _ = request(port, "GET", path, timeout=1.0)
        return 200 <= status < 300
    except (OSError, http.client.HTTPException):
        return False


def seed_dataset(port):
    """Register a user and create articles from the harness.

    Deliberately not done in k6's setup(): k6 records built-in HTTP metrics for setup
    traffic, which would land inside the measured percentiles and request counts.
    """
    suffix = f"{int(time.time())}-{os.getpid()}"
    status, body = request(
        port,
        "POST",
        "/api/users",
        {"user": {"email": f"bench-{suffix}@example.com", "username": f"bench_{suffix}", "password": "Passw0rd!"}},
    )
    if status != 201:
        raise TrialFailed(f"seed user registration returned {status}: {body[:200]}")
    token = json.loads(body)["user"]["token"]
    for index in range(SEED_ARTICLES):
        status, body = request(
            port,
            "POST",
            "/api/articles",
            {
                "article": {
                    "title": f"Seed {suffix} {index}",
                    "description": "Seed article",
                    "body": "Seeded before the measured window.",
                    "tagList": [f"bench-{index % 5}"],
                }
            },
            token=token,
        )
        if status != 201:
            raise TrialFailed(f"seed article {index} returned {status}: {body[:200]}")
    return token


def artifacts():
    jars = [j for j in (ROOT / "service/build/libs").glob("realworld-backend-spring-*.jar") if "plain" not in j.name]
    binary = ROOT / "service/build/native/nativeCompile/realworld-backend-spring"
    if len(jars) != 1:
        raise SystemExit(
            f"expected exactly one boot jar, found {len(jars)}: {[j.name for j in jars]}\n"
            "run ./gradlew clean :service:bootJar :service:nativeCompile first"
        )
    if not binary.exists():
        raise SystemExit(f"native binary missing at {binary}; run ./gradlew :service:nativeCompile")
    return jars[0], binary


def digest(path):
    sha = hashlib.sha256()
    with open(path, "rb") as handle:
        for block in iter(lambda: handle.read(1 << 20), b""):
            sha.update(block)
    return sha.hexdigest()[:16]


def command(variant, port, heap):
    jar, binary = artifacts()
    heap_args = [f"-Xmx{heap}"] if heap else []
    port_arg = f"--server.port={port}"
    if variant == "jvm":
        return ["java", *heap_args, "-jar", str(jar), port_arg]
    return [str(binary), *heap_args, port_arg]


class App:
    """One application run: launch, wait for ready, sample memory, stop."""

    def __init__(self, variant, heap, log_path):
        self.variant = variant
        self.heap = heap
        self.log_path = log_path
        self.port = free_port()
        self.proc = None
        self.sampler = None
        self.launched_at = None

    def __enter__(self):
        self.log_file = open(self.log_path, "w")
        self.launched_at = time.monotonic()
        self.proc = subprocess.Popen(
            command(self.variant, self.port, self.heap),
            cwd=ROOT,
            stdout=self.log_file,
            stderr=subprocess.STDOUT,
        )
        self.sampler = RssSampler(self.proc.pid)
        self.sampler.start()
        return self

    def wait_ready(self, path=HEALTH_PATH):
        """Seconds from launch until the endpoint first answers 2xx headers."""
        deadline = self.launched_at + READY_TIMEOUT
        while time.monotonic() < deadline:
            if self.proc.poll() is not None:
                raise TrialFailed(f"{self.variant} exited with {self.proc.returncode}; see {self.log_path}")
            if probe(self.port, path):
                return time.monotonic() - self.launched_at
            time.sleep(POLL_INTERVAL)
        raise TrialFailed(f"{self.variant} not ready within {READY_TIMEOUT}s; see {self.log_path}")

    def __exit__(self, *exc):
        if self.sampler:
            try:
                self.sampler.stop()
            except TrialFailed as failure:
                log(f"  warning: {failure}")
        if self.proc and self.proc.poll() is None:
            self.proc.terminate()
            try:
                self.proc.wait(timeout=20)
            except subprocess.TimeoutExpired:
                self.proc.kill()
                self.proc.wait(timeout=10)
        self.log_file.close()
        return False


def writer(path, fieldnames):
    handle = open(path, "w", newline="")
    csv_writer = csv.DictWriter(handle, fieldnames=fieldnames)
    csv_writer.writeheader()
    return handle, csv_writer


def summarise(values):
    """Median plus the spread, because the spread is the interesting part."""
    ordered = sorted(values)
    quartiles = statistics.quantiles(ordered, n=4) if len(ordered) >= 4 else None
    return {
        "n": len(ordered),
        "min": round(ordered[0], 3),
        "median": round(statistics.median(ordered), 3),
        "max": round(ordered[-1], 3),
        "iqr": round(quartiles[2] - quartiles[0], 3) if quartiles else None,
    }


def order_for(index):
    """Alternate which runtime goes first so machine drift does not always favour one."""
    return ["jvm", "native"] if index % 2 else ["native", "jvm"]


# --- phases ---------------------------------------------------------------------------------


def phase_startup(out_dir, args):
    handle, rows = writer(
        out_dir / "startup.csv",
        ["variant", "heap", "trial", "order", "ready_s", "first_api_after_health_s", "peak_startup_rss_kb"],
    )
    logs = out_dir / "logs"
    logs.mkdir(exist_ok=True)
    results = {"jvm": [], "native": []}
    order = 0
    for trial in range(1, args.trials + args.warmups + 1):
        for variant in order_for(trial):
            order += 1
            with App(variant, args.heap, logs / f"startup-{variant}-{trial}.log") as app:
                ready = app.wait_ready()
                # Sequential by construction: the API is probed only after health passes, so
                # this is "first API response after the health gate", not independent readiness.
                first_api = app.wait_ready(API_PATH)
                peak = app.sampler.peak
            if peak is None:
                raise TrialFailed(f"no RSS samples for {variant} trial {trial}")
            if trial <= args.warmups:
                log(f"  startup {variant:6s} trial {trial:2d} (warmup, discarded)")
                continue
            log(f"  startup {variant:6s} trial {trial:2d}: ready {ready:.3f}s, api {first_api:.3f}s, peak {peak // 1024} MiB")
            rows.writerow(
                {
                    "variant": variant,
                    "heap": args.heap or "runtime default",
                    "trial": trial - args.warmups,
                    "order": order,
                    "ready_s": round(ready, 4),
                    "first_api_after_health_s": round(first_api, 4),
                    "peak_startup_rss_kb": peak,
                }
            )
            results[variant].append((ready, first_api, peak))
    handle.close()
    return {
        variant: {
            "ready_s": summarise([r for r, _, _ in trials]),
            "first_api_after_health_s": summarise([a for _, a, _ in trials]),
            "peak_startup_rss_mib": summarise([p / 1024 for _, _, p in trials]),
        }
        for variant, trials in results.items()
    }


def phase_idle(out_dir, args):
    """RSS after the process has settled, which is what an idle replica actually holds."""
    handle, rows = writer(out_dir / "idle.csv", ["variant", "heap", "trial", "settle_s", "idle_rss_kb"])
    logs = out_dir / "logs"
    logs.mkdir(exist_ok=True)
    results = {"jvm": [], "native": []}
    for trial in range(1, args.idle_trials + 1):
        for variant in order_for(trial):
            with App(variant, args.heap, logs / f"idle-{variant}-{trial}.log") as app:
                app.wait_ready()
                time.sleep(args.settle)
                idle = require_rss(app.proc.pid, f"{variant} idle trial {trial}")
            log(f"  idle {variant:6s} trial {trial}: {idle // 1024} MiB after {args.settle}s")
            rows.writerow(
                {
                    "variant": variant,
                    "heap": args.heap or "runtime default",
                    "trial": trial,
                    "settle_s": args.settle,
                    "idle_rss_kb": idle,
                }
            )
            results[variant].append(idle / 1024)
    handle.close()
    return {variant: summarise(values) for variant, values in results.items()}


def k6(script, port, token, rate, duration, summary_out):
    env = {
        **os.environ,
        "BASE_URL": f"http://127.0.0.1:{port}/api",
        "TOKEN": token,
        "RATE": str(rate),
        "DURATION": duration,
    }
    if summary_out:
        env["SUMMARY_OUT"] = str(summary_out)
    return subprocess.run(
        ["k6", "run", "--quiet", "--no-usage-report", str(script)],
        cwd=ROOT,
        env=env,
        capture_output=True,
        text=True,
    )


def load_cell(script, variant, rate, repeat, args, logs, summaries):
    """One runtime at one offered rate: seed, warm up, then measure a clean window."""
    with App(variant, args.heap, logs / f"load-{variant}-{rate}-r{repeat}.log") as app:
        app.wait_ready()
        app.wait_ready(API_PATH)
        token = seed_dataset(app.port)
        # Warm up outside the measured window; for the JVM this is where the JIT works.
        warmup = k6(script, app.port, token, rate, args.warmup_duration, None)
        if warmup.returncode != 0:
            raise TrialFailed(f"k6 warmup failed for {variant} at {rate} rps:\n{warmup.stderr[-1500:]}")
        summary_path = summaries / f"{variant}-{rate}-r{repeat}.json"
        started = time.monotonic()
        result = k6(script, app.port, token, rate, args.load_duration, summary_path)
        ended = time.monotonic()
        if result.returncode != 0 or not summary_path.exists():
            raise TrialFailed(f"k6 failed for {variant} at {rate} rps:\n{result.stderr[-1500:]}")
        during = app.sampler.between(started + STEADY_LEAD_IN, ended - STEADY_LEAD_OUT)
        if not during:
            raise TrialFailed(f"no RSS samples inside the measured window for {variant} at {rate} rps")
        time.sleep(args.recovery)
        post = require_rss(app.proc.pid, f"{variant} post-load at {rate} rps")
    metrics = json.loads(summary_path.read_text())["metrics"]
    duration_metric = metrics["http_req_duration"]["values"]
    dropped = int(metrics.get("dropped_iterations", {}).get("values", {}).get("count", 0))
    return {
        "variant": variant,
        "heap": args.heap or "runtime default",
        "rate": rate,
        "repeat": repeat,
        "achieved_rps": round(metrics["http_reqs"]["values"]["rate"], 1),
        "dropped_iterations": dropped,
        "max_vus": int(metrics.get("vus_max", {}).get("values", {}).get("max", 0)),
        "error_rate": round(metrics["http_req_failed"]["values"]["rate"], 5),
        "p50_ms": round(duration_metric["med"], 2),
        "p95_ms": round(duration_metric["p(95)"], 2),
        "p99_ms": round(duration_metric["p(99)"], 2),
        "steady_rss_kb": int(statistics.median(during)),
        "peak_load_rss_kb": max(during),
        "post_load_rss_kb": post,
    }


def phase_load(out_dir, args):
    """Fixed arrival rate so both runtimes see identical offered load."""
    script = ROOT / "benchmark/k6-steady-load.js"
    fields = [
        "variant", "heap", "rate", "repeat", "achieved_rps", "dropped_iterations", "max_vus",
        "error_rate", "p50_ms", "p95_ms", "p99_ms",
        "steady_rss_kb", "peak_load_rss_kb", "post_load_rss_kb",
    ]
    handle, rows = writer(out_dir / "load.csv", fields)
    logs = out_dir / "logs"
    logs.mkdir(exist_ok=True)
    summaries = out_dir / "k6"
    summaries.mkdir(exist_ok=True)
    collected = {}
    for rate in args.rates:
        for repeat in range(1, args.load_repeats + 1):
            for variant in order_for(repeat):
                row = load_cell(script, variant, rate, repeat, args, logs, summaries)
                rows.writerow(row)
                collected.setdefault(f"{variant}@{rate}", []).append(row)
                log(
                    f"  load {variant:6s} @{rate:4d} rps r{repeat}: achieved {row['achieved_rps']}, "
                    f"dropped {row['dropped_iterations']}, p99 {row['p99_ms']}ms, "
                    f"steady {row['steady_rss_kb'] // 1024} MiB, errors {row['error_rate']}"
                )
    handle.close()
    return {
        cell: {
            "repeats": len(cell_rows),
            "achieved_rps": summarise([r["achieved_rps"] for r in cell_rows]),
            "dropped_iterations": sum(r["dropped_iterations"] for r in cell_rows),
            "p95_ms": summarise([r["p95_ms"] for r in cell_rows]),
            "p99_ms": summarise([r["p99_ms"] for r in cell_rows]),
            "steady_rss_mib": summarise([r["steady_rss_kb"] / 1024 for r in cell_rows]),
        }
        for cell, cell_rows in collected.items()
    }


def phase_build(out_dir, args):
    """Wall time for a clean build of each artifact. Gradle caches and daemon stay warm."""
    handle, rows = writer(out_dir / "build.csv", ["target", "wall_s", "gradle_state"])
    results = {}
    for target, task in (("bootJar", ":service:bootJar"), ("nativeCompile", ":service:nativeCompile")):
        run(["./gradlew", "--quiet", "clean"])
        started = time.monotonic()
        proc = subprocess.run(["./gradlew", "--quiet", task], cwd=ROOT, capture_output=True, text=True)
        wall = time.monotonic() - started
        if proc.returncode != 0:
            raise TrialFailed(f"{task} failed:\n{proc.stderr[-1500:]}")
        rows.writerow({"target": target, "wall_s": round(wall, 1), "gradle_state": "clean outputs, warm cache"})
        results[target] = {"wall_s": round(wall, 1), "gradle_state": "clean outputs, warm cache"}
        log(f"  build {target}: {wall:.1f}s")
    handle.close()
    # The second clean removed the jar; restore it so the later phases have both artifacts.
    run(["./gradlew", "--quiet", ":service:bootJar"])
    return results


def phase_sizes(out_dir, _args):
    """On-disk and compressed size. Compressed is what a release download actually costs."""
    jar, binary = artifacts()
    handle, rows = writer(out_dir / "sizes.csv", ["artifact", "bytes", "gzip_bytes", "sha256_prefix"])
    results = {}
    for name, path in (("jar", jar), ("native", binary)):
        raw = path.stat().st_size
        gz = int(
            subprocess.run(
                ["sh", "-c", f"gzip -9 -c '{path}' | wc -c"], capture_output=True, text=True, cwd=ROOT
            ).stdout.strip()
        )
        rows.writerow({"artifact": name, "bytes": raw, "gzip_bytes": gz, "sha256_prefix": digest(path)})
        results[name] = {"mib": round(raw / 1024 / 1024, 1), "gzip_mib": round(gz / 1024 / 1024, 1)}
        log(f"  size {name}: {raw / 1024 / 1024:.1f} MiB, {gz / 1024 / 1024:.1f} MiB gzipped")
    handle.close()
    return results


def environment(args):
    def capture(cmd, lines=1):
        try:
            out = subprocess.run(cmd, capture_output=True, text=True)
            return " / ".join((out.stdout + out.stderr).strip().splitlines()[:lines])
        except (OSError, IndexError):
            return "unknown"

    jar, binary = artifacts()
    return {
        "recorded_at": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "commit": run(["git", "rev-parse", "HEAD"]).stdout.strip(),
        "dirty_working_tree": bool(run(["git", "status", "--porcelain"]).stdout.strip()),
        "machine": capture(["sysctl", "-n", "machdep.cpu.brand_string"]),
        "cpus": os.cpu_count(),
        "memory_gib": round(int(capture(["sysctl", "-n", "hw.memsize"])) / 1024**3),
        "os": f"{platform.system()} {platform.release()} ({platform.machine()})",
        # Three lines, so the GraalVM distribution identity survives rather than just "openjdk".
        "java": capture(["java", "-version"], lines=3),
        "k6": capture(["k6", "version"]),
        "python": platform.python_version(),
        "artifacts": {"jar": f"{jar.name} {digest(jar)}", "native": f"{binary.name} {digest(binary)}"},
        "heap_setting": f"-Xmx{args.heap} on both runtimes" if args.heap else "none; each runtime uses its own ergonomics",
        "load_generator": "k6 on the same host over loopback, so the generator competes with the server",
        "memory_accounting": "host process RSS on macOS; not cgroup accounting, so this is not a container result",
    }


PHASES = {
    "build": phase_build,
    "sizes": phase_sizes,
    "startup": phase_startup,
    "idle": phase_idle,
    "load": phase_load,
}


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("phases", nargs="*", default=["all"], help=f"one or more of: all, {', '.join(PHASES)}")
    parser.add_argument("--trials", type=int, default=20, help="measured startup trials per variant")
    parser.add_argument("--warmups", type=int, default=2, help="discarded startup trials per variant")
    parser.add_argument("--idle-trials", type=int, default=5)
    parser.add_argument("--settle", type=int, default=30, help="seconds to settle before reading idle RSS")
    parser.add_argument("--rates", default="50,200,500", help="offered request rates for the load phase")
    parser.add_argument("--load-repeats", type=int, default=2, help="measured runs per runtime per rate")
    parser.add_argument("--load-duration", default="60s")
    parser.add_argument("--warmup-duration", default="30s")
    parser.add_argument("--recovery", type=int, default=30, help="seconds after load before reading RSS")
    parser.add_argument("--heap", default="", help="value for -Xmx applied to both runtimes, e.g. 512m")
    parser.add_argument("--allow-dirty", action="store_true", help="measure anyway with uncommitted changes")
    parser.add_argument("--out", default="benchmark/results")
    args = parser.parse_args()
    args.rates = [int(r) for r in args.rates.split(",") if r]

    selected = list(PHASES) if "all" in args.phases else args.phases
    unknown = [p for p in selected if p not in PHASES]
    if unknown:
        raise SystemExit(f"unknown phase(s): {', '.join(unknown)}")
    if "load" in selected and not shutil.which("k6"):
        raise SystemExit("k6 is required for the load phase")

    stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    out_dir = ROOT / args.out / stamp
    out_dir.mkdir(parents=True, exist_ok=True)
    env = environment(args)
    if env["dirty_working_tree"] and not args.allow_dirty:
        raise SystemExit(
            "working tree is dirty, so these numbers could not be tied to a commit.\n"
            "commit first, or pass --allow-dirty for a throwaway run."
        )
    (out_dir / "environment.json").write_text(json.dumps(env, indent=2) + "\n")
    log(f"writing to {out_dir.relative_to(ROOT)}")

    summary = {"environment": env, "results": {}}
    for phase in selected:
        log(f"phase: {phase}")
        summary["results"][phase] = PHASES[phase](out_dir, args)
    (out_dir / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    log(f"done: {out_dir.relative_to(ROOT)}/summary.json")


if __name__ == "__main__":
    main()
