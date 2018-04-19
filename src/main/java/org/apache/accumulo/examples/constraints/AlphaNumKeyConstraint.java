/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.examples.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.constraints.Constraint;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

/**
 * This class is an accumulo constraint that ensures all fields of a key are alpha numeric.
 */
public class AlphaNumKeyConstraint implements Constraint {

  static final short NON_ALPHA_NUM_ROW = 1;
  static final short NON_ALPHA_NUM_COLF = 2;
  static final short NON_ALPHA_NUM_COLQ = 3;

  static final String ROW_VIOLATION_MESSAGE = "Row was not alpha numeric";
  static final String COLF_VIOLATION_MESSAGE = "Column family was not alpha numeric";
  static final String COLQ_VIOLATION_MESSAGE = "Column qualifier was not alpha numeric";

  private boolean isAlphaNum(byte bytes[]) {
    for (byte b : bytes) {
      boolean ok = ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9'));
      if (!ok)
        return false;
    }

    return true;
  }

  private Set<Short> addViolation(Set<Short> violations, short violation) {
    if (violations == null) {
      violations = new LinkedHashSet<>();
      violations.add(violation);
    } else if (!violations.contains(violation)) {
      violations.add(violation);
    }
    return violations;
  }

  @Override
  public List<Short> check(Environment env, Mutation mutation) {
    Set<Short> violations = null;

    if (!isAlphaNum(mutation.getRow()))
      violations = addViolation(violations, NON_ALPHA_NUM_ROW);

    Collection<ColumnUpdate> updates = mutation.getUpdates();
    for (ColumnUpdate columnUpdate : updates) {
      if (!isAlphaNum(columnUpdate.getColumnFamily()))
        violations = addViolation(violations, NON_ALPHA_NUM_COLF);

      if (!isAlphaNum(columnUpdate.getColumnQualifier()))
        violations = addViolation(violations, NON_ALPHA_NUM_COLQ);
    }

    return null == violations ? null : new ArrayList<>(violations);
  }

  @Override
  public String getViolationDescription(short violationCode) {

    switch (violationCode) {
      case NON_ALPHA_NUM_ROW:
        return ROW_VIOLATION_MESSAGE;
      case NON_ALPHA_NUM_COLF:
        return COLF_VIOLATION_MESSAGE;
      case NON_ALPHA_NUM_COLQ:
        return COLQ_VIOLATION_MESSAGE;
    }

    return null;
  }

  public static void main(String[] args) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    Connector connector = Connector.builder().usingProperties("conf/accumulo-client.properties").build();
    try {
      connector.tableOperations().create("testConstraints");
    } catch (TableExistsException e) {
      // ignore
    }

    /**
     * Add the {@link AlphaNumKeyConstraint} to the table. Be sure to use the fully qualified class name.
     */
    int num = connector.tableOperations().addConstraint("testConstraints", "org.apache.accumulo.examples.constraints.AlphaNumKeyConstraint");

    System.out.println("Attempting to write non alpha numeric data to testConstraints");
    try (BatchWriter bw = connector.createBatchWriter("testConstraints")) {
      Mutation m = new Mutation("r1--$$@@%%");
      m.put("cf1", "cq1", new Value(("value1").getBytes()));
      bw.addMutation(m);
    } catch (MutationsRejectedException e) {
      e.getConstraintViolationSummaries().forEach(violationSummary -> System.out.println("Constraint violated: " + violationSummary.constrainClass));
    }

    connector.tableOperations().removeConstraint("testConstraints", num);
  }
}
