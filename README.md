# Spark SysLog file reader.

Spark application parses SysLog files and saves messages as json partitioned by host/application/month/day.

Currently supports only log files formated as:

 ```Jan 12 23:23:11 host application: message```

Other formats can be easily supported by adding addtitional regexp matchers.


Build project using:

`gradle build`

Test:

`gradle test`

Run on cluster using:

`spark-submit  build/libs/log-reader-<version>.jar <pathToLogFiles> <ouputPath>`

