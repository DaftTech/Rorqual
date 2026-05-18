# 0021 — `ByteVector` as canonical binary type

- Status: Accepted (2026-05)
- Refines: [0006](0006-scodec-bits.md)

## Context

Several candidates for the "bytes" type on the public API:

| Type | Slice | Concat | NIO interop | Parser API |
|---|---|---|---|---|
| `Array[Byte]` | copy | copy | wrap | barren |
| `IArray[Byte]` | copy | copy | cast | barren |
| `ByteBuffer` (heap) | O(1) view | copy | native | mutable, stateful |
| `ByteBuffer` (direct) | O(1) view | copy | **zero-copy FileChannel** | as above |
| `ByteVector` | **O(1) view** | O(log n) | `.view(bb)` is zero-copy | rich (hex/bits/codec) |
| `fs2.Chunk[Byte]` | depends on backing | streaming carrier | depends | thin |

Crucially, `ByteVector.view(bb: ByteBuffer)` wraps without copying (caller
must not mutate the buffer), and `fs2.Chunk.byteVector(bv)` is O(1). So
the end-to-end zero-copy path is:

```
FileChannel.read(directBuffer) → ByteVector.view(bb) → Chunk.byteVector(bv) → Stream[F, Byte]
```

## Decision

- **`ByteVector` is the canonical public binary type** — return type of
  `readBlocks`, input to `writeBlocks`, input/output of parsers, hex
  literals in tests.
- Internally, backends may use direct `ByteBuffer` at the NIO boundary and
  expose via `ByteVector.view`.
- `fs2.Chunk[Byte]` is reserved for streaming carriers, not data storage.

## Consequences

- Parsers get hex literals, drop/take/slice, big/little endian readers
  for free.
- Immutability is a guarantee, not a convention — forensic parsers can be
  trusted not to mutate their input.
- scodec speaks `ByteVector` natively — no boundary conversions in
  partition or iSCSI PDU parsers.
- One small caveat: callers using `ByteVector.view(bb)` must not reuse the
  buffer. v0.1 allocates per read; pooling is a future optimization.
