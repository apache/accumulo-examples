package org.apache.accumulo.examples.bloom;

import org.apache.accumulo.examples.common.Constants;

enum BloomCommon {
  ;
  static final String BLOOM_TEST1_TABLE = Constants.NAMESPACE + ".bloom_test1";
  static final String BLOOM_TEST2_TABLE = Constants.NAMESPACE + ".bloom_test2";
  static final String BLOOM_TEST3_TABLE = Constants.NAMESPACE + ".bloom_test3";
  static final String BLOOM_TEST4_TABLE = Constants.NAMESPACE + ".bloom_test4";
  static final String BLOOM_ENABLED_PROPERTY = "table.bloom.enabled";
}
