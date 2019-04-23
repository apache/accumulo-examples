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
    username@instance> createtable input
    username@instance> insert dog cf cq dogvalue
    username@instance> insert cat cf cq catvalue
    username@instance> insert junk family qualifier junkvalue
    username@instance> quit

The TableToFile class configures a map-only job to read the specified columns and
write the key/value pairs to a file in HDFS.

The following will extract the rows containing the column "cf:cq":

    $ ./bin/runmr mapreduce.TableToFile -t input --columns cf:cq --output /tmp/output

    $ hadoop fs -ls /tmp/output
    -rw-r--r--   1 username supergroup          0 2013-01-10 14:44 /tmp/output/_SUCCESS
    drwxr-xr-x   - username supergroup          0 2013-01-10 14:44 /tmp/output/_logs
    drwxr-xr-x   - username supergroup          0 2013-01-10 14:44 /tmp/output/_logs/history
    -rw-r--r--   1 username supergroup       9049 2013-01-10 14:44 /tmp/output/_logs/history/job_201301081658_0011_1357847072863_username_TableToFile%5F1357847071434
    -rw-r--r--   1 username supergroup      26172 2013-01-10 14:44 /tmp/output/_logs/history/job_201301081658_0011_conf.xml
    -rw-r--r--   1 username supergroup         50 2013-01-10 14:44 /tmp/output/part-m-00000

We can see the output of our little map-reduce job:

    $ hadoop fs -text /tmp/output/part-m-00000
    catrow cf:cq []	catvalue
    dogrow cf:cq []	dogvalue
    $

