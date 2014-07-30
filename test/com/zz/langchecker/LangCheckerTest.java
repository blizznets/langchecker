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

public class LangCheckerTest {
  @Test
  public void canFindFirstVowel3Gram() {
    check(LangChecker.firstNgram(Lang.EN, "wasidort", 3, true).or("")).is("");
    check(LangChecker.firstNgram(Lang.EN, "waouil", 3, true).or("")).is("aou");

    check(LangChecker.firstNgram(Lang.RU, "бавигад", 3, true).or("")).is("");
    check(LangChecker.firstNgram(Lang.RU, "аабууу", 3, true).or("")).is("ууу");
  }
}
