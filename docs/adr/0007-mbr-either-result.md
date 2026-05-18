# 0007 — MBR parser returns Either[Err, MBR]

- Status: Superseded by [0013](0013-soft-fail-parsers.md) (2026-05)
- Originally Accepted: 2017-05 (historical)
- Deciders: Pierre Kisters
- Evidence: commits `e282a77` ("added MBR Parser"), `cdd303e` ("mbr partition")

## Context

The original MBR parser returned `Either[Err, MBR]` with two error cases:
`NoMBRFound` (missing 0x55AA signature) and `InvalidPartition`. `CHS`
validation used `require(...)`, which **throws** on bad input — a single
out-of-range byte aborts the entire parse.

## Decision

All-or-nothing parsing: either every entry is valid (Right) or one error
collapses the result (Left).

## Consequences

- Simple to call and reason about.
- Useless for forensic recovery: a partially-damaged MBR returns no data at
  all, or worse, throws.
- Superseded by [0013](0013-soft-fail-parsers.md) — Rorqual v0.1 returns
  `Ior[NEL[ParseError], MBR]` and recovers what it can.
