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
# Apache Accumulo Table-to-File Example

This example uses mapreduce to extract specified columns from an existing table.

To run this example you will need some data in a table. The following will
put a trivial amount of data into accumulo using the accumulo shell:

    $ accumulo shell
    root@instance> createnamespace examples
    root@instance> createtable examples.input
    root@instance examples.input> insert dog cf cq dogvalue
    root@instance examples.input> insert cat cf cq catvalue
    root@instance examples.input> insert junk family qualifier junkvalue
    root@instance examples.input> quit

The TableToFile class configures a map-only job to read the specified columns and
writes the key/value pairs to a file in HDFS.

The following will extract the rows containing the column "cf:cq":

    $ ./bin/runmr mapreduce.TableToFile -t examples.input --columns cf:cq --output /tmp/output

    $ hadoop fs -ls /tmp/output
    Found 2 items
    -rw-r--r--   3 root supergroup          0 2021-05-04 10:32 /tmp/output/_SUCCESS
    -rw-r--r--   3 root supergroup         44 2021-05-04 10:32 /tmp/output/part-m-00000

We can see the output of our little map-reduce job:

    $ hadoop fs -text /tmp/output/part-m-00000
    catrow cf:cq []	catvalue
    dogrow cf:cq []	dogvalue