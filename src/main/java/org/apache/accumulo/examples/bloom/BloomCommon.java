package org.apache.accumulo.examples.bloom;

import org.apache.accumulo.examples.common.Constants;

enum BloomCommon {
  ;
  public static final String BLOOM_TEST1_TABLE = Constants.NAMESPACE + ".bloom_test1";
  public static final String BLOOM_TEST2_TABLE = Constants.NAMESPACE + ".bloom_test2";
  public static final String BLOOM_TEST3_TABLE = Constants.NAMESPACE + ".bloom_test3";
  public static final String BLOOM_TEST4_TABLE = Constants.NAMESPACE + ".bloom_test4";
  public static final String BLOOM_ENABLED_PROPERTY = "table.bloom.enabled";
}
