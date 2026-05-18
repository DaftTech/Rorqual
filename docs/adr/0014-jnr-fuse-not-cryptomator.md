# 0014 — FUSE frontend via raw jnr-fuse (not Cryptomator)

- Status: Accepted (2026-05)

## Context

JVM FUSE bindings on the menu in 2026:

- `jnr-fuse` (com.github.serceman) — Apache 2.0, thin JNR binding over
  libfuse (Linux/macOS) and WinFsp (Windows).
- Cryptomator `fuse-nio-adapter` — higher-level, exposes a
  `java.nio.file.Path` as a FUSE mount. **AGPLv3** with a paid commercial
  exception.

Rorqual's FUSE frontend exposes a **block device** (a raw image file), not
a filesystem. We don't need a NIO bridge — we need to implement a handful
of FUSE ops (`open`, `read`, `write`, `getattr`, `truncate`).

## Decision

Use `jnr-fuse` directly. Skip `fuse-nio-adapter`.

## Consequences

- Apache 2.0 only. No AGPL contamination — see [0017](0017-apache-2-license.md).
- ~300 LOC of FUSE op implementations, vs ~50 LOC + an adapter dep. Worth
  it for the license + the closer fit to "expose a block device."
- The mount lifecycle (mountpoint setup, unmount-on-shutdown) is roughly
  the pattern p2pfs's `MounterUtils` demonstrates, adapted to CE3
  `Resource`.
