# 0016 — Read-only by default for physical block devices

- Status: Accepted (2026-05)

## Context

The original `BlockStorageDevice` defaulted `writable = true`. `DriveStorageDevice`
inherited this. A user who fat-fingers a `dd`-equivalent loop against
`/dev/sda` or `\\.\PhysicalDrive0` corrupts their host OS.

Forensic users *especially* must not mutate evidence: a tool that defaults
writable invites tampering.

## Decision

`PhysicalBlockDevice` opens read-only by default. Write access requires
explicit `OpenMode.ReadWrite` plus an elevated-privilege check.

(In-memory and file-backed devices remain writable by default — they have
no evidence-mutation concern.)

## Consequences

- One safe default; the dangerous path is opt-in.
- Aligns with the [Reader/Writer split](0020-reader-writer-split.md):
  callers that only need reads request `openRead` and physically cannot
  write.
- Forensic tooling can wrap any device with `BlockDevice.readOnly` for
  defense in depth.
