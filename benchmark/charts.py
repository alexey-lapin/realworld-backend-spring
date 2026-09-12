#!/usr/bin/env python3
"""Draws the measurement CSVs as SVG.

Emits one light and one dark file per chart, because GitHub strips CSS from SVG and a
README has to work in both themes; the markdown uses <picture> to pick. Standard library
only: the charts are small enough that hand-written SVG beats a plotting dependency.

  ./benchmark/charts.py                 # write benchmark/charts/*.svg
  ./benchmark/charts.py --page out.html # also write a single inspectable page
"""

import argparse
import csv
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RESULTS = ROOT / "benchmark/results"

# Validated with the dataviz palette validator: all six checks pass on both surfaces.
THEMES = {
    "light": {
        "surface": "#fcfcfb", "text": "#0b0b0b", "muted": "#52514e",
        "grid": "#e3e2df", "jvm": "#2a78d6", "native": "#eb6834",
    },
    "dark": {
        "surface": "#1a1a19", "text": "#ffffff", "muted": "#c3c2b7",
        "grid": "#33322f", "jvm": "#3987e5", "native": "#d95926",
    },
}
W, H = 760, 380
PAD = {"l": 78, "r": 168, "t": 66, "b": 56}


def rows(run, name):
    path = RESULTS / run / name
    with open(path, newline="") as handle:
        return list(csv.DictReader(handle))


def latest(pattern):
    return sorted(p.name for p in RESULTS.glob(pattern))


def esc(text):
    return str(text).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


class Svg:
    """Minimal SVG builder. Y grows downward; helpers take chart coordinates."""

    def __init__(self, theme, title, subtitle, y_label, height=H):
        self.h = height
        self.t = THEMES[theme]
        self.parts = []
        self.title = title
        self.subtitle = subtitle
        self.y_label = y_label

    def frame(self):
        t = self.t
        self.parts.append(f'<rect width="{W}" height="{self.h}" fill="{t["surface"]}"/>')
        self.parts.append(
            f'<text x="{PAD["l"]}" y="26" fill="{t["text"]}" font-size="15" font-weight="600">{esc(self.title)}</text>'
        )
        self.parts.append(
            f'<text x="{PAD["l"]}" y="43" fill="{t["muted"]}" font-size="11.5">{esc(self.subtitle)}</text>'
        )
        self.parts.append(
            f'<text transform="translate(18,{(PAD["t"] + self.h - PAD["b"]) / 2}) rotate(-90)" text-anchor="middle" '
            f'fill="{t["muted"]}" font-size="11">{esc(self.y_label)}</text>'
        )

    def grid_line(self, y, label):
        t = self.t
        self.parts.append(
            f'<line x1="{PAD["l"]}" y1="{y:.1f}" x2="{W - PAD["r"]}" y2="{y:.1f}" stroke="{t["grid"]}" stroke-width="1"/>'
        )
        self.parts.append(
            f'<text x="{PAD["l"] - 10}" y="{y + 4:.1f}" text-anchor="end" fill="{t["muted"]}" font-size="11">{esc(label)}</text>'
        )

    def x_tick(self, x, label):
        self.parts.append(
            f'<text x="{x:.1f}" y="{self.h - PAD["b"] + 20}" text-anchor="middle" fill="{self.t["muted"]}" font-size="11">{esc(label)}</text>'
        )

    def path(self, points, colour, dashed=False):
        d = " ".join(("M" if i == 0 else "L") + f"{x:.1f} {y:.1f}" for i, (x, y) in enumerate(points))
        dash = ' stroke-dasharray="5 4"' if dashed else ""
        self.parts.append(
            f'<path d="{d}" fill="none" stroke="{colour}" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"{dash}/>'
        )

    def dot(self, x, y, colour, hollow=False, hint=""):
        ring = f'<circle cx="{x:.1f}" cy="{y:.1f}" r="5.5" fill="{self.t["surface"]}"/>'
        fill = self.t["surface"] if hollow else colour
        body = f'<circle cx="{x:.1f}" cy="{y:.1f}" r="4" fill="{fill}" stroke="{colour}" stroke-width="2"><title>{esc(hint)}</title></circle>'
        self.parts.append(ring + body)

    def bar(self, x, y, w, h, colour, hint=""):
        self.parts.append(
            f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{max(h, 0.5):.1f}" rx="3" fill="{colour}">'
            f'<title>{esc(hint)}</title></rect>'
        )

    def label(self, x, y, text, colour=None, anchor="start", size=11.5, weight="400"):
        self.parts.append(
            f'<text x="{x:.1f}" y="{y:.1f}" text-anchor="{anchor}" fill="{colour or self.t["muted"]}" '
            f'font-size="{size}" font-weight="{weight}">{esc(text)}</text>'
        )

    def render(self):
        body = "".join(self.parts)
        return (
            f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {self.h}" width="{W}" height="{self.h}" '
            f'font-family="ui-sans-serif,-apple-system,Segoe UI,Helvetica,Arial,sans-serif" role="img">{body}</svg>'
        )


