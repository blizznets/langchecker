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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Chars;
import java.util.Map;

final class Characters {
  private static final char[] VOWELS_RU = {
      'а', 'е', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я', 'ё'};

  private static final char[] CONSONANTS_RU = {
      'б', 'в', 'г', 'д', 'ж', 'з', 'й', 'к', 'л', 'м', 'н',
      'п', 'р', 'с', 'т', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ь'};

  private static final char[] VOWELS_EN = {
      'a', 'e', 'i', 'o', 'u', 'y'};

  private static final char[] CONSONANTS_EN = {
      'b', 'c', 'd', 'f', 'h', 'g', 'j', 'k', 'l', 'm',
      'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'z'};

  private static final Map<Lang, char[]> VOWELS = ImmutableMap.of(Lang.RU, VOWELS_RU, Lang.EN, VOWELS_EN);

  private static final Map<Lang, char[]> CONSONANTS = ImmutableMap.of(Lang.RU, CONSONANTS_RU, Lang.EN, CONSONANTS_EN);

  // 0
  private static final char[] SEPARATORS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      ' ', '\t', '\r', '\n', '!', '?', '_', '=', '-', '+', '*', '/', '|', '\\', '@', '#', '$', '%', '^', '&', '(', ')'};

  // 1
  private static final char[] SEPARATOR_OR_POSSIBLE_RU = {
      '<', ',', '~', '`', ':', ';', '{', '[', '}', ']', '"', '\'', '>', '.'};

  // 2
  private static final char[] EN_OR_POSSIBLE_RU = {
      'f', 'd', 'u', 'l', 't', 'p', 'b', 'q', 'r', 'k', 'v', 'y', 'j', 'g', 'h', 'c', 'n', 'e', 'a',
      'w', 'x', 'i', 'o', 's', 'm', '\'', 'z'};

  // 3
  private static final char[] RU_OR_POSSIBLE_EN = {
      'ф', 'и', 'с', 'в', 'у', 'а', 'п', 'р', 'ш', 'о', 'л', 'д', 'ь', 'т', 'щ',
      'з', 'й', 'к', 'ы', 'е', 'г', 'м', 'ц', 'ч', 'н', 'я', 'э'};

  // 4
  private static final char[] RU_OR_POSSIBLE_SEPARATOR = {
      'б', 'ё', 'ж', 'х', 'ъ', 'э', 'ю'};

  private static final Map<Character, Character> SWITCH_RU_FROM_EN; static {
    SWITCH_RU_FROM_EN = ImmutableMap.<Character, Character>builder()
        .put('f', 'а')
        .put('<', 'б').put(',', 'б')
        .put('d', 'в')
        .put('u', 'г')
        .put('l', 'д')
        .put('t', 'е')
        .put('~', 'ё').put('`', 'ё')
        .put(':', 'ж').put(';', 'ж')
        .put('p', 'з')
        .put('b', 'и')
        .put('q', 'й')
        .put('r', 'к')
        .put('k', 'л')
        .put('v', 'м')
        .put('y', 'н')
        .put('j', 'о')
        .put('g', 'п')
        .put('h', 'р')
        .put('c', 'с')
        .put('n', 'т')
        .put('e', 'у')
        .put('a', 'ф')
        .put('{', 'х').put('[', 'х')
        .put('w', 'ц')
        .put('x', 'ч')
        .put('i', 'ш')
        .put('o', 'щ')
        .put('}', 'ъ').put(']', 'ъ')
        .put('s', 'ы')
        .put('m', 'ь')
        .put('"', 'э').put('\'', 'э')
        .put('>', 'ю').put('.', 'ю')
        .put('z', 'я')
        .build();
  }

  private static final Map<Character, Character> SWITCH_EN_FROM_RU; static {
    SWITCH_EN_FROM_RU = ImmutableMap.<Character, Character>builder()
        .put('ф', 'a')
        .put('и', 'b')
        .put('с', 'c')
        .put('в', 'd')
        .put('у', 'e')
        .put('а', 'f')
        .put('п', 'g')
        .put('р', 'h')
        .put('ш', 'i')
        .put('о', 'j')
        .put('л', 'k')
        .put('д', 'l')
        .put('ь', 'm')
        .put('т', 'n')
        .put('щ', 'o')
        .put('з', 'p')
        .put('й', 'q')
        .put('к', 'r')
        .put('ы', 's')
        .put('е', 't')
        .put('г', 'u')
        .put('м', 'v')
        .put('ц', 'w')
        .put('ч', 'x')
        .put('н', 'y')
        .put('я', 'z')
        .put('э', '\'')
        .put('б', ',')
        .put('ё', '\'')
        .put('ж', ';')
        .put('х', '[')
        .put('ъ', ']')
        .put('ю', '.')
        .build();
  }

  private static final Map<Lang, Map<Character, Character>> KEYBOARD_LAYOUTS =
      ImmutableMap.of(Lang.RU, SWITCH_RU_FROM_EN, Lang.EN, SWITCH_EN_FROM_RU);

  private Characters() {
  }

  public static String switchLang(String word, Lang destinationLang) {
    Map<Character, Character> switchTable = KEYBOARD_LAYOUTS.get(destinationLang);
    char[] chars = word.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      Character replacement = switchTable.get(chars[i]);
      if (replacement != null) {
        chars[i] = replacement;
      }
    }
    return new String(chars);
  }

  public static Function<String, String> switchLangFunction(final Lang destinationLang) {

    return new Function<String, String>() {
      @Override
      public String apply(String input) {
        return switchLang(input, destinationLang);
      }
    };
  }

  public static boolean isSeparator(char ch) {
    return Chars.contains(SEPARATORS, ch);
  }

  public static Predicate<Character> isSeparator() {
    return IsSeparator.PREDICATE;
  }

  enum IsSeparator implements Predicate<Character> {
    PREDICATE;

    @Override
    public boolean apply(Character input) {
      return isSeparator(input);
    }
  }

  public static boolean isSeparatorOrPossibleRu(char ch) {
    return Chars.contains(SEPARATOR_OR_POSSIBLE_RU, ch);
  }

  public static Predicate<Character> isSeparatorOrPossibleRu() {
    return IsSeparatorOrPossibleRu.PREDICATE;
  }

  enum IsSeparatorOrPossibleRu implements Predicate<Character> {
    PREDICATE;

    @Override
    public boolean apply(Character input) {
      return isSeparatorOrPossibleRu(input);
    }
  }

  public static boolean isEnOrPossibleRu(char ch) {
    return Chars.contains(EN_OR_POSSIBLE_RU, ch);
  }

  public static boolean isRuOrPossibleEn(char ch) {
    return Chars.contains(RU_OR_POSSIBLE_EN, ch);
  }

  public static boolean isRuOrPossibleSeparator(char ch) {
    return Chars.contains(RU_OR_POSSIBLE_SEPARATOR, ch);
  }

  /**
   * @param ch the given char
   * @return true if russian in lower case, false otherwise
   */
  public static boolean isRussianChar(char ch) {
    return ch == 'ё' || ch >= 'а' && ch <= 'я';
  }

  /**
   * @param word the given word
   * @return true if russian in lower case without separators, false otherwise
   */
  public static boolean isRussianWord(String word) {
    for (char ch : word.toCharArray()) {
      if (!isRussianChar(ch)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param ch the given char
   * @return true if english in lower case, false otherwise
   */
  public static boolean isEnglishChar(char ch) {
    return ch >= 'a' && ch <= 'z' || ch == '\'';
  }

  /**
   * @param word the given word
   * @return true if english in lower case without separators, false otherwise
   */
  public static boolean isEnglishWord(String word) {
    for (char ch : word.toCharArray()) {
      if (!isEnglishChar(ch)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isVowel(Lang lang, char ch) {
    return Chars.contains(VOWELS.get(lang), ch);
  }

  public static boolean isConsonant(Lang lang, char ch) {
    return Chars.contains(CONSONANTS.get(lang), ch);
  }

  public static boolean hasVowel(Lang lang, String word) {
    for (char ch : word.toCharArray()) {
      if (isVowel(lang, ch)) {
        return true;
      }
    }
    return false;
  }
}
