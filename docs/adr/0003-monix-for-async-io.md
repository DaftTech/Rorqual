# 0003 — Monix Observable + Task for async I/O

- Status: Superseded by [0011](0011-cats-effect-fs2.md) (2026-05)
- Originally Accepted: 2017-04 (historical)
- Deciders: Pierre Kisters
- Evidence: commits `5699968` ("ParsableObservable"), `58c39dd` ("align byte chunk streams"),
  `58abcb6` ("TransformObservable"), `ba7e71d` ("unblocking io"), `9d7ba05`

## Context

When the codebase started in 2017, Monix was an actively maintained reactive-
streams library for Scala with good `Task` and `Observable` types. The author
built the alignment plumbing (`alignSync`, `alignAsync`, `TransformableObservable`)
on top of `Observable`.

## Decision

Use Monix `Task` (effect type) and `Observable` (streaming) as the async
primitives across `core`, `file`, `device`, and `partition`.

## Consequences

Pinned Rorqual to Monix's release cadence. The codebase ended up on Monix
`3.0.0-RC1` and stayed there for ~4 years (`2018-2022`), missing later 3.x
stabilization. The Cats Effect / fs2 ecosystem grew larger in that window and
absorbed most of Monix's user base.

Superseded by [0011](0011-cats-effect-fs2.md) — Rorqual v0.1 migrates to
Cats Effect 3 + fs2.
