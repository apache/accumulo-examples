<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
# Apache Accumulo Hello World Example

This tutorial uses the following Java classes:

 * [InsertWithBatchWriter.java] - Inserts 10K rows (50K entries) into accumulo with each row having 5 entries
 * [ReadData.java] - Reads data between two rows

Inserts data with a BatchWriter:

    $ ./bin/runex helloworld.InsertWithBatchWriter

On the accumulo status page at the URL below (where 'master' is replaced with the name or IP of your accumulo master), you should see 50K entries

    http://master:9995/

To view the entries, use the shell (run `accumulo shell -u username -p password` to access it) to scan the table:

    username@instance> table hellotable
    username@instance hellotable> scan

You can also use a Java class to scan the table:

    $ ./bin/runex helloworld.ReadData

[InsertWithBatchWriter.java]: ../src/main/java/org/apache/accumulo/examples/helloworld/InsertWithBatchWriter.java
[ReadData.java]: ../src/main/java/org/apache/accumulo/examples/helloworld/ReadData.java
