# Architecture Decision Records

This directory captures the architectural decisions that shape Rorqual.
We use [MADR 3.0](https://adr.github.io/madr/) in its short form.

## Index

### Historical (reconstructed from git, 2017-2022)
Each cites the source commits as evidence. These describe what *was* decided
in the original codebase, even where v0.1 supersedes them.

- [0001 — Modular SBT layout](0001-modular-sbt-layout.md)
- [0002 — 512-byte block as canonical I/O unit](0002-canonical-block-size.md)
- [0003 — Monix Observable + Task for async I/O](0003-monix-for-async-io.md) — *superseded by [0011](0011-cats-effect-fs2.md)*
- [0004 — Aligned + WholeBlocks wrappers for unaligned I/O](0004-aligned-wholeblocks-wrappers.md)
- [0005 — Shell out to OS tools for drive enumeration](0005-shell-drive-enumeration.md) — *partially superseded by [0015](0015-replace-wmic.md)*
- [0006 — scodec-bits as binary representation](0006-scodec-bits.md)
- [0007 — MBR parser returns Either](0007-mbr-either-result.md) — *superseded by [0013](0013-soft-fail-parsers.md)*
- [0008 — Scala 2.12 single version, no cross-build](0008-scala-212-only.md) — *superseded by [0010](0010-scala-3-lts.md)*
- [0009 — Scala Steward bot for dependency updates](0009-scala-steward.md)

### v0.1 (new)
- [0010 — Migrate to Scala 3 LTS](0010-scala-3-lts.md)
- [0011 — Cats Effect 3 + fs2 replace Monix; direct-style models](0011-cats-effect-fs2.md)
- [0012 — MUnit as test framework](0012-munit.md)
- [0013 — Soft-failing parsers via `Ior[NEL[ParseError], A]`](0013-soft-fail-parsers.md)
- [0014 — FUSE frontend via raw jnr-fuse (not Cryptomator)](0014-jnr-fuse-not-cryptomator.md)
- [0015 — Replace `wmic` with PowerShell on Windows](0015-replace-wmic.md)
- [0016 — Read-only by default for physical block devices](0016-physical-readonly-default.md)
- [0017 — Apache 2.0 license](0017-apache-2-license.md)
- [0018 — iSCSI target scope: minimal, no auth, no digests](0018-iscsi-scope.md)
- [0019 — Keep `com.dafttech.*` package root](0019-keep-package-root.md)
- [0020 — Separate Reader/Writer capability traits](0020-reader-writer-split.md)
- [0021 — ByteVector as canonical binary type](0021-bytevector-canonical.md)
- [0022 — Distinguish logical from physical block size](0022-logical-vs-physical-blocksize.md)
- [0023 — In-memory streaming writes via materialize-then-RMW for v0.1](0023-inmemory-materialize-rmw.md)

## Writing a new ADR

Copy [`_template.md`](_template.md), give it the next number, and link from
this README. Status starts as `Proposed`; flip to `Accepted` when merged.
Superseding a previous decision? Mark both ADRs and link them.
