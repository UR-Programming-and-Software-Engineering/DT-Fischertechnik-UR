#!/usr/bin/env python3
"""
Sequential simulation sweep driver (deterministic order).

- Iterates parameters in fixed, incremental order (no randomness):
  packageGenerationInterval in [1,2,3,4,5] (outer loop),
  then all 4^6 machine speed combos for the machines in MACHINES (inner loop).
- Runs one job at a time against /start-with-config.
- Detects completion via /get-running-simulation: waits until the NEW id has timeEnded.
- Logs each run to CSV.

Run:
  python sim_sweep.py --base-url http://localhost:8080/api/simulation --sample-size 10000
"""

import argparse
import itertools
import sys
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable, Tuple, List

import requests
from requests import Session

# ---- Static config ----
MACHINES = [
    "ConveyorBelt01",
    "VacuumGripper02",
    "SortingLine01",
    "VacuumGripper01",
    "MultiProcessing01",
    "HighBay01",
]
PKG_INTERVALS = [1, 2, 3, 4, 5]
SPEED_CHOICES = [0.5, 1.0, 1.5, 2.0]  # per-machine power multipliers
TOTAL_SPACE = len(PKG_INTERVALS) * (len(SPEED_CHOICES) ** len(MACHINES))  # 5 * 4^6 = 20000

# ---------------------------- Helpers ----------------------------

def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()

def get_running_snapshot(sess: Session, base_url: str) -> dict[str, Any] | None:
    """
    Returns the latest entity from /get-running-simulation or None if not available.
    Completion is indicated by non-null 'timeEnded'.
    """
    try:
        r = sess.get(f"{base_url}/get-running-simulation", timeout=10)
        r.raise_for_status()
        return r.json()
    except Exception:
        return None

def is_running(sess: Session, base_url: str) -> bool:
    snap = get_running_snapshot(sess, base_url)
    if not snap:
        return False
    return snap.get("timeEnded") in (None, "null", "")

def wait_until_idle(sess: Session, base_url: str, poll_seconds: float = 2.0, max_wait: float | None = None) -> None:
    start = time.time()
    while True:
        if not is_running(sess, base_url):
            return
        if max_wait and (time.time() - start) > max_wait:
            raise TimeoutError("Timed out waiting for simulator to become idle.")
        time.sleep(poll_seconds)

def submit_run(sess: Session, base_url: str, payload: dict) -> requests.Response:
    return sess.post(f"{base_url}/start-with-config", json=payload, timeout=30)

def wait_for_completion(
    sess: Session,
    base_url: str,
    baseline_last_id: int | None,
    expected_seconds: float,
    max_factor: float = 4.0
) -> tuple[bool, int | None, float]:
    """
    Waits for a new run (id changes from baseline) and for that run to set timeEnded.
    Returns: (completed, new_run_id, elapsed_seconds)
    """
    start = time.time()
    backoff = 1.5
    max_wait = expected_seconds * max_factor
    observed_new_id: int | None = None

    while True:
        snap = get_running_snapshot(sess, base_url)
        now = time.time()

        if snap:
            cur_id = snap.get("id")
            ended = snap.get("timeEnded") is not None

            if baseline_last_id is None:
                if observed_new_id is None:
                    observed_new_id = cur_id
            else:
                if observed_new_id is None and cur_id != baseline_last_id:
                    observed_new_id = cur_id

            if observed_new_id is not None and cur_id == observed_new_id and ended:
                return True, observed_new_id, now - start

            if ended and (baseline_last_id is None or cur_id != baseline_last_id):
                return True, cur_id, now - start

        if (now - start) > max_wait:
            return False, observed_new_id, now - start

        time.sleep(backoff)
        backoff = min(backoff * 1.25, 8.0)

def build_payload(job_idx: int, pkg_interval: int, machine_speeds: List[float],
                  sim_hours: int, speed_multiplier: int) -> dict:
    assert len(machine_speeds) == len(MACHINES)
    power_map = {name: float(val) for name, val in zip(MACHINES, machine_speeds)}
    return {
        "parameters": {
            "id": job_idx,
            "simulationDays": 0,
            "simulationHours": sim_hours,
            "simulationMinutes": 0,
            "speedMultiplier": speed_multiplier,
            "packageGenerationInterval": pkg_interval,
            "machinePowerMultipliers": power_map,
        },
        "timestamp": now_iso(),
    }

