# 0005 — Shell out to OS tools for drive enumeration

- Status: Accepted (historical, 2017-04-27); partially superseded by [0015](0015-replace-wmic.md)
- Deciders: Pierre Kisters
- Evidence: commits `bc666e2` ("list drives"), `ebd69b4` ("UnixBlockDevice"), `3a84a4c`

## Context

Listing physical drives from the JVM requires either JNI bindings or shelling
out to native tools. The original implementation shelled out:

- Windows: `wmic diskdrive get …` and parse the tabular output.
- Linux: `lsblk -b -io KNAME,TYPE,SIZE,RO`.

## Decision

Use process invocation and parse stdout rather than taking on a native binding.

## Consequences

- Zero native dependencies; portable to any JVM with the OS tools present.
- Brittle to output-format changes.
- Windows 11 24H2+ removed `wmic` by default — see [0015](0015-replace-wmic.md)
  for the v0.1 replacement (PowerShell `Get-CimInstance Win32_DiskDrive`).
- macOS enumeration was never implemented and remains out of scope for v0.1.