def log_scale(value, lo, hi, px_lo, px_hi):
    import math

    span = math.log10(hi) - math.log10(lo)
    return px_lo + (math.log10(max(value, lo)) - math.log10(lo)) / span * (px_hi - px_lo)


def capacity_chart(theme, ramp):
    """Achieved versus offered: the line leaves the diagonal exactly at the knee."""
    s = Svg(
        theme,
        "Where each runtime stops keeping up",
        "2 CPUs, 2 GiB container, read-only mix · hollow marker = requests dropped, rate not sustained",
        "Achieved req/s",
    )
    s.frame()
    x0, x1 = PAD["l"], W - PAD["r"]
    y0, y1 = H - PAD["b"], PAD["t"]
    rates = sorted({int(r["rate"]) for r in ramp})
    top = 9000
    for value in (1000, 2000, 4000, 8000):
        s.grid_line(y0 - value / top * (y0 - y1), f"{value:,}")
    for rate in rates:
        s.x_tick(log_scale(rate, 500, 8000, x0, x1), f"{rate:,}")
    s.label((x0 + x1) / 2, H - 14, "Offered req/s (log scale)", anchor="middle")
    # Reference: what perfect service looks like.
    s.path(
        [(log_scale(r, 500, 8000, x0, x1), y0 - r / top * (y0 - y1)) for r in rates],
        s.t["grid"],
        dashed=True,
    )
    s.label(log_scale(8000, 500, 8000, x0, x1) - 6, y0 - 8000 / top * (y0 - y1) - 10, "offered = achieved", anchor="end")
    for variant, colour_key, name in (("jvm", "jvm", "JVM"), ("native", "native", "Native")):
        series = [r for r in ramp if r["variant"] == variant]
        points, knee = [], None
        for row in series:
            rate, achieved = int(row["rate"]), float(row["achieved_rps"])
            point = (log_scale(rate, 500, 8000, x0, x1), y0 - achieved / top * (y0 - y1))
            points.append(point)
            if row["sustained"] == "True":
                knee = rate
        s.path(points, s.t[colour_key])
        for row, point in zip(series, points):
            sustained = row["sustained"] == "True"
            s.dot(
                *point, s.t[colour_key], hollow=not sustained,
                hint=f'{name} offered {int(row["rate"]):,} → achieved {float(row["achieved_rps"]):,.0f}, '
                     f'{int(row["dropped_iterations"]):,} dropped, p99 {row["p99_ms"]}ms',
            )
        anchor_index = next(
            (i for i, row in enumerate(series) if int(row["rate"]) == knee), len(points) - 1
        )
        anchor = points[anchor_index]
        # JVM sits above native here, so its labels go up and native's go down: neither
        # then crosses the other's line.
        dy_name, dy_note = (-34, -19) if variant == "jvm" else (26, 41)
        s.label(anchor[0] + 14, anchor[1] + dy_name, f"{name}", s.t[colour_key], weight="600", size=12.5)
        s.label(anchor[0] + 14, anchor[1] + dy_note, f"{knee:,} req/s sustained", s.t[colour_key])
    return s.render()


