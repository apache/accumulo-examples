package org.apache.accumulo.examples.constraints;

import org.apache.accumulo.examples.Common;

public enum ConstraintsCommon {
  ;
  public static final String CONSTRAINTS_TABLE = Common.NAMESPACE + ".testConstraints";
  public static final String CONSTRAINT_VIOLATED_MSG = "Constraint violated: {}";
}
