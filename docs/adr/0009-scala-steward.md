# 0009 — Scala Steward bot for dependency updates

- Status: Accepted (historical, 2022-07)
- Evidence: commit burst on 2022-07-12 / 2022-07-13 — twelve `Update X to Y`
  commits authored by `github-actions[bot]` (`80306ef`, `ba23340`, `2332224`,
  `20bf753`, `11467ac`, `d3bc924`, `4bc51f0`, `50ad7d2`, `9b6ab8c`, `cac7c0f`,
  `85d1089`, `b1eea7f`, `4808db7`).

## Context

The original maintainer set up Scala Steward to open PRs for dependency
updates. The 2022 burst represents that single batch; no feature work
accompanied it, and the bot has been inactive since.

## Decision

Use Scala Steward for routine dependency bumps.

## Consequences

- Catches CVE fixes and minor updates without manual effort.
- Doesn't catch ecosystem migrations (Monix → CE3) or major version
  rewrites — those need human decisions, recorded as ADRs.
- v0.1 keeps the practice but pairs it with `sbt-updates` for ad-hoc checks.