def tail_chart(theme, ramp):
    s = Svg(
        theme,
        "Tail latency as offered load rises",
        "p99 per step, same ramp · sustained steps only",
        "p99 latency (ms, log)",
    )
    s.frame()
    x0, x1 = PAD["l"], W - PAD["r"]
    y0, y1 = H - PAD["b"], PAD["t"]
    for value in (1, 2, 5, 10, 20):
        s.grid_line(y0 - log_scale(value, 1, 20, 0, y0 - y1), f"{value}")
    for rate in (500, 1000, 2000, 4000):
        s.x_tick(log_scale(rate, 500, 4000, x0, x1), f"{rate:,}")
    s.label((x0 + x1) / 2, H - 14, "Offered req/s (log scale)", anchor="middle")
    for variant, name in (("jvm", "JVM"), ("native", "Native")):
        series = [r for r in ramp if r["variant"] == variant and r["sustained"] == "True"]
        points = [
            (
                log_scale(int(r["rate"]), 500, 4000, x0, x1),
                y0 - log_scale(float(r["p99_ms"]), 1, 20, 0, y0 - y1),
            )
            for r in series
        ]
        s.path(points, s.t[variant])
        for row, point in zip(series, points):
            s.dot(*point, s.t[variant], hint=f'{name} at {int(row["rate"]):,} req/s: p99 {row["p99_ms"]}ms, p95 {row["p95_ms"]}ms')
        s.label(points[-1][0] + 12, points[-1][1] + 4, name, s.t[variant], weight="600")
    return s.render()


def memory_chart(theme, series):
    """Idle resident set across profiles: the JVM's number is a policy, not a floor."""
    s = Svg(theme, "Idle memory depends on what you let the JVM have", "Resident set after a 30 s settle, median of 5 starts", "Idle RSS (MiB)")
    s.frame()
    x0, x1 = PAD["l"], W - PAD["r"]
    y0, y1 = H - PAD["b"], PAD["t"]
    top = 560
    for value in (0, 100, 200, 300, 400, 500):
        s.grid_line(y0 - value / top * (y0 - y1), str(value))
    group_w = (x1 - x0) / len(series)
    for index, (label, values) in enumerate(series):
        centre = x0 + group_w * (index + 0.5)
        for offset, variant, name in ((-1, "jvm", "JVM"), (1, "native", "Native")):
            value = values[variant]
            bar_w = 34
            bx = centre + offset * (bar_w / 2 + 1) - (bar_w if offset < 0 else 0)
            by = y0 - value / top * (y0 - y1)
            s.bar(bx, by, bar_w, y0 - by, s.t[variant], hint=f"{name}, {label}: {value:.0f} MiB idle")
            s.label(bx + bar_w / 2, by - 7, f"{value:.0f}", s.t["text"], anchor="middle", size=11)
        for line, part in enumerate(label.split("\n")):
            s.x_tick(centre, part) if line == 0 else s.label(centre, H - PAD["b"] + 34, part, anchor="middle")
    for index, (variant, name) in enumerate((("jvm", "JVM"), ("native", "Native"))):
        s.label(x1 + 16, PAD["t"] + 4 + index * 18, name, s.t[variant], weight="600")
    return s.render()


