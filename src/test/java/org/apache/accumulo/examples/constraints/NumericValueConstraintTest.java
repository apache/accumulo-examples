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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

public class NumericValueConstraintTest {

  private final NumericValueConstraint nvc = new NumericValueConstraint();

  @Test
  public void testCheck() {
    Mutation goodMutation = new Mutation("r");
    goodMutation.put("cf", "cq", new Value("1234".getBytes()));
    assertNull(nvc.check(null, goodMutation));

    // Check that multiple bad mutations result in one violation only
    Mutation badMutation = new Mutation("r");
    badMutation.put("cf", "cq", new Value("foo1234".getBytes()));
    badMutation.put("cf2", "cq2", new Value("foo1234".getBytes()));
    assertEquals(NumericValueConstraint.NON_NUMERIC_VALUE,
        Iterables.getOnlyElement(nvc.check(null, badMutation)).shortValue());
  }

  @Test
  public void testGetViolationDescription() {
    assertEquals(NumericValueConstraint.VIOLATION_MESSAGE,
        nvc.getViolationDescription(NumericValueConstraint.NON_NUMERIC_VALUE));
    assertNull(nvc.getViolationDescription((short) 2));
  }
}
