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
# Apache Accumulo Word Count example

The WordCount example ([WordCount.java]) uses MapReduce and Accumulo to compute
word counts for a set of documents. This is accomplished using a map-only MapReduce
job and a Accumulo table with combiners.

To run this example, create a directory in HDFS containing text files. You can
use the Accumulo README for data:

    $ hdfs dfs -mkdir /wc
    $ hdfs dfs -copyFromLocal /path/to/accumulo/README.md /wc/README.md

Verify that the file was created:

    $ hdfs dfs -ls /wc

After creating the table, run the WordCount MapReduce job with your HDFS input directory:

    $ ./bin/runmr mapreduce.WordCount -i /wc

[WordCount.java] creates an Accumulo table (named with a SummingCombiner iterator
attached to it. It runs a map-only M/R job that reads the specified HDFS directory containing text files and
writes word counts to Accumulo table.

After the MapReduce job completes, query the Accumulo table to see word counts.

    $ accumulo shell
    username@instance> table wordCount
    username@instance wordCount> scan -b the
    the count:20080906 []    75
    their count:20080906 []    2
    them count:20080906 []    1
    then count:20080906 []    1
    ...

When the WordCount MapReduce job was run above, the client properties were serialized
into the MapReduce configuration.  This is insecure if the properties contain sensitive 
information like passwords. A more secure option is store accumulo-client.properties
in HDFS and run th job with the `-D` options.  This will configure the MapReduce job
to obtain the client properties from HDFS:

    $ hdfs dfs -copyFromLocal ./conf/accumulo-client.properties /user/myuser/
    $ ./bin/runmr mapreduce.WordCount -i /wc -t wordCount2 -d /user/myuser/accumulo-client.properties

After the MapReduce job completes, query the `wordCount2` table. The results should
be the same as before:

    $ accumulo shell
    username@instance> table wordCount
    username@instance wordCount> scan -b the
    the count:20080906 []    75
    their count:20080906 []    2
    ...


[WordCount.java]: ../src/main/java/org/apache/accumulo/examples/mapreduce/WordCount.java
