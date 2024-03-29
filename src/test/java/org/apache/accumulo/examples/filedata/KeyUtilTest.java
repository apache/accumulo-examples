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
package org.apache.accumulo.examples.filedata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.hadoop.io.Text;
import org.junit.jupiter.api.Test;

public class KeyUtilTest {
  public static void checkSeps(String... s) {
    Text t = KeyUtil.buildNullSepText(s);
    String[] rets = KeyUtil.splitNullSepText(t);

    int length = 0;
    for (String str : s)
      length += str.length();
    assertEquals(t.getLength(), length + s.length - 1);
    assertEquals(rets.length, s.length);
    for (int i = 0; i < s.length; i++)
      assertEquals(s[i], rets[i]);
  }

  @Test
  public void testNullSep() {
    checkSeps("abc", "d", "", "efgh");
    checkSeps("ab", "");
    checkSeps("abcde");
    checkSeps("");
    checkSeps("", "");
  }
}
