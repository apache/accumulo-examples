package org.apache.accumulo.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

  private static final Logger log = LoggerFactory.getLogger(Constants.class);

  // Table names
  public static String TABLE_PREFIX = "accex_";
  public static String BATCH_TABLE = TABLE_PREFIX + "batch";

  // Batch Example

  // log messages
  public static String TABLE_EXISTS_MSG = "Table already exists. User may wish to delete existing "
      + "table and rerun example. Table name: ";

}
