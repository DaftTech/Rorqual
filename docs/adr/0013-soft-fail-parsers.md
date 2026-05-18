# 0013 — Soft-failing parsers via `Ior[NEL[ParseError], A]`

- Status: Accepted (2026-05)
- Supersedes: [0007](0007-mbr-either-result.md)

## Context

The old MBR parser returned `Either[Err, MBR]` and threw on invalid CHS
values. For a binary parser this is the wrong shape: a single damaged byte
collapses the entire result.

Rorqual targets forensic users — people inspecting damaged disks, partial
images, or wiped media. The whole point of running a parser against a
damaged source is to learn "what could you recover, and what's broken?"

Available type-shapes:
- `Either[E, A]` — binary success/failure. Wrong.
- `Validated[NEL[E], A]` — accumulates errors, but value is mutually exclusive
  with errors. Wrong.
- scodec `Attempt[A]` — also binary. Wrong.
- **`Ior[NEL[E], A]`** — `Right(a)`, `Left(errs)`, or `Both(errs, a)`. Right.

## Decision

Define `ParseResult[A] = Ior[NonEmptyList[ParseError], A]` and use it for
all partition parsing (MBR today, GPT next). `ParseError` carries:

- `offset: Long` — byte offset within the source
- `field: String` — structured path (`"mbr.partitions[2].chs.start.cylinder"`)
- `message: String`
- `severity: Severity` (`Warning | Error | Fatal`)

Parsers never throw. Each partition entry, each CHS field, parses
independently and aggregates its diagnostics.

## Consequences

- Forensic recovery becomes a first-class use case, not an accident.
- Missing 0x55AA signature is a warning, not a refusal.
- One damaged entry doesn't blow up the other three.
- Tests can assert *both* "what was recovered" and "what errors were reported."
- Slight call-site overhead: callers must decide what to do with `Ior.Both`.
  Acceptable cost.
