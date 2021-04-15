package org.apache.accumulo.examples.client;

import org.apache.accumulo.examples.common.Constants;

enum ClientCommon {
  ;
  static final String BATCH_TABLE = Constants.NAMESPACE + ".batch";
  static final String ROWOPS_TABLE = Constants.NAMESPACE + ".rowops";
  static final String READWRITE_TABLE = Constants.NAMESPACE + ".readwrite";

  static final String TABLE_NOT_FOUND_MSG = "Could not find table {}: {}";
}