def startup_chart(theme, steps):
    """Stacked self time: where the wait actually goes, per runtime."""
    height = 300
    s = Svg(
        theme,
        "Where startup time goes",
        "Self time per step from actuator/startup, median across 20 starts · Linux, host process",
        "",
        height=height,
    )
    s.frame()
    x0, x1 = PAD["l"], W - PAD["r"] + 60
    top = max(sum(v for _, v in parts) for parts in steps.values()) * 1.08
    row_h, gap = 46, 62
    tones = ["", "cc", "99", "66", "40"]
    for index, (variant, parts) in enumerate(steps.items()):
        y = PAD["t"] + 8 + index * (row_h + gap)
        s.label(x0, y - 10, "JVM" if variant == "jvm" else "Native image", s.t[variant], weight="600", size=12.5)
        cursor, unlabelled = x0, []
        for slot, (name, value) in enumerate(parts):
            width = value / top * (x1 - x0)
            if "[" in name:
                short = name.split("[")[-1].rstrip("]") or name.split(".")[-1]
            else:
                short = ".".join(name.split(".")[-2:])
            s.bar(cursor, y, max(width - 2, 1), row_h, s.t[variant] + tones[min(slot, 4)], hint=f"{name}: {value:.3f}s")
            # Roughly 6.2px per character at 11px; anything narrower gets named underneath.
            if width > len(short) * 6.2 + 18:
                ink = s.t["surface"] if slot < 2 else s.t["text"]
                s.label(cursor + 8, y + 19, short, ink, size=11, weight="600")
                s.label(cursor + 8, y + 34, f"{value:.2f}s", ink, size=11)
            else:
                unlabelled.append(f"{short} {value:.2f}s")
            cursor += width
        s.label(cursor + 10, y + row_h / 2 + 4, f"total {sum(v for _, v in parts):.2f}s", s.t["text"], weight="600")
        if unlabelled:
            s.label(x0, y + row_h + 18, "then " + " · ".join(unlabelled), size=11)
    s.label(
        x0,
        height - 18,
        "Liquibase rebuilds the schema in H2 every boot; config-classes.parse is absent on native because AOT did it at build time",
    )
    return s.render()


def collect():
    """Pull the series the charts need out of the raw CSVs."""
    ramp_run = latest("*")[-1]
    ramp = None
    for run in reversed(latest("*")):
        if (RESULTS / run / "ramp.csv").exists():
            ramp, ramp_run = rows(run, "ramp.csv"), run
            break
    memory, steps = [], {}
    profiles = [
        ("macOS\nhost process", "20260903T031623Z"),
        ("Linux\nhost process", "20260904T030258Z"),
        ("Linux\n512 MiB container", "20260905T210927Z"),
        ("Linux\n1 GiB container", "20260905T214236Z"),
    ]
    for label, run in profiles:
        if not (RESULTS / run / "summary.json").exists():
            continue
        idle = json.loads((RESULTS / run / "summary.json").read_text())["results"]["idle"]
        memory.append((label, {"jvm": idle["jvm"]["median"], "native": idle["native"]["median"]}))
    breakdown_run = "20260905T221618Z"
    if (RESULTS / breakdown_run / "startup_steps.csv").exists():
        raw = rows(breakdown_run, "startup_steps.csv")
        for variant in ("jvm", "native"):
            totals = {}
            for row in raw:
                if row["variant"] != variant:
                    continue
                totals.setdefault(row["step"], []).append(float(row["self_s"]))
            ranked = sorted(((sum(v) / len(v), k) for k, v in totals.items()), reverse=True)
            top = [(name, value) for value, name in ranked[:4]]
            rest = sum(value for value, _ in ranked[4:])
            steps[variant] = top + [("everything else", rest)]
    return ramp, ramp_run, memory, steps


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--out", default="benchmark/charts")
    parser.add_argument("--page", default="", help="also write a single HTML page with all charts")
    args = parser.parse_args()

    ramp, ramp_run, memory, steps = collect()
    out = ROOT / args.out
    out.mkdir(parents=True, exist_ok=True)
    charts = {
        "capacity": lambda theme: capacity_chart(theme, ramp),
        "tail-latency": lambda theme: tail_chart(theme, ramp),
        "idle-memory": lambda theme: memory_chart(theme, memory),
        "startup-composition": lambda theme: startup_chart(theme, steps),
    }
    written = {}
    for name, draw in charts.items():
        for theme in THEMES:
            svg = draw(theme)
            (out / f"{name}-{theme}.svg").write_text(svg + "\n")
            written[(name, theme)] = svg
        print(f"wrote {name}-light.svg, {name}-dark.svg")

    if args.page:
        page = Path(args.page) if Path(args.page).is_absolute() else ROOT / args.page
        page.write_text(build_page(written, list(charts), ramp, ramp_run))
        print(f"wrote {page}")


