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
# Apache Accumulo Unique Columns example

The UniqueColumns examples ([UniqueColumns.java]) computes the unique set
of column family and column qualifiers in a table. It also demonstrates
how a mapReduce job can directly read a tables files from HDFS.

Create a table and add rows that all have identical column family and column 
qualifiers.

```
$ /path/to/accumulo shell -u username -p secret
username@instance> createnamespace examples
username@instance> createtable examples.unique
username@instance> examples.unique> insert row1 fam1 qual1 v1
username@instance> examples.unique> insert row2 fam1 qual1 v2
username@instance> examples.unique> insert row3 fam1 qual1 v3
```

Exit the Accumulo shell and run the uniqueColumns mapReduce job against 
this table. Note that if the output file already exists in HDFS, it will 
need to be deleted.

```
$ ./bin/runmr mapreduce.UniqueColumns --table examples.unique --reducers 1 --output /tmp/unique
```

When the mapReduce job completes, examine the output.

```
$ hdfs dfs -cat /tmp/unique/part-r-00000
cf:fam1
cq:qual1
```

The output displays the unique column family and column qualifier values. In 
this case since all rows use the same values, there are only two values output.

Note that since the example used only one reducer all output will be contained
within the single `part-r-00000` file. If more than one reducer is used the output
will be spread among various `part-r-xxxxx` files.

Go back to the shell and add some additional entries.

```text
$ /path/to/accumulo shell -u username -p secret
username@instance> table unique
username@instance example.unique> insert row1 fam2 qual2 v2
username@instance example.unique> insert row1 fam3 qual2 v2
username@instance example.unique> insert row1 fam2 qual2 v2
username@instance example.unique> insert row2 fam2 qual2 v2
username@instance example.unique> insert row3 fam2 qual2 v2
username@instance example.unique> insert row3 fam3 qual3 v2
username@instance example.unique> insert row3 fam3 qual4 v2
```

Re-running the command will now find any additional unique column values.

```text
$ hdfs dfs -rm -r -f /tmp/unique
$ ./bin/runmr mapreduce.UniqueColumns --table examples.unique --reducers 1 --output /tmp/unique 
$ hdfs dfs -cat /tmp/unique/part-r-00000
cf:fam1
cf:fam2
cf:fam3
cq:qual1
cq:qual2
cq:qual3
cq:qual4
```

The output now includes the additional column values that were added during the last batch of inserts.


[UniqueColumns.java]: ../src/main/java/org/apache/accumulo/examples/mapreduce/UniqueColumns.java
