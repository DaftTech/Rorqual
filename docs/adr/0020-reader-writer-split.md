# 0020 — Separate Reader/Writer capability traits

- Status: Accepted (2026-05)

## Context

The original API collapsed read and write into one `BlockStorageHandle` with
a `writable: Boolean` runtime flag. A reader-only caller had no way to express
"I want a handle that *cannot* write" at the type level.

For forensic tooling this is a real safety concern: a buggy or malicious
parser can mutate the evidence it's analysing. A runtime flag check is too
late.

## Decision

Split into capability traits:

```scala
trait BlockDeviceReader[F[_]]:
  def readBlocks(startBlock: Long, blockCount: Int): F[ByteVector]
  def readBytes(offset: Long, length: Long): Stream[F, Byte]

trait BlockDeviceWriter[F[_]] extends BlockDeviceReader[F]:
  def writeBlocks(startBlock: Long, data: ByteVector): F[Unit]
  def writeBytesAt(offset: Long): Pipe[F, Byte, Nothing]
  def flush: F[Unit]
```

`BlockDevice[F]` exposes `openRead: Resource[F, BlockDeviceReader[F]]` and
`openWrite: Resource[F, BlockDeviceWriter[F]]` separately. `openWrite` fails
on a read-only device.

`Writer extends Reader` because partial-block writes require reads (RMW),
and a write-only block device isn't a meaningful abstraction.

## Consequences

- Parsers take `BlockDeviceReader[F]`. The compiler enforces no writes.
- Read-only sources (compressed images, network mounts) can implement
  `Reader` without faking write methods.
- Two trait names instead of one. Trivial cost.