def table(headers, body_rows, caption):
    head = "".join(f"<th>{esc(h)}</th>" for h in headers)
    body = "".join("<tr>" + "".join(f"<td>{esc(c)}</td>" for c in row) + "</tr>" for row in body_rows)
    return (
        f'<details class="data"><summary>{esc(caption)}</summary><div class="scroll">'
        f"<table><thead><tr>{head}</tr></thead><tbody>{body}</tbody></table></div></details>"
    )


def build_page(written, order, ramp, ramp_run):
    """One page per chart: the figure, then the rows it was drawn from."""
    environment = json.loads((RESULTS / ramp_run / "environment.json").read_text())
    captions = {
        "capacity": "Offered rate stepped until requests are dropped. The JVM holds twice the rate native does.",
        "tail-latency": "p99 at each sustained step. Native's tail is worse at every rate on this workload.",
        "idle-memory": "The JVM's idle footprint is a policy, not a floor: give it a container limit and it halves.",
        "startup-composition": "Native removes config-class parsing entirely; what remains is the app's own boot work.",
    }
    tables = {
        "capacity": table(
            ["Runtime", "Offered", "Achieved", "Dropped", "p99 ms", "Sustained"],
            [
                [r["variant"], f'{int(r["rate"]):,}', f'{float(r["achieved_rps"]):,.0f}',
                 f'{int(r["dropped_iterations"]):,}', r["p99_ms"], "yes" if r["sustained"] == "True" else "no"]
                for r in ramp
            ],
            "Ramp data",
        ),
        "tail-latency": table(
            ["Runtime", "Offered", "p50 ms", "p95 ms", "p99 ms"],
            [[r["variant"], f'{int(r["rate"]):,}', r["p50_ms"], r["p95_ms"], r["p99_ms"]]
             for r in ramp if r["sustained"] == "True"],
            "Latency per step",
        ),
    }
    figures = []
    for name in order:
        figures.append(
            f'<section><figure class="chart">{written[(name, "light")]}{written[(name, "dark")]}'
            f'<figcaption>{esc(captions[name])}</figcaption></figure>{tables.get(name, "")}</section>'
        )
    facts = [
        ("Machine", f'{environment["machine"]}, {environment["cpus"]} cores, {environment["memory_gib"]} GiB'),
        ("OS", environment["os"]),
        ("Runtime", environment["java"].split(" / ")[1] if " / " in environment["java"] else environment["java"]),
        ("Commit", environment["commit"][:12]),
        ("Load generator", environment["load_generator"]),
        ("Memory accounting", environment["memory_accounting"]),
    ]
    facts_html = "".join(f"<dt>{esc(k)}</dt><dd>{esc(v)}</dd>" for k, v in facts)
    return PAGE.replace("{{figures}}", "".join(figures)).replace("{{facts}}", facts_html)


