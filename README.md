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
# Apache Accumulo Examples

[![Build Status][ti]][tl]

## Setup instructions

Follow the steps below to run the Accumulo examples:

1. Clone this repository

        git clone https://github.com/apache/accumulo-examples.git

2. Follow [Accumulo's quickstart][quickstart] to install and run an Accumulo instance.
   Accumulo has an [accumulo-client.properties] in `conf/` that must be configured as
   the examples will use this file to connect to your instance.

3. Review [env.sh.example] in to see if you need to customize it. If `ACCUMULO_HOME` & `HADOOP_HOME`
   are set in your shell, you may be able skip this step. Make sure `ACCUMULO_CLIENT_PROPS` is
   set to the location of your [accumulo-client.properties].

        cp conf/env.sh.example conf/env.sh
        vim conf/env.sh

3. Build the examples repo and copy the examples jar to Accumulo's `lib/ext` directory:

        ./bin/build
        cp target/accumulo-examples.jar /path/to/accumulo/lib/ext/

4. Each Accumulo example has its own documentation and instructions for running the example which
   are linked to below.

When running the examples, remember the tips below:

* Examples are run using the `runex` or `runmr` commands which are located in the `bin/` directory
  of this repo. The `runex` command is a simple script that use the examples shaded jar to run a
  a class. The `runmr` starts a MapReduce job in YARN.
* Commands intended to be run in bash are prefixed by '$' and should be run from the root of this
  repository.
* Several examples use the `accumulo` and `accumulo-util` commands which are expected to be on your 
  `PATH`. These commands are found in the `bin/` directory of your Accumulo installation.
* Commands intended to be run in the Accumulo shell are prefixed by '>'.

## Available Examples

Each example below highlights a feature of Apache Accumulo.

| Example | Description |
|---------|-------------|
| [batch] | Using the batch writer and batch scanner |
| [bloom] | Creating a bloom filter enabled table to increase query performance |
| [bulkIngest] | Ingesting bulk data using map/reduce jobs on Hadoop |
| [classpath] | Using per-table classpaths |
| [client] | Using table operations, reading and writing data in Java. |
| [combiner] | Using example StatsCombiner to find min, max, sum, and count. |
| [compactionStrategy] | Configuring a compaction strategy |
| [constraints] | Using constraints with tables. Limit the mutation size to avoid running out of memory |
| [deleteKeyValuePair] | Deleting a key/value pair and verifying the deletion in RFile. |
| [dirlist] | Storing filesystem information. |
| [export] | Exporting and importing tables. |
| [filedata] | Storing file data. |
| [filter] | Using the AgeOffFilter to remove records more than 30 seconds old. |
| [helloworld] | Inserting records both inside map/reduce jobs and outside. And reading records between two rows. |
| [isolation] | Using the isolated scanner to ensure partial changes are not seen. |
| [regex] | Using MapReduce and Accumulo to find data using regular expressions. |
| [reservations] | Using conditional mutations to implement simple reservation system. |
| [rgbalancer] | Using a balancer to spread groups of tablets within a table evenly |
| [rowhash] | Using MapReduce to read a table and write to a new column in the same table. |
| [sample] | Building and using sample data in Accumulo. |
| [shard] | Using the intersecting iterator with a term index partitioned by document. |
| [spark] | Using Accumulo as input and output for Apache Spark jobs |
| [tabletofile] | Using MapReduce to read a table and write one of its columns to a file in HDFS. |
| [terasort] | Generating random data and sorting it using Accumulo. |
| [uniquecols] | Use MapReduce to count unique columns in Accumulo |
| [visibility] | Using visibilities (or combinations of authorizations). Also shows user permissions. |
| [wordcount] | Use MapReduce and Accumulo to do a word count on text files |

## Release Testing

This repository can be used to test Accumulo release candidates.  See
[docs/release-testing.md](docs/release-testing.md).

[quickstart]: https://accumulo.apache.org/docs/2.x/getting-started/quickstart
[accumulo-client.properties]: https://accumulo.apache.org/docs/2.x/configuration/files#accumulo-clientproperties
[env.sh.example]: conf/env.sh.example
[manual]: https://accumulo.apache.org/latest/accumulo_user_manual/
[INSTALL.md]: https://github.com/apache/accumulo/blob/master/INSTALL.md
[batch]: docs/batch.md
[bloom]: docs/bloom.md
[bulkIngest]: docs/bulkIngest.md
[classpath]: docs/classpath.md
[client]: docs/client.md 
[combiner]: docs/combiner.md
[compactionStrategy]: docs/compactionStrategy.md
[constraints]: docs/constraints.md
[deleteKeyValuePair]: docs/deleteKeyValuePair.md
[dirlist]: docs/dirlist.md
[export]: docs/export.md
[filedata]: docs/filedata.md
[filter]: docs/filter.md
[helloworld]: docs/helloworld.md
[isolation]: docs/isolation.md
[maxmutation]: docs/maxmutation.md
[regex]: docs/regex.md
[reservations]: docs/reservations.md
[rgbalancer]: docs/rgbalancer.md
[rowhash]: docs/rowhash.md
[sample]: docs/sample.md
[shard]: docs/shard.md
[spark]: spark/README.md
[tabletofile]: docs/tabletofile.md
[terasort]: docs/terasort.md
[uniquecols]: docs/uniquecols.md
[visibility]: docs/visibility.md
[wordcount]: docs/wordcount.md
[ti]: https://travis-ci.org/apache/accumulo-examples.svg?branch=master
[tl]: https://travis-ci.org/apache/accumulo-examples
