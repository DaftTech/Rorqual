# 0022 — Distinguish logical from physical block size

- Status: Accepted (2026-05)
- Refines: [0002](0002-canonical-block-size.md)

## Context

The original `BlockStorageDevice` had a single `blockSize: Long = 512`.
Modern Advanced Format drives expose two block sizes:

- **Logical** — the size of an addressable block (what `READ (10/16)` sees).
- **Physical** — the underlying sector size on the platter / NAND page.

512n drives: 512/512. 512e drives: 512/4096. 4Kn drives: 4096/4096.
The distinction matters for alignment performance: writes that straddle a
physical boundary trigger an RMW *inside the drive*, even when the host's
write is aligned to the logical block.

## Decision

Replace the scalar `blockSize` with `BlockGeometry(size, logicalBlockSize,
physicalBlockSize)`. Both block sizes are refined at construction:

- Powers of 2.
- `physical >= logical`.
- `size` is a multiple of `logicalBlockSize`.

## Consequences

- Forensic tooling can report and respect the geometry of the source.
- Alignment-aware backends (`FileBlockDevice` writing to a 512e disk) can
  choose to align writes to the physical block size for performance.
- One extra field in `BlockGeometry`. Trivial cost.
