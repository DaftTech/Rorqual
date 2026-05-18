# 0006 — scodec-bits as binary representation

- Status: Accepted (historical, 2017)
- Deciders: Pierre Kisters
- Evidence: present in `build.sbt` and throughout MBR/Partition source from project inception

## Context

Parsing partition tables requires bit-level operations (CHS layout packs a
10-bit cylinder field across two bytes), little-endian integers, hex literals,
and slice/concat without copying. `scodec-bits` ships immutable `ByteVector`
and `BitVector` types with all of these.

## Decision

Use `org.scodec.scodec-bits.ByteVector` as the binary representation in
parsers, on the public API of block-device reads, and in `toBytes` round-trips.

## Consequences

Sets up a natural path to `scodec-core` for declarative parsers in v0.1
(GPT, iSCSI PDUs). Reinforced and broadened by [0021](0021-bytevector-canonical.md).
