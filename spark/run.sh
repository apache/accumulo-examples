#!/bin/bash

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
