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

import java.util.Collection;
import java.util.List;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.constraints.Constraint;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.examples.cli.ClientOpts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an accumulo constraint that ensures values are numeric strings.
 */
public class NumericValueConstraint implements Constraint {

  private static final Logger log = LoggerFactory.getLogger(NumericValueConstraint.class);

  static final short NON_NUMERIC_VALUE = 1;
  static final String VIOLATION_MESSAGE = "Value is not numeric";

  private static final List<Short> VIOLATION_LIST = List.of(NON_NUMERIC_VALUE);

  private boolean isNumeric(byte[] bytes) {
    for (byte b : bytes) {
      boolean ok = (b >= '0' && b <= '9');
      if (!ok)
        return false;
    }
    return true;
  }

  @Override
  public List<Short> check(Environment env, Mutation mutation) {
    Collection<ColumnUpdate> updates = mutation.getUpdates();

    for (ColumnUpdate columnUpdate : updates) {
      if (!isNumeric(columnUpdate.getValue()))
        return VIOLATION_LIST;
    }
    return null;
  }

  @Override
  public String getViolationDescription(short violationCode) {
    if (violationCode == NON_NUMERIC_VALUE) {
      return VIOLATION_MESSAGE;
    }
    return null;
  }

  public static void main(String[] args)
      throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
    ClientOpts opts = new ClientOpts();
    opts.parseArgs(NumericValueConstraint.class.getName(), args);

    try (AccumuloClient client = Accumulo.newClient().from(opts.getClientPropsPath()).build()) {
      ConstraintsCommon.createConstraintsTable(client);

      /*
       * Add the {@link NumericValueConstraint} constraint to the table. Be sure to use the fully
       * qualified class name
       */
      int num = client.tableOperations().addConstraint(ConstraintsCommon.CONSTRAINTS_TABLE,
          "org.apache.accumulo.examples.constraints.NumericValueConstraint");

      log.info("Attempting to write non numeric data to testConstraints");
      try (BatchWriter bw = client.createBatchWriter(ConstraintsCommon.CONSTRAINTS_TABLE)) {
        Mutation m = new Mutation("r1");
        m.put("cf1", "cq1", new Value(("value1--$$@@%%").getBytes()));
        bw.addMutation(m);
      } catch (MutationsRejectedException e) {
        e.getConstraintViolationSummaries()
            .forEach(m -> log.error(ConstraintsCommon.CONSTRAINT_VIOLATED_MSG, m.constrainClass));
      }
      client.tableOperations().removeConstraint(ConstraintsCommon.CONSTRAINTS_TABLE, num);
    }
  }
}