PAGE = """<title>RealWorld Runtime Benchmarks</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;500;600&family=IBM+Plex+Mono:wght@400;500&display=swap">
<style>
  :root {
    color-scheme: light;
    --ground: #fbfbf9;
    --raised: #f3f2ee;
    --ink: #141413;
    --muted: #5c5b56;
    --hairline: #e4e3de;
    --jvm: #2a78d6;
    --native: #eb6834;
    --sans: "IBM Plex Sans", system-ui, -apple-system, sans-serif;
    --mono: "IBM Plex Mono", ui-monospace, SFMono-Regular, Menlo, monospace;
  }
  @media (prefers-color-scheme: dark) {
    :root:not([data-theme="light"]) {
      color-scheme: dark;
      --ground: #1a1a19;
      --raised: #232320;
      --ink: #f7f7f4;
      --muted: #b6b5ac;
      --hairline: #33322f;
      --jvm: #3987e5;
      --native: #d95926;
    }
  }
  :root[data-theme="dark"] {
    color-scheme: dark;
    --ground: #1a1a19;
    --raised: #232320;
    --ink: #f7f7f4;
    --muted: #b6b5ac;
    --hairline: #33322f;
    --jvm: #3987e5;
    --native: #d95926;
  }
  body { background: var(--ground); color: var(--ink); font-family: var(--sans); line-height: 1.55; }
  main { max-width: 880px; margin: 0 auto; padding: 56px 24px 96px; display: flex; flex-direction: column; gap: 40px; }
  h1 { font-size: 30px; font-weight: 600; letter-spacing: -0.02em; margin: 0 0 10px; text-wrap: balance; }
  .lede { margin: 0; max-width: 62ch; color: var(--muted); font-size: 15.5px; }
  .verdict { display: flex; flex-wrap: wrap; gap: 10px 28px; margin-top: 20px; font-family: var(--mono);
             font-size: 13px; font-variant-numeric: tabular-nums; }
  .verdict b { font-weight: 500; }
  .verdict .jvm { color: var(--jvm); }
  .verdict .native { color: var(--native); }
  section { display: flex; flex-direction: column; gap: 12px; border-top: 1px solid var(--hairline); padding-top: 28px; }
  .chart { margin: 0; display: flex; flex-direction: column; gap: 10px; }
  .chart svg { max-width: 100%; height: auto; }
  .chart svg:last-of-type { display: none; }
  @media (prefers-color-scheme: dark) {
    :root:not([data-theme="light"]) .chart svg:first-of-type { display: none; }
    :root:not([data-theme="light"]) .chart svg:last-of-type { display: block; }
  }
  :root[data-theme="dark"] .chart svg:first-of-type { display: none; }
  :root[data-theme="dark"] .chart svg:last-of-type { display: block; }
  figcaption { color: var(--muted); font-size: 14px; max-width: 62ch; }
  .data summary { cursor: pointer; font-family: var(--mono); font-size: 12.5px; color: var(--muted);
                  padding: 6px 0; width: fit-content; }
  .data summary:focus-visible { outline: 2px solid var(--jvm); outline-offset: 3px; }
  .scroll { overflow-x: auto; }
  table { border-collapse: collapse; font-family: var(--mono); font-size: 12.5px;
          font-variant-numeric: tabular-nums; margin-top: 6px; }
  th, td { text-align: right; padding: 5px 14px 5px 0; border-bottom: 1px solid var(--hairline); white-space: nowrap; }
  th:first-child, td:first-child { text-align: left; }
  th { font-weight: 500; color: var(--muted); }
  dl { display: grid; grid-template-columns: max-content 1fr; gap: 6px 20px; margin: 0;
       font-size: 13px; }
  dt { font-family: var(--mono); color: var(--muted); }
  dd { margin: 0; }
  footer { border-top: 1px solid var(--hairline); padding-top: 24px; color: var(--muted); font-size: 13.5px; }
</style>
<main>
  <header>
    <h1>JVM against native image, measured</h1>
    <p class="lede">Every figure below comes from the CSVs committed alongside this page, drawn by
      <span style="font-family:var(--mono)">benchmark/charts.py</span>. Hover any mark for its exact values;
      the rows behind each chart are one click away.</p>
    <p class="verdict">
      <span><b class="native">Native</b> 0.88 s to healthy · 218 MiB idle</span>
      <span><b class="jvm">JVM</b> 4.19 s · 495 MiB idle</span>
      <span><b class="jvm">JVM</b> 4,000 req/s sustained vs <b class="native">2,000</b></span>
    </p>
  </header>
  {{figures}}
  <footer>
    <dl>{{facts}}</dl>
  </footer>
</main>
"""


if __name__ == "__main__":
    main()
