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
# Apache Accumulo Regex Example

This example uses mapreduce and accumulo to find items using regular expressions.
This is accomplished using a map-only mapreduce job and a scan-time iterator.

To run this example you will need some data in a table. The following will
put a trivial amount of data into accumulo using the accumulo shell:

    $ accumulo shell
    username@instance> createtable regex
    username@instance> insert dogrow dogcf dogcq dogvalue
    username@instance> insert catrow catcf catcq catvalue
    username@instance> quit

The RegexExample class sets an iterator on the scanner. This does pattern matching
against each key/value in accumulo, and only returns matching items. It will do this
in parallel and will store the results in files in hdfs.

The following will search for any rows in the input table that starts with "dog":

    $ ./bin/runmr mapreduce.RegexExample -t regex --rowRegex 'dog.*' --output /tmp/output

    $ hdfs dfs -ls /tmp/output
    Found 3 items
    -rw-r--r--   1 username supergroup          0 2013-01-10 14:11 /tmp/output/_SUCCESS
    drwxr-xr-x   - username supergroup          0 2013-01-10 14:10 /tmp/output/_logs
    -rw-r--r--   1 username supergroup         51 2013-01-10 14:10 /tmp/output/part-m-00000

We can see the output of our little map-reduce job:

    $ hdfs dfs -cat /tmp/output/part-m-00000
    dogrow dogcf:dogcq [] 1357844987994 false	dogvalue
