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

enum CharType {
  SEPARATOR(0b1),
  SEPARATOR_OR_POSSIBLE_RU(0b10),
  EN_OR_POSSIBLE_RU(0b100),
  RU_OR_POSSIBLE_EN(0b1000),
  RU_OR_POSSIBLE_SEPARATOR(0b10000);

  private final int mask;

  private CharType(int mask) {
    this.mask = mask;
  }

  public static CharType of(char ch) {
    // XXX order is important: all about apostrophe
    if (Characters.isEnOrPossibleRu(ch)) {
      return EN_OR_POSSIBLE_RU;
    }

    if (Characters.isSeparatorOrPossibleRu(ch)) {
      return SEPARATOR_OR_POSSIBLE_RU;
    }

    if (Characters.isRuOrPossibleSeparator(ch)) {
      return RU_OR_POSSIBLE_SEPARATOR;
    }

    if (Characters.isRuOrPossibleEn(ch)) {
      return RU_OR_POSSIBLE_EN;
    }

    return SEPARATOR;
  }

  public static Set createSet() {
    return new Set();
  }

  public static final class Set {
    private int mask;

    private Set() {
      this.mask = 0;
    }

    public void add(CharType charType) {
      this.mask |= charType.mask;
    }

    public boolean contains(CharType charType) {
      return (this.mask & charType.mask) != 0;
    }

    public boolean containsOnly(CharType charType) {
      return this.mask == charType.mask;
    }

    public boolean containsOnly(CharType charType1, CharType charType2) {
      return this.mask == (charType1.mask | charType2.mask);
    }

    public boolean containsOnlyFirstOrBoth(CharType charType1, CharType charType2) {
      return containsOnly(charType1) || containsOnly(charType1, charType2);
    }

    @Override
    public String toString() {
      return Integer.toBinaryString(mask);
    }
  }
}
