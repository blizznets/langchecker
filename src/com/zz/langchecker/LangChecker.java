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
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

final class LangChecker {
  final Map<Lang, Set<String>> nonexistent2gram;
  final Map<Lang, Set<String>> nonexistent3gram;
  final Map<Lang, Set<String>> nonexistent4gram;

  public LangChecker(
      Map<Lang, Set<String>> nonexistent2gram,
      Map<Lang, Set<String>> nonexistent3gram,
      Map<Lang, Set<String>> nonexistent4gram) {
    this.nonexistent2gram = nonexistent2gram;
    this.nonexistent3gram = nonexistent3gram;
    this.nonexistent4gram = nonexistent4gram;
  }

  public static LangChecker create() {
    return new LangChecker(
        ImmutableMap.of(
            Lang.EN, readVocabulary("nonexistent2gram-en.txt"),
            Lang.RU, readVocabulary("nonexistent2gram-ru.txt")),
        ImmutableMap.of(
            Lang.EN, readVocabulary("nonexistent3gram-en.txt"),
            Lang.RU, readVocabulary("nonexistent3gram-ru.txt")),
        ImmutableMap.of(
            Lang.EN, readVocabulary("nonexistent4gram-en.txt"),
            Lang.RU, readVocabulary("nonexistent4gram-ru.txt")));
  }

  public boolean check(Lang lang, String word) {
    int length = word.length();

    if (!Characters.hasVowel(lang, word)) {
      return false;
    }

    if (length >= 6) {
      Optional<String> firstConsonant6gram = firstNgram(lang, word, 6, false);
      if (firstConsonant6gram.isPresent()) {
        return false;
      }
    }

    if (length >= 3) {
      Optional<String> firstVowel3gram = firstNgram(lang, word, 3, true);
      if (firstVowel3gram.isPresent() && nonexistent3gram.get(lang).contains(firstVowel3gram.get())) {
        return false;
      }
    }

    if (length >= 4) {
      Optional<String> firstConsonant4gram = firstNgram(lang, word, 4, false);
      if (firstConsonant4gram.isPresent() && nonexistent4gram.get(lang).contains(firstConsonant4gram.get())) {
        return false;
      }
    }

    Set<String> nonexistent2grams = nonexistent2gram.get(lang);
    if (length >= 2) {
      if (nonexistent2grams.contains("*" + word.substring(0, 2))
          || nonexistent2grams.contains(word.substring(length - 2) + "*")) {
        return false;
      }
    }

    if (length >= 4) {
      for (int i = 1; i < length - 2; i++) {
        if (nonexistent2grams.contains(word.substring(i, i + 2))) {
          return false;
        }
      }
    }

    return true;
  }

  static Optional<String> firstNgram(Lang lang, String word, int n, boolean vowel) {
    char[] chars = word.toCharArray();
    for (int begin = 0, end = n; end <= word.length(); begin++, end++) {
      boolean check = true;
      for (int i = begin; i < end; i++) {
        if (vowel && !Characters.isVowel(lang, chars[i])
            || !vowel && !Characters.isConsonant(lang, chars[i])) {
          check = false;
          break;
        }
      }
      if (check) {
        return Optional.of(word.substring(begin, end));
      }
    }

    return Optional.absent();
  }

  private static Set<String> readVocabulary(String name) {
    try {
      return ImmutableSet.copyOf(Resources
          .asCharSource(LangChecker.class.getResource(name), Charsets.UTF_8)
          .readLines());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
