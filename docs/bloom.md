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
# Apache Accumulo Bloom Filter Example

This example shows how to create a table with bloom filters enabled.  The second part
shows how bloom filters increase query performance when looking for values that
do not exist in a table.

## Bloom Filters Enabled

Accumulo data is divided into tablets and each tablet has multiple r-files.
Lookup performance of a tablet with 3 r-files can be 3 times slower than
a tablet with one r-file. However if the files contain unique sets of data,
then bloom filters can help with performance.

Run the example below to create two identical tables. One table has bloom
filters enabled, the other does not. The major compaction ratio was increased to
prevent the files from being compacted into one file. If Accumulo is not configured
with enough memory to hold 1 million rows then more r-files will be created.

    $ ./bin/runex bloom.BloomFilters

Run the example below to perform 500 lookups against each table. Even though only one r-file will 
likely contain entries for the query, all files will be interrogated.
    
    $ ./bin/runex bloom.BloomBatchScanner

    Scanning bloom_test1 with seed 7
    Scan finished! 282.49 lookups/sec, 1.77 secs, 500 results
    All expected rows were scanned
    Scanning bloom_test2 with seed 7
    Scan finished! 704.23 lookups/sec, 0.71 secs, 500 results
    All expected rows were scanned

You can verify the table has three or more r-files by looking in HDFS. To look in HDFS
you will need the table ID, which can be found with the following shell command.

    $ accumulo shell -u username -p password -e 'tables -l'
    accumulo.metadata    =>        !0
    accumulo.root        =>        +r
    bloom_test1          =>         2
    bloom_test2          =>         3
    trace                =>         1

So the table id for bloom_test2 is 3. The command below shows what files this
table has in HDFS. This assumes Accumulo is at the default location in HDFS.

    $ hdfs dfs -ls -R /accumulo/tables/3
    drwxr-xr-x   - username supergroup          0 2012-01-10 14:02 /accumulo/tables/3/default_tablet
    -rw-r--r--   3 username supergroup   52672650 2012-01-10 14:01 /accumulo/tables/3/default_tablet/F00000dj.rf
    -rw-r--r--   3 username supergroup   52436176 2012-01-10 14:01 /accumulo/tables/3/default_tablet/F00000dk.rf
    -rw-r--r--   3 username supergroup   52850173 2012-01-10 14:02 /accumulo/tables/3/default_tablet/F00000dl.rf

Running the rfile-info command shows that one of the files has a bloom filter
and its 1.5MB.

    $ accumulo rfile-info /accumulo/tables/3/default_tablet/F00000dj.rf
    Locality group         : <DEFAULT>
	Start block          : 0
	Num   blocks         : 752
	Index level 0        : 43,598 bytes  1 blocks
	First key            : row_0000001169 foo:1 [exampleVis] 1326222052539 false
	Last key             : row_0999999421 foo:1 [exampleVis] 1326222052058 false
	Num entries          : 999,536
	Column families      : [foo]

    Meta block     : BCFile.index
      Raw size             : 4 bytes
      Compressed size      : 12 bytes
      Compression type     : gz

    Meta block     : RFile.index
      Raw size             : 43,696 bytes
      Compressed size      : 15,592 bytes
      Compression type     : gz

    Meta block     : acu_bloom
      Raw size             : 1,540,292 bytes
      Compressed size      : 1,433,115 bytes
      Compression type     : gz

## Bloom Filters when data is not found

Run the example below to create 2 tables, one with bloom filters enabled.

    $ ./bin/runex bloom.BloomFiltersNotFound

One million random values initialized with seed 7 are inserted into each table.  
Once the flush completes, 500 random queries are done against each table but with a different seed.
Even when nothing is found the lookups are faster against the table with the bloom filters.

    Writing data to bloom_test3 and bloom_test4 (bloom filters enabled)
    Scanning bloom_test3 with seed 8
    Scan finished! 780.03 lookups/sec, 0.64 secs, 0 results
    Did not find 500
    Scanning bloom_test4 with seed 8
    Scan finished! 1736.11 lookups/sec, 0.29 secs, 0 results
    Did not find 500
