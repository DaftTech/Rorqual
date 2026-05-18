package com.dafttech.rorqual.partition

import cats.data.{Ior, NonEmptyList}

/** The result of a soft-failing parse.
  *
  *   - `Ior.Right(a)`        — clean parse, no errors.
  *   - `Ior.Both(errors, a)` — partial parse: some structure was recovered alongside errors.
  *   - `Ior.Left(errors)`    — nothing recoverable.
  *
  * Designed for forensic use: a single bad field never blocks recovery of the
  * surrounding structure.
  */
type ParseResult[+A] = Ior[NonEmptyList[ParseError], A]

object ParseResult:

  def success[A](a: A): ParseResult[A] = Ior.Right(a)

  def failure[A](err: ParseError): ParseResult[A] =
    Ior.Left(NonEmptyList.one(err))

  def warn[A](a: A, err: ParseError): ParseResult[A] =
    Ior.Both(NonEmptyList.one(err), a)

  def fromEither[A](e: Either[ParseError, A]): ParseResult[A] = e match
    case Right(a)  => success(a)
    case Left(err) => failure(err)

  extension [A](pr: ParseResult[A])
    /** Drop the value side; keep only the diagnostics. */
    def errorList: List[ParseError] = pr.left.fold(List.empty[ParseError])(_.toList)

    /** Whether at least one diagnostic was recorded (regardless of severity). */
    def hasErrors: Boolean = pr.left.isDefined

    /** The value, if any was recovered. */
    def recovered: Option[A] = pr.right
