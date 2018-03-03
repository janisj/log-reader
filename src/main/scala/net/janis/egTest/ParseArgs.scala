package net.janis.egTest

class ParseArgs(val inputPath :String, val outputPath:String)

object ParseArgs{

  def printHelp(msg:String) =
    throw new IllegalArgumentException(
    s"""$msg:
       |
       | Spark application parses sysLog files with lines as :
       | Jan 12 23:23:11 host application: message
       |
       | Log messages are stored as json files in partitions:  host/application/month/day/part-*.json .
       |      Usage:\n
       |            inputPath -  [Required] path to log file directory
       |            outputPath - [Required] path where partitioned output files are stored
       |            help - [Optional] displays this help message
       |
       | Example:
       |      spark-submit inputDir outputDir
       |
        """.stripMargin)


  def apply(args: Array[String]):ParseArgs ={
    if (args.contains("help"))  printHelp("Help")
    if (args.size <2) printHelp("Not enough parameters")
    if (args.size >2) printHelp("Too many parameters")
    new ParseArgs(args(0), args(1))
  }
}
