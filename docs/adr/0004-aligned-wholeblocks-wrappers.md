# 0004 — Aligned + WholeBlocks wrappers for unaligned I/O

- Status: Accepted (historical, 2017-04-25)
- Deciders: Pierre Kisters
- Evidence: commits `58c39dd` ("align byte chunk streams"), `9faaaaf` ("WholeBlocks trait")

## Context

Block devices only accept reads/writes at sector boundaries, but callers
naturally want byte-addressed I/O. The original code introduced two
self-typed traits:

- `AlignedBlockStorage` — splits requests to land on block boundaries.
- `WholeBlocks` — performs read-modify-write so a sub-sector write actually
  hits the device aligned.

## Decision

Layer alignment and RMW as mixin traits over `BlockStorageHandle`.

## Consequences

- The composition worked but required users to know which mix-ins to apply.
- The traits leaked alignment math (`alignSync`, `blockAddresses`) onto the
  device itself.
- v0.1 keeps the *idea* — block-addressed I/O at the bottom, byte-addressed
  on top with RMW underneath — but exposes it as a single API surface
  (`readBlocks`/`writeBlocks` plus `readBytes`/`writeBytesAt`) rather than
  trait stacking. See [0020](0020-reader-writer-split.md).
