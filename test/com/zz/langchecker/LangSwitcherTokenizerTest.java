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

public class LangSwitcherTokenizerTest {
  @Test
  public void emptyIsEmpty() {
    check(LangSwitcherTokenizer.create().tokenize("")).hasToString("");
  }

  @Test
  public void canDetectCorrectEnglishWords() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize("beautiful")).hasToString("beautiful");
    check(tokenizer.tokenize("lesson")).hasToString("lesson");
    check(tokenizer.tokenize("bullet")).hasToString("bullet");

    check(tokenizer.tokenize("dark.light")).hasToString("dark.light");
  }

  @Test
  public void canDetectCorrectRussianWords() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize("источник")).hasToString("источник");
    check(tokenizer.tokenize("утопия")).hasToString("утопия");
    check(tokenizer.tokenize("взломщик")).hasToString("взломщик");
    check(tokenizer.tokenize("борт")).hasToString("борт");
  }

  @Test
  public void canDetectSwitchedEnglishWords() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize("игшдвштп")).hasToString("building");
    check(tokenizer.tokenize("афдсщт")).hasToString("falcon");
    check(tokenizer.tokenize("зфкфвшыу")).hasToString("paradise");

    check(tokenizer.tokenize("ьгышсбифк")).hasToString("music,bar");
  }

  @Test
  public void canDetectSwitchedRussianWords() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize("xfcnbwf")).hasToString("частица");
    check(tokenizer.tokenize("flhtc")).hasToString("адрес");
    check(tokenizer.tokenize("gbhfvblf")).hasToString("пирамида");
  }

  @Test
  public void canDetectSwitchedRussianWordsWithSeparators() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize(",bkmzhl")).hasToString("бильярд");
    check(tokenizer.tokenize("k.,jdm")).hasToString("любовь");
    check(tokenizer.tokenize("gjl]tpl")).hasToString("подъезд");
  }

  @Test
  public void canDetectDigits() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize("24")).hasToString("24");
    check(tokenizer.tokenize(",jhm,f24")).hasToString("борьба24");
    check(tokenizer.tokenize("4еуые")).hasToString("4test");
    check(tokenizer.tokenize("дшау4пщщв")).hasToString("life4good");
  }

  @Test
  public void canDetectUppercaseLetters() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize("<>HJ")).hasToString("БЮРО");
    check(tokenizer.tokenize("<.hj")).hasToString("Бюро");
    check(tokenizer.tokenize("<.hJ")).hasToString("БюрО");
    check(tokenizer.tokenize("{jl")).hasToString("Ход");
    check(tokenizer.tokenize("[JL")).hasToString("хОД");

    check(tokenizer.tokenize("ЬФН еРу аЩКсу ИУ цшер нщГ")).hasToString("MAY tHe fORce BE with yoU");
  }

  @Test
  public void leaveAsIsIfUnknown() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize("xcvn")).hasToString("xcvn");
    check(tokenizer.tokenize("чсмт")).hasToString("чсмт");

    check(tokenizer.tokenize("вбрз")).hasToString("вбрз");
    check(tokenizer.tokenize("хбхб")).hasToString("хбхб");
  }

  @Test
  public void leaveAsIsIfEnAbbreviations() {
    LangSwitcherTokenizer tokenizer = LangSwitcherTokenizer.create();

    check(tokenizer.tokenize("T.G.I")).hasToString("T.G.I");
    check(tokenizer.tokenize("r.i.p.")).hasToString("r.i.p.");
  }
}
