/*
 *
 *     Copyright 2013-2014 https://github.com/blizznets authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.zz.langchecker;

import org.junit.Test;
import static org.immutables.check.Checkers.*;

public class CharTypeTest {
  @Test
  public void canCheckContains() {
    CharType.Set set = CharType.createSet();
    set.add(CharType.EN_OR_POSSIBLE_RU);
    set.add(CharType.EN_OR_POSSIBLE_RU);
    set.add(CharType.SEPARATOR);

    check(set.contains(CharType.EN_OR_POSSIBLE_RU));
    check(set.contains(CharType.SEPARATOR));
    check(!set.contains(CharType.RU_OR_POSSIBLE_EN));
  }

  @Test
  public void canCheckContainsOnly() {
    CharType.Set set = CharType.createSet();
    set.add(CharType.RU_OR_POSSIBLE_SEPARATOR);

    check(set.containsOnly(CharType.RU_OR_POSSIBLE_SEPARATOR));
    check(!set.containsOnly(CharType.RU_OR_POSSIBLE_EN));

    set.add(CharType.RU_OR_POSSIBLE_EN);

    check(!set.containsOnly(CharType.RU_OR_POSSIBLE_SEPARATOR));
    check(!set.containsOnly(CharType.RU_OR_POSSIBLE_EN));
  }

  @Test
  public void canCheckContainsOnlyForTwoCharTypes() {
    CharType.Set set = CharType.createSet();
    set.add(CharType.RU_OR_POSSIBLE_SEPARATOR);

    check(!set.containsOnly(CharType.RU_OR_POSSIBLE_SEPARATOR, CharType.RU_OR_POSSIBLE_EN));

    set.add(CharType.RU_OR_POSSIBLE_EN);

    check(set.containsOnly(CharType.RU_OR_POSSIBLE_SEPARATOR, CharType.RU_OR_POSSIBLE_EN));

    set.add(CharType.SEPARATOR);

    check(!set.containsOnly(CharType.RU_OR_POSSIBLE_SEPARATOR, CharType.RU_OR_POSSIBLE_EN));
  }

  @Test
  public void canCheckContainsOnlyFirstOrBoth() {
    CharType.Set set = CharType.createSet();
    set.add(CharType.RU_OR_POSSIBLE_SEPARATOR);

    check(set.containsOnlyFirstOrBoth(CharType.RU_OR_POSSIBLE_SEPARATOR, CharType.RU_OR_POSSIBLE_EN));
    check(!set.containsOnlyFirstOrBoth(CharType.RU_OR_POSSIBLE_EN, CharType.RU_OR_POSSIBLE_SEPARATOR));

    set.add(CharType.RU_OR_POSSIBLE_EN);

    check(set.containsOnlyFirstOrBoth(CharType.RU_OR_POSSIBLE_SEPARATOR, CharType.RU_OR_POSSIBLE_EN));
    check(set.containsOnlyFirstOrBoth(CharType.RU_OR_POSSIBLE_EN, CharType.RU_OR_POSSIBLE_SEPARATOR));
  }
}
