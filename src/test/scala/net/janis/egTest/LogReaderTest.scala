package net.janis.egTest


import java.io.File

import net.janis.egTest.LogReader.Log
import org.apache.hadoop.fs.{FileSystem}
import org.apache.spark.sql.SparkSession
import org.junit.rules.TemporaryFolder
import org.scalatest.{BeforeAndAfter, FunSuite}

import org.apache.commons.io.FileUtils

import scala.collection.JavaConversions._

class LogReaderTester extends FunSuite with BeforeAndAfter {


  val testDir = "build/resources/test/"
  val tmpFolder = new TemporaryFolder()
  def outputFolder: String = tmpFolder.getRoot.getAbsolutePath + "/out"

  lazy val testSpark = SparkSession
    .builder()
    .config("spark.master", "local")
    .appName("LogReader")
    .enableHiveSupport()
    .getOrCreate()

  lazy val fs = FileSystem.get(testSpark.sparkContext.hadoopConfiguration)

  before {
    tmpFolder.create()
  }
  after {
    tmpFolder.delete()
  }



  test("log extraction") {
    val logs = Seq(
      "Jan 12 23:23:11 host application: good Log",
      "bad Log",
      "Janu 12 23:23:11 host application: bad Log",
      "Jan 02 00:00:00 host application: good Log",
      "Jan 02 23:59:59 host application: good Log"
    )
    val extracted = LogReader.extactLog(logs.iterator).toList
    assert(extracted.size == 5, "size is correct")
    assert(extracted(0).isLeft, "Extraction ok")
    assert(extracted(0).left.get == Log("Jan", 12, "23:23:11", "host", "application", "good Log"))
    assert(extracted(1).isRight, "Extraction failed")
    assert(extracted(1).right.get == "bad Log")
    assert(extracted(2).isRight, "Extraction failed")
    assert(extracted(2).right.get == "Janu 12 23:23:11 host application: bad Log")
    assert(extracted(3).isLeft, "Extraction ok")
    assert(extracted(3).left.get == Log("Jan", 2, "00:00:00", "host", "application", "good Log"))
    assert(extracted(4).isLeft, "Extraction ok")
    assert(extracted(4).left.get == Log("Jan", 2, "23:59:059", "host", "application", "good Log"))
  }

  test("files split into partitions") {
    val output = outputFolder + "/out"
    LogReader.run(testDir + "multiplePartitions", outputFolder, testSpark)

    val files: Array[String] = FileUtils.listFiles(new File(outputFolder), Array("json"), true).map(_.toString).toArray
    assert(files.size == 5)
    assert(files.exists(_.contains("host=host1/application=application1/month=Feb/day=1/part-")))
    assert(files.exists(_.contains("host=host1/application=application1/month=Feb/day=2/part-")))
    assert(files.exists(_.contains("host=host1/application=application1/month=Jan/day=12/part-")))
    assert(files.exists(_.contains("host=host2/application=application1/month=Jan/day=13/part-")))
    assert(files.exists(_.contains("host=host1/application=application2/month=Jan/day=12/part-")))
  }

  test("files combined in partition") {
    val output = outputFolder + "/out"
    LogReader.run(testDir + "onePartition", outputFolder, testSpark)

    val files: Array[String] = FileUtils.listFiles(new File(outputFolder), Array("json"), true).map(_.toString).toArray
    assert(files.size == 1)
    assert(files.exists(_.contains("host=host1/application=application1/month=Jan/day=12/part-")))
  }

}

