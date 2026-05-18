# 0015 — Replace `wmic` with PowerShell on Windows

- Status: Accepted (2026-05)
- Supersedes (partially): [0005](0005-shell-drive-enumeration.md)

## Context

`WindowsBlockDevice` shells out to `wmic diskdrive get …` and parses
tabular output. Microsoft deprecated `wmic` and **removed it from default
Windows installs starting with Windows 11 24H2** (late 2024). On a current
Windows machine, the existing code raises `IOException: Cannot run
program "wmic"`.

## Decision

Use PowerShell `Get-CimInstance Win32_DiskDrive` (or the `Storage`-module
cmdlets) and parse structured output (CSV or `ConvertTo-Json`).

## Consequences

- Works on every supported Windows version (PowerShell 5.1 is in-box; PS 7
  is opt-in but compatible).
- More verbose invocation. Acceptable.
- Linux path (`lsblk`) remains unchanged.
- macOS enumeration is still unimplemented (out of scope for v0.1).
