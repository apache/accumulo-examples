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
# Apache Accumulo RowHash Example

This example shows a simple map/reduce job that reads from an accumulo table and
writes back into that table.

To run this example you will need some data in a table. The following will
put a trivial amount of data into accumulo using the accumulo shell:

    $ accumulo shell
    username@instance> createtable rowhash
    username@instance> insert a-row cf cq value
    username@instance> insert b-row cf cq value
    username@instance> quit

The RowHash class will insert a hash for each row in the database if it contains a
specified colum. Here's how you run the map/reduce job

    $ ./bin/runmr mapreduce.RowHash -t rowhash --column cf:cq

Now we can scan the table and see the hashes:

    $ accumulo shell
    username@instance> scan -t rowhash
    a-row cf:cq []    value
    a-row cf-HASHTYPE:cq-MD5BASE64 []    IGPBYI1uC6+AJJxC4r5YBA==
    b-row cf:cq []    value
    b-row cf-HASHTYPE:cq-MD5BASE64 []    IGPBYI1uC6+AJJxC4r5YBA==
    username@instance>

