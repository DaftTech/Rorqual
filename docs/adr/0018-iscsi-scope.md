# 0018 — iSCSI target scope: minimal, no auth, no digests

- Status: Accepted (2026-05)

## Context

A full iSCSI target implementation (target + initiator, CHAP, header/data
digests, MC/S, error recovery levels) is a multi-month project. v0.1 needs
a meaningful subset that's actually useful and reachable.

## Decision

v0.1 ships an **iSCSI target only**, with:

- **In scope**: TCP listener (fs2-io), Login phase with security `None`,
  Full Feature Phase dispatch (SCSI Command, Data-Out, R2T, Data-In,
  Response, NopIn/Out, Logout), and the minimal SCSI CDB set
  (`TEST UNIT READY`, `INQUIRY` + supported VPD pages, `READ CAPACITY 10/16`,
  `READ 10/16`, `WRITE 10/16`, `REPORT LUNS`, `MODE SENSE 6/10`), Fixed-format
  sense data.
- **Out of scope** for v0.1: initiator, CHAP, header/data digests, MC/S,
  error recovery level > 0.

## Consequences

- A v0.1 iSCSI target is enough to expose any `BlockDevice` to a Linux
  `open-iscsi` initiator and pass `dd` / `mkfs` smoke tests.
- Production-grade iSCSI (auth, integrity, multi-connection sessions) is
  a v0.2 conversation.
- Interop test in CI confirms basic compatibility with the most-deployed
  initiator (`open-iscsi`).
