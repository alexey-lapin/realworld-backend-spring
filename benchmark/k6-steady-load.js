// Open-model load for the JVM-vs-native comparison. Fixed arrival rate, so both runtimes see
// identical offered load no matter how fast they answer; a closed model would quietly taper
// the rate when the server slows and hide the tail.
//
// The workload is synthetic and stated as such: 60% article list, 20% tag list, 20% article
// create. It is not a recording of real frontend traffic, and 20% writes is heavier than
// ordinary browsing would be.
//
// The harness registers the user and seeds articles over plain HTTP before this script runs,
// because k6 records built-in metrics for setup() traffic and that would land inside the
// measured percentiles and request counts.
import http from "k6/http";
import exec from "k6/execution";
import { check } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080/api";
const TOKEN = __ENV.TOKEN;

export const options = {
  discardResponseBodies: false,
  summaryTrendStats: ["min", "med", "p(90)", "p(95)", "p(99)", "max"],
  scenarios: {
    steady: {
      executor: "constant-arrival-rate",
      rate: Number(__ENV.RATE || 100),
      timeUnit: "1s",
      duration: __ENV.DURATION || "60s",
      preAllocatedVUs: Number(__ENV.PREALLOC || 128),
      maxVUs: Number(__ENV.MAX_VUS || 1024),
      gracefulStop: "5s",
    },
  },
};

export function setup() {
  if (!TOKEN) {
    throw new Error("TOKEN is required; the harness seeds the dataset and passes it in");
  }
  return { token: TOKEN };
}

// iterationInTest is a scenario-wide counter, so the mix holds across the run. __ITER would
// be per-VU, which makes every newly allocated VU start the cycle again and skews it to reads.
export default function (data) {
  const slot = exec.scenario.iterationInTest % 5;
  if (slot === 4) {
    const res = http.post(
      `${BASE_URL}/articles`,
      JSON.stringify({
        article: {
          title: `Load ${exec.scenario.iterationInTest}-${Math.random().toString(16).slice(2)}`,
          description: "Load test article",
          body: "Generated inside the measured window.",
          tagList: ["bench-write"],
        },
      }),
      {
        headers: { "Content-Type": "application/json", Authorization: `Token ${data.token}` },
        tags: { op: "create_article" },
      },
    );
    check(res, { "create article 201": (r) => r.status === 201 });
  } else if (slot === 3) {
    const res = http.get(`${BASE_URL}/tags`, { tags: { op: "list_tags" } });
    check(res, { "list tags 200": (r) => r.status === 200 });
  } else {
    const res = http.get(`${BASE_URL}/articles?limit=20&offset=0`, { tags: { op: "list_articles" } });
    check(res, { "list articles 200": (r) => r.status === 200 });
  }
}

// k6 dropped --summary-export, so the harness passes SUMMARY_OUT and reads the JSON from here.
export function handleSummary(data) {
  const out = __ENV.SUMMARY_OUT;
  return out ? { [out]: JSON.stringify(data, null, 2) } : {};
}
