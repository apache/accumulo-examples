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
# Apache Accumulo Terasort Example

This example uses map/reduce to generate random input data that will
be sorted by storing it into accumulo. It uses data very similar to the
hadoop terasort benchmark.

First, make sure the 'examples' namespace exists. If it already exists, the error message can be 
ignored.

    $ accumulo shell -u root -p secret -e 'createnamespace examples'   

This example is run with arguments describing the amount of data:

    $ ./bin/runmr mapreduce.TeraSortIngest --count 10 --minKeySize 10 --maxKeySize 10 \
        --minValueSize 78 --maxValueSize 78 --table examples.sort --splits 10

After the map reduce job completes, scan the data:

    $ accumulo shell
    username@instance> scan -t examples.sort
    +l-$$OE/ZH c:         4 []    GGGGGGGGGGWWWWWWWWWWMMMMMMMMMMCCCCCCCCCCSSSSSSSSSSIIIIIIIIIIYYYYYYYYYYOOOOOOOO
    ,C)wDw//u= c:        10 []    CCCCCCCCCCSSSSSSSSSSIIIIIIIIIIYYYYYYYYYYOOOOOOOOOOEEEEEEEEEEUUUUUUUUUUKKKKKKKK
    75@~?'WdUF c:         1 []    IIIIIIIIIIYYYYYYYYYYOOOOOOOOOOEEEEEEEEEEUUUUUUUUUUKKKKKKKKKKAAAAAAAAAAQQQQQQQQ
    ;L+!2rT~hd c:         8 []    MMMMMMMMMMCCCCCCCCCCSSSSSSSSSSIIIIIIIIIIYYYYYYYYYYOOOOOOOOOOEEEEEEEEEEUUUUUUUU
    LsS8)|.ZLD c:         5 []    OOOOOOOOOOEEEEEEEEEEUUUUUUUUUUKKKKKKKKKKAAAAAAAAAAQQQQQQQQQQGGGGGGGGGGWWWWWWWW
    M^*dDE;6^< c:         9 []    UUUUUUUUUUKKKKKKKKKKAAAAAAAAAAQQQQQQQQQQGGGGGGGGGGWWWWWWWWWWMMMMMMMMMMCCCCCCCC
    ^Eu)<n#kdP c:         3 []    YYYYYYYYYYOOOOOOOOOOEEEEEEEEEEUUUUUUUUUUKKKKKKKKKKAAAAAAAAAAQQQQQQQQQQGGGGGGGG
    le5awB.$sm c:         6 []    WWWWWWWWWWMMMMMMMMMMCCCCCCCCCCSSSSSSSSSSIIIIIIIIIIYYYYYYYYYYOOOOOOOOOOEEEEEEEE
    q__[fwhKFg c:         7 []    EEEEEEEEEEUUUUUUUUUUKKKKKKKKKKAAAAAAAAAAQQQQQQQQQQGGGGGGGGGGWWWWWWWWWWMMMMMMMM
    w[o||:N&H, c:         2 []    QQQQQQQQQQGGGGGGGGGGWWWWWWWWWWMMMMMMMMMMCCCCCCCCCCSSSSSSSSSSIIIIIIIIIIYYYYYYYY

Of course, a real benchmark would ingest millions of entries.
