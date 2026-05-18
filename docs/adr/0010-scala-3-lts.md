# 0010 — Migrate to Scala 3 LTS

- Status: Accepted (2026-05)
- Supersedes: [0008](0008-scala-212-only.md)

## Context

Scala 2.12 has been EOL-trending for new development since 2.13 released
and effectively dead since Scala 3.3 LTS in 2023. By 2026, Cats Effect 3
and fs2 3.x target 2.13 and 3 first-class; 2.12 is rare and increasingly
unsupported. Rorqual was on 2.12.16 with no cross-build.

## Decision

Migrate to Scala 3.3.7 LTS. No cross-build to 2.13 or 2.12.

## Consequences

- Access to enums, opaque types, given/using, extension methods,
  `derives` — all of which clean up the new core API meaningfully.
- One source set, simpler build.
- Closes the door on consumers stuck on 2.12. Acceptable: those users
  have a JVM-compatible artifact in older Rorqual snapshots.
- Pairs with [0011](0011-cats-effect-fs2.md) (CE3/fs2 ecosystem).
