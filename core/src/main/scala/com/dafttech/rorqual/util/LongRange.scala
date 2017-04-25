package com.dafttech.rorqual.util

import com.dafttech.rorqual.util.LongRange._

import scala.annotation.tailrec
import scala.collection.immutable.NumericRange
import scala.language.implicitConversions

/**
  * Created by pierr on 25.04.2017.
  */
class LongRange(val range: NumericRange[Long]) extends AnyVal {
  def locationAfterN(n: Long): Long = range.start + (range.step * n)

  def newEmptyRange(value: Long) = NumericRange(value, value, range.step)

  def take(n: Long): NumericRange[Long] =
    if (n <= 0 || range.isEmpty) newEmptyRange(range.start)
    else if (n >= range.length) range
    else new NumericRange.Inclusive(range.start, locationAfterN(n - 1), range.step)

  def drop(n: Long): NumericRange[Long] =
    if (n <= 0 || range.isEmpty) range
    else if (n >= range.length) newEmptyRange(range.end)
    else range.copy(locationAfterN(n), range.end, range.step)

  def grouped(size: Long): List[NumericRange[Long]] = {
    @tailrec
    def rec(range: NumericRange[Long], ranges: List[NumericRange[Long]]): List[NumericRange[Long]] =
      if (range.isEmpty) ranges
      else rec(range.drop(size), range.take(size) +: ranges)

    rec(range, Nil).reverse
  }
}

object LongRange {
  implicit def range2longRange(range: NumericRange[Long]): LongRange = new LongRange(range)
}
