package com.dafttech.rorqual.partition

/** A single problem encountered while parsing partition metadata.
  *
  * Carries the byte offset within the source so forensic tooling can highlight
  * the exact location of damage.
  *
  * @param offset  byte offset within the buffer that was parsed
  * @param field   structured path to the field, e.g. `"mbr.partitions[2].chs.start"`
  * @param message human-readable description
  * @param severity see [[Severity]]
  */
final case class ParseError(
    offset: Long,
    field: String,
    message: String,
    severity: Severity
)
