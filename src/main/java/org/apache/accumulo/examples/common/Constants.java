package org.apache.accumulo.examples.common;

public enum Constants {
  ;
  public static final String NAMESPACE = "examples";

  public static String TABLE_EXISTS_MSG = "Table already exists. User may wish to delete existing "
      + "table and re-run example. Table name: ";
  public static String NAMESPACE_EXISTS_MSG = "Namespace already exists. Message can be ignored. "
      + "Namespace: ";
  public static String ALL_ROWS_SCANNED = "All expected rows were scanned";
}
