package com.dafttech.rorqual.partition.mbr

import org.lolhens.ifoption.implicits._
import scodec.bits.{ByteVector, _}

/**
  * Created by pierr on 18.05.2017.
  */
case class MBR(bootstrap: ByteVector,
               partitions: List[Partition]) {
  def toBytes: ByteVector =
    bootstrap ++
      ByteVector.concat(partitions.map(_.toBytes)).padRight(64) ++
      MBR.Signature
}

object MBR {
  val Signature = hex"55AA"

  def fromBytes(bytes: ByteVector): Either[Err, MBR] =
    bytes.drop(446 + 64).take(2) match {
      case Signature =>
        val partitionTable = bytes.drop(446).take(64)
        val partitions: List[Partition] = (0 until 4)
          .map(i => partitionTable.drop(i * 16).take(16))
          .flatMap(partitionEntry => Partition.fromBytes(partitionEntry))
          .toList

        partitions.collectFirst {
          case partition if partition.invalid =>
            Left(Err.InvalidPartition)
        } Else Right(
          MBR(bytes.take(446), partitions)
        )

      case _ =>
        Left(Err.NoMBRFound)
    }
}
