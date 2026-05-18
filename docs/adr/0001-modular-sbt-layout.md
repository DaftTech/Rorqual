# 0001 — Modular SBT layout

- Status: Accepted (historical, 2017-04-07)
- Deciders: Pierre Kisters
- Evidence: commits `c8d6ec3` ("added submodules"), `43468ff` ("module layout changes")

## Context

The original author split Rorqual into separate SBT subprojects from the start
rather than building it as a single artifact. The split mirrored the eventual
feature surface: a `core` abstraction, file/disk backends, OS-detection helpers,
partition parsing, and per-frontend modules (FUSE, iSCSI).

## Decision

Use a multi-module SBT build with `core`, `os`, `file`, `device`, `partition`,
`fuse`, and `iscsi` as separate projects. `core` has no backend-specific deps;
backends depend on `core`; frontends and parsers depend on `core` and (where
needed) backends.

## Consequences

- Frontends and backends can be pulled in independently — a user who only wants
  the in-memory device + MBR parser doesn't pay for jnr-fuse.
- Forces clean dependency direction: backends/frontends → core, never the
  reverse.
- More SBT ceremony than a single artifact.

Still in force as of v0.1.
