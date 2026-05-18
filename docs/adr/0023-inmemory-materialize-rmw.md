# 0023 — In-memory `writeBytesAt` materialises before RMW (v0.1)

- Status: Accepted (2026-05) — provisional, revisit in v0.2

## Context

`BlockDeviceWriter.writeBytesAt(offset: Long): Pipe[F, Byte, Nothing]`
accepts a `Stream[F, Byte]`. The correct streaming implementation reads
the first partial block (RMW), streams the aligned middle, RMWs the tail
block, and lets backpressure through.

A correct *and* fully streaming implementation is non-trivial — it has to
hold one block of state, handle the case where the entire write is inside
a single block, deal with truncation if upstream completes mid-block, and
serialize concurrent writers to overlapping blocks.

## Decision

For v0.1, `InMemoryBlockDevice.writeBytesAt` compiles the entire input
stream into a `ByteVector` and then performs a single RMW. Simple,
correct, but consumes memory proportional to the write size.

## Consequences

- Correct semantics; passes the round-trip and RMW-across-boundary tests.
- Not suitable for multi-GB writes on memory-constrained hosts.
- Acceptable for v0.1: in-memory devices are bounded by `geometry.size`
  anyway. The streaming version belongs with `FileBlockDevice` where it
  actually matters.

To revisit in v0.2 when `FileBlockDevice` lands.
