# 0011 — Cats Effect 3 + fs2 replace Monix; direct-style models

- Status: Accepted (2026-05)
- Supersedes: [0003](0003-monix-for-async-io.md)

## Context

Monix `3.0.0-RC1` from 2018 has remained Rorqual's effect/streaming runtime
for years. The Typelevel ecosystem (Cats Effect 3, fs2 3.x) is now the
dominant Scala FP stack — better documented, more libraries, more active
maintenance.

Separately, the original code mixed Monix types throughout the model layer
(parsers held `Observable`, etc.), making them hard to test in isolation.

## Decision

- Effect type: **Cats Effect 3 `IO`** (generic `F[_]: Concurrent` where useful).
- Streaming: **fs2** `Stream[F, Byte]`, `Pipe`, `Chunk`.
- **Direct style for models and parsers**: plain `case class`, `enum`,
  `sealed trait`. No effect/streaming types in their signatures. Effects
  live only at I/O boundaries (`BlockDeviceReader/Writer`, backends, frontends).

## Consequences

- Parsers and data types are trivially testable with plain function calls —
  no `Task.runSyncUnsafe`, no `Observable.toListL` ceremony.
- Concurrency primitives (`Mutex`, `Semaphore`, `Ref`) come from CE3 and
  compose with `Resource`.
- TCP/network I/O for iSCSI uses `fs2-io`.
- Migration cost is paid once; ecosystem alignment pays back forever.
