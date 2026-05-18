# 0002 — 512-byte block as canonical I/O unit

- Status: Accepted (historical, 2017-04-08)
- Deciders: Fabian Sachara, Pierre Kisters
- Evidence: commits `0b9dd83`, `abc35f0` ("BlockStorage"), `c3e0487` ("added blockFile")

## Context

Disks and disk images have been built around 512-byte sectors for decades. The
original `BlockStorageDevice` trait defaulted `blockSize = 512` to match this
ubiquitous default. Modern Advanced Format drives expose either 512e (512
logical / 4096 physical) or 4Kn (4096/4096) geometry, but 512 logical is still
the safe default.

## Decision

Treat block size as a per-device property with a default of 512 bytes. All
block-addressed I/O is measured in multiples of the device's logical block size.

## Consequences

- Matches every disk image and historical filesystem.
- Allows 4Kn drives by overriding the default — see [0022](0022-logical-vs-physical-blocksize.md)
  for the v0.1 refinement (explicit logical/physical distinction).

Still in force, refined by [0022](0022-logical-vs-physical-blocksize.md).
