# 0017 — Apache 2.0 license

- Status: Accepted (2026-05)

## Context

The original repository shipped without a LICENSE file — making it legally
unusable by anyone outside the contributors. v0.1 needs a clear license
before any artifact can ship to Sonotype Central.

Candidates: MIT, Apache 2.0, BSD-3, LGPLv3, AGPLv3.

## Decision

**Apache 2.0**. Adds explicit patent grant on top of MIT-like permissiveness.
Industry standard for new Scala libraries.

## Consequences

- Rules out AGPL deps — drives [0014](0014-jnr-fuse-not-cryptomator.md)
  (skip Cryptomator `fuse-nio-adapter`).
- Contributors implicitly license their PRs under Apache 2.0 (Section 5).
- A `NOTICE` file accompanies the LICENSE and credits original authors
  per the git history.
