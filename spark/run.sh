#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


if [[ -z "$1" || -z "$2" ]]; then
  echo "Usage: ./run.sh [bulk|batch] /path/to/accumulo-client.properties"
  exit 1
fi

JAR=./target/accumulo-spark-shaded.jar
if [[ ! -f $JAR ]]; then
  mvn clean package -P create-shade-jar
fi

if [[ -z "$SPARK_HOME" ]]; then
  echo "SPARK_HOME must be set!"
  exit 1
fi

if [[ -z "$HADOOP_CONF_DIR" ]]; then
  echo "HADOOP_CONF_DIR must be set!"
  exit 1
fi

"$SPARK_HOME"/bin/spark-submit \
  --class org.apache.accumulo.spark.CopyPlus5K \
  --master yarn \
  --deploy-mode client \
  $JAR \
  $1 $2
