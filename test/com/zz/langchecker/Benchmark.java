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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {
  public static void main(String[] args) throws IOException {
    //        langCheckerRu();
    //        langCheckerEn();
    langSwitcherSeparatorRu();
    langSwitcherSeparatorEn();
  }

  private static void langCheckerRu() {
    System.out.println("lang checker ru");

    List<String> vocabulary = Vocabulary.wordsRu();
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<String> switchedVocabulary = Lists.transform(vocabulary, Characters.switchLangFunction(Lang.EN));
    stopwatch.stop();

    System.out.println("\toriginal");
    checkWords(vocabulary, LangChecker.create(), Lang.RU);

    System.out.println("\tswitched: " + stopwatch);
    checkWords(switchedVocabulary, LangChecker.create(), Lang.EN);
  }

  private static void langCheckerEn() {
    System.out.println("lang checker en");

    List<String> vocabulary = Vocabulary.wordsEn();
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<String> switchedVocabulary = Lists.transform(vocabulary, Characters.switchLangFunction(Lang.RU));
    stopwatch.stop();

    System.out.println("\toriginal");
    checkWords(vocabulary, LangChecker.create(), Lang.EN);

    System.out.println("\tswitched: " + stopwatch);
    checkWords(switchedVocabulary, LangChecker.create(), Lang.RU);
  }

  private static void checkWords(List<String> vocabulary, LangChecker langChecker, Lang lang) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    int positive = 0;
    int negative = 0;

    for (String s : vocabulary) {
      if (!s.isEmpty()) {
        boolean check = langChecker.check(lang, s);
        if (check) {
          positive++;
        } else {
          negative++;
        }
      }
    }

    System.out.println("\t\t" + stopwatch.stop());
    System.out.println("\t\tpos=" + positive);
    System.out.println("\t\tneg=" + negative);
  }

  private static void langSwitcherSeparatorRu() {
    System.out.println("lang switcher separator ru");

    List<String> vocabulary = Vocabulary.wordsRu();
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<String> switchedVocabulary = Lists.transform(vocabulary, Characters.switchLangFunction(Lang.EN));
    stopwatch.stop();

    System.out.println("\toriginal");
    runOnWords(vocabulary, LangSwitcherTokenizer.create());

    System.out.println("\tswitched: " + stopwatch);
    runOnWords(switchedVocabulary, LangSwitcherTokenizer.create());
  }

  private static void langSwitcherSeparatorEn() {
    System.out.println("lang switcher separator en");

    List<String> vocabulary = Vocabulary.wordsEn();
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<String> switchedVocabulary = Lists.transform(vocabulary, Characters.switchLangFunction(Lang.RU));
    stopwatch.stop();

    System.out.println("\toriginal");
    runOnWords(vocabulary, LangSwitcherTokenizer.create());

    System.out.println("\tswitched: " + stopwatch);
    runOnWords(switchedVocabulary, LangSwitcherTokenizer.create());
  }

  private static void runOnWords(List<String> vocabulary, LangSwitcherTokenizer separator) {
    ArrayList<TokenizerResponse> responses = Lists.newArrayList();
    Stopwatch stopwatch = Stopwatch.createStarted();
    int positive = 0;
    int negative = 0;

    for (String s : vocabulary) {
      if (!s.isEmpty()) {
        TokenizerResponse response = separator.tokenize(s);
        responses.add(response);
        if (response.corrected().isPresent()) {
          negative++;
        } else {
          positive++;
        }
      }
    }

    System.out.println("\t\t" + stopwatch.stop() + ", responses: " + responses.size());
    System.out.println("\t\tpos=" + positive);
    System.out.println("\t\tneg=" + negative);
  }
}
