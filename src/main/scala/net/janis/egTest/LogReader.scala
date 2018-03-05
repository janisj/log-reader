package net.janis.egTest

import org.apache.spark.sql.{Encoders, _}

/**
  * parse log files in format
  * Jan 12 23:23:11 host application: message
  */

object LogReader {

  case class Log(month: String, day: Int, time: String, host: String, application: String, msg: String)

  val extractLog: Iterator[String] => Iterator[Either[Log, String]] = lineIterator => {
    val sysLogRegExp = """([A-Z][a-z]{2}) ([0-3][0-9]) ([0-9:]{8}) (\w+) (\w+): (.*)$""".r
    lineIterator.map(_ match {
      case sysLogRegExp(month, day, time, host, application, msg) => Left(Log(month, day.toInt, time, host, application, msg))
      case line: String => Right(line)
    })
  }

  def run(inputPath: String, outputPath: String, spark: SparkSession) = {
    import spark.implicits._
    implicit val enc: Encoder[Either[Log, String]] = Encoders.kryo[Either[Log, String]]

    var ds: Dataset[Either[Log, String]] = spark.read.textFile(inputPath).mapPartitions(extractLog)
    val partitionColumns: Seq[Column] = Seq($"host", $"application", $"month", $"day")
    ds.flatMap(_.left.toOption).toDF().repartition(partitionColumns: _*)
      .write.partitionBy(partitionColumns.map(_.toString()): _*).json(outputPath)
    // ds(_.right) can be used to save unsuccessfully parsed logs for examination
  }


  def main(args: Array[String]) = {

    val parsedArgs: ParseArgs = ParseArgs(args)
    val spark = SparkSession
      .builder()
      .appName("LogReader")
      .getOrCreate()

    run(parsedArgs.inputPath, parsedArgs.outputPath, spark)
  }

}