def iter_parameter_sets(limit: int) -> Iterable[Tuple[int, List[float]]]:
    """
    Deterministic order:
      for pkg in [1,2,3,4,5]:
        for speeds in product([0.5,1.0,1.5,2.0], repeat=6)  # MACHINES order
    Yields up to 'limit' items.
    """
    count = 0
    for pkg in PKG_INTERVALS:
        for speeds in itertools.product(SPEED_CHOICES, repeat=len(MACHINES)):
            yield pkg, list(speeds)
            count += 1
            if count >= limit:
                return

def write_csv_header(path: Path) -> None:
    if path.exists():
        return
    with path.open("w", encoding="utf-8") as f:
        f.write(",".join([
            "index","submitted_at","completed_at","duration_sec","status",
            "pkgInterval",
            *[f"{m}_speed" for m in MACHINES],
            "id_before","id_after","http_status","http_text"
        ]) + "\n")

def append_csv(path: Path, row: list) -> None:
    def clean(x: object) -> str:
        s = "" if x is None else str(x)
        return s.replace(",", ";").replace("\n", " ")
    with path.open("a", encoding="utf-8") as f:
        f.write(",".join(clean(x) for x in row) + "\n")

# ---------------------------- Main ----------------------------

def main():
    ap = argparse.ArgumentParser(description="Sequential simulation sweep driver (deterministic)")
    ap.add_argument("--base-url", default="http://localhost:8080/api/simulation",
                    help="Base URL for the simulation API (without trailing slash)")
    ap.add_argument("--sample-size", type=int, default=10000,
                    help=f"How many parameter sets to run (1..{TOTAL_SPACE})")
    ap.add_argument("--sim-hours", type=int, default=2, help="Simulation time in hours (logical time)")
    ap.add_argument("--speed-multiplier", type=int, default=250, help="Simulation speedup factor")
    ap.add_argument("--expected-seconds", type=float, default=36.0,
                    help="Expected wall time per run (used for polling timeout)")
    ap.add_argument("--output", default="sweep_log.csv", help="CSV log path")
    args = ap.parse_args()

    sample_size = min(max(args.sample_size, 1), TOTAL_SPACE)
    out_path = Path(args.output)
    write_csv_header(out_path)

    print(f"Total parameter space: {TOTAL_SPACE} combos; running first {sample_size} deterministically.")

    sess = requests.Session()
    sess.headers.update({"Content-Type": "application/json"})

    # Baseline: last seen id from /get-running-simulation
    snap = get_running_snapshot(sess, args.base_url)
    baseline_id = snap.get("id") if snap else None
    print(f"Baseline last id: {baseline_id}")

    try:
        idx = 0
        for pkg_interval, speeds in iter_parameter_sets(sample_size):
            idx += 1

            # Ensure previous run is finished
            print(f"[{idx}/{sample_size}] Waiting for simulator to be idle…")
            wait_until_idle(sess, args.base_url)

            payload = build_payload(
                job_idx=idx,
                pkg_interval=pkg_interval,
                machine_speeds=speeds,
                sim_hours=args.sim_hours,
                speed_multiplier=args.speed_multiplier
            )

            submitted_at = now_iso()
            print(f"[{idx}/{sample_size}] Starting: pkgInterval={pkg_interval}, speeds={speeds}")
            r = submit_run(sess, args.base_url, payload)

            if r.status_code != 200:
                print(f"  -> HTTP {r.status_code}: {r.text[:200]}")
                append_csv(out_path, [
                    idx, submitted_at, "", "", "HTTP_ERROR",
                    pkg_interval, *speeds, baseline_id, "", r.status_code, r.text[:500]
                ])
                time.sleep(5)
                continue

            ok, new_id, elapsed = wait_for_completion(
                sess, args.base_url, baseline_last_id=baseline_id,
                expected_seconds=args.expected_seconds, max_factor=4.0
            )
            completed_at = now_iso() if ok else ""
            status = "OK" if ok else "TIMEOUT"
            print(f"  -> {status} in {elapsed:.1f}s; id {baseline_id} → {new_id}")

            append_csv(out_path, [
                idx, submitted_at, completed_at, f"{elapsed:.3f}", status,
                pkg_interval, *speeds, baseline_id, new_id, r.status_code, r.text[:500]
            ])

            if new_id is not None:
                baseline_id = new_id

            time.sleep(0.5)

        print("Sweep complete.")
        print(f"Log written to: {out_path.resolve()}")

    except KeyboardInterrupt:
        print("\nInterrupted by user. Partial results kept in CSV.")
        sys.exit(130)

if __name__ == "__main__":
    main()
