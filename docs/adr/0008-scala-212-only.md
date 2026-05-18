# 0008 — Scala 2.12 single version, no cross-build

- Status: Superseded by [0010](0010-scala-3-lts.md) (2026-05)
- Originally Accepted: 2017 (historical)
- Deciders: Pierre Kisters
- Evidence: `build.sbt` across the entire history pins `scalaVersion := "2.12.x"`.

## Context

The project started on Scala 2.12 and never cross-built. The 2022 Scala
Steward burst bumped the patch version (2.12.16) but never opened a 2.13
or Scala 3 migration.

## Decision

Single Scala 2.12 build.

## Consequences

By 2026, 2.12 is long past its useful life for new development. Most
ecosystem libraries (Cats Effect 3, fs2 3.x) target 2.13 / 3 and offer
limited or no 2.12 support.

Superseded by [0010](0010-scala-3-lts.md).
