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
# Apache Accumulo MapReduce Example

## WordCount Example

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

    $ ./bin/run-mapred mapreduce.WordCount -i /wc

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

Another example to look at is
org.apache.accumulo.examples.mapreduce.UniqueColumns. This example
computes the unique set of columns in a table and shows how a map reduce job
can directly read a tables files from HDFS.

One more example available is
org.apache.accumulo.examples.mapreduce.TokenFileWordCount.
The TokenFileWordCount example works exactly the same as the WordCount example
explained above except that it uses a token file rather than giving the
password directly to the map-reduce job (this avoids having the password
displayed in the job's configuration which is world-readable).

To create a token file, use the create-token utility

  $ accumulo create-token

It defaults to creating a PasswordToken, but you can specify the token class
with -tc (requires the fully qualified class name). Based on the token class,
it will prompt you for each property required to create the token.

The last value it prompts for is a local filename to save to. If this file
exists, it will append the new token to the end. Multiple tokens can exist in
a file, but only the first one for each user will be recognized.

Rather than waiting for the prompts, you can specify some options when calling
create-token, for example

  $ accumulo create-token -u root -p secret -f root.pw

would create a token file containing a PasswordToken for
user 'root' with password 'secret' and saved to 'root.pw'

This local file needs to be uploaded to hdfs to be used with the
map-reduce job. For example, if the file were 'root.pw' in the local directory:

  $ hadoop fs -put root.pw root.pw

This would put 'root.pw' in the user's home directory in hdfs.

Because the basic WordCount example uses Opts to parse its arguments
(which extends ClientOnRequiredTable), you can use a token file with
the basic WordCount example by calling the same command as explained above
except replacing the password with the token file (rather than -p, use -tf).

  $ ./bin/run-mapred mapreduce.WordCount --input /user/username/wc -t wordCount -u username -tf tokenfile

In the above examples, username was 'root' and tokenfile was 'root.pw'

However, if you don't want to use the Opts class to parse arguments,
the TokenFileWordCount is an example of using the token file manually.

  $ ./bin/run-mapred mapreduce.TokenFileWordCount instance zookeepers username tokenfile /user/username/wc wordCount

The results should be the same as the WordCount example except that the
authentication token was not stored in the configuration. It was instead
stored in a file that the map-reduce job pulled into the distributed cache.
(If you ran either of these on the same table right after the
WordCount example, then the resulting counts should just double.)

[WordCount.java]: ../src/main/java/org/apache/accumulo/examples/mapreduce/WordCount.java
