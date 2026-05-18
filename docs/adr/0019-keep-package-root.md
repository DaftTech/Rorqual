# 0019 — Keep `com.dafttech.*` package root and group ID

- Status: Accepted (2026-05)

## Context

The existing package root is split awkwardly: `com.dafttech.rorqual.*` for
core/partition, `com.dafttech.device` for backends, `com.dafttech.os` for
OS helpers. A "clean" rewrite invites moving everything under a single
root.

The repository organisation is `DaftTech`. Maven group `com.dafttech` is
already registered/used.

## Decision

Keep `com.dafttech.*` as the package root and Maven group ID. Continue to
allow per-module sub-packages (`com.dafttech.rorqual.core`,
`com.dafttech.rorqual.partition`, `com.dafttech.os`).

## Consequences

- Continuity with existing code and git history.
- No churn from a rename; no broken external links.
- Some inconsistency between `com.dafttech.rorqual.*` and `com.dafttech.os`.
  Cosmetic; acceptable.
