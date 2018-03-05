# Spark SysLog file reader.

Spark application parses SysLog files. File lines should be formated as:

 ```Jan 12 23:23:11 host application: message```

Build project using:

`gradle build`

Test:

`gradle test`

Run on cluster using:

`spark-submit  build/libs/log-reader-<version>.jar <pathToLogFiles> <ouputPath>`

