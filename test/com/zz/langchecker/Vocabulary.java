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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.List;

public final class Vocabulary {
  private static ImmutableList<String> wordsRu; static {
    try {
      wordsRu = Resources
          .asCharSource(Vocabulary.class.getResource("words-ru.txt"), Charsets.UTF_8)
          .readLines();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static ImmutableList<String> wordsEn; static {
    try {
      wordsEn = Resources
          .asCharSource(Vocabulary.class.getResource("words-en.txt"), Charsets.UTF_8)
          .readLines();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Vocabulary() {
  }

  public static List<String> wordsRu() {
    return wordsRu;
  }

  public static List<String> wordsEn() {
    return wordsEn;
  }
}
