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

## Setup instructions

Before running any of the examples, the following steps must be performed.

1. Install and run Accumulo via the instructions found in [INSTALL.md] of Accumulo's tarball. 
   Remember the instance name. It will be referred to as "instance" throughout the examples. A
   comma-separated list of zookeeper servers will be referred to as "zookeepers".

2. Create an Accumulo user (for help see the 'User Administration' section of the 
   [user manual][manual]), or use the root user. This user and their password should replace any
   reference to "username" or "password" in the examples. This user needs the ability to create
   tables.

3. Clone and build this repository.

        git clone https://github.com/apache/accumulo-examples.git
        mvn clean package

4. Specify Accumulo connection information in `conf/accumulo-client.properties`.  Some old examples
   still read connection information from an examples.conf file so that should also be configured.

        cd accumulo-examples
        nano conf/accumulo-client.properties
        cp examples.conf.template examples.conf
        nano examples.conf

5. The examples have some custom iterators that need to be executed by Accumulo tablet servers.
   Make them available by copying the accumulo-examples.jar to Accumulo's `lib/ext` directory.

        cp target/accumulo-examples-X.Y.Z.jar /path/accumulo/lib/ext/

6. Each Accumulo example has its own documentation and instructions for running the example which
   are linked to below.

When running the examples, remember the tips below:

* Examples are run using the `runex` command which is located in the `bin/` directory of this repo.
  The `runex` command is a simple wrapper around the Maven Exec plugin.
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
| [constraints] | Using constraints with tables. |
| [dirlist] | Storing filesystem information. |
| [export] | Exporting and importing tables. |
| [filedata] | Storing file data. |
| [filter] | Using the AgeOffFilter to remove records more than 30 seconds old. |
| [helloworld] | Inserting records both inside map/reduce jobs and outside. And reading records between two rows. |
| [isolation] | Using the isolated scanner to ensure partial changes are not seen. |
| [mapred] | Using MapReduce to read from and write to Accumulo tables. |
| [maxmutation] | Limiting mutation size to avoid running out of memory. |
| [regex] | Using MapReduce and Accumulo to find data using regular expressions. |
| [reservations] | Using conditional mutations to implement simple reservation system. |
| [rgbalancer] | Using a balancer to spread groups of tablets within a table evenly |
| [rowhash] | Using MapReduce to read a table and write to a new column in the same table. |
| [sample] | Building and using sample data in Accumulo. |
| [shard] | Using the intersecting iterator with a term index partitioned by document. |
| [tabletofile] | Using MapReduce to read a table and write one of its columns to a file in HDFS. |
| [terasort] | Generating random data and sorting it using Accumulo. |
| [visibility] | Using visibilities (or combinations of authorizations). Also shows user permissions. |

## Release Testing

This repository can be used to test Accumulo release candidates.  See
[docs/release-testing.md](docs/release-testing.md).

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
[dirlist]: docs/dirlist.md
[export]: docs/export.md
[filedata]: docs/filedata.md
[filter]: docs/filter.md
[helloworld]: docs/helloworld.md
[isolation]: docs/isolation.md
[mapred]: docs/mapred.md
[maxmutation]: docs/maxmutation.md
[regex]: docs/regex.md
[reservations]: docs/reservations.md
[rgbalancer]: docs/rgbalancer.md
[rowhash]: docs/rowhash.md
[sample]: docs/sample.md
[shard]: docs/shard.md
[tabletofile]: docs/tabletofile.md
[terasort]: docs/terasort.md
[visibility]: docs/visibility.md
