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
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static com.zz.langchecker.Characters.*;

public final class LangSwitcherTokenizer implements Tokenizer {
  private static final Joiner JOINER = Joiner.on("");

  private static final char APOSTROPHE = '\'';
  private static final char APOSTROPHE_1 = '`';

  final LangChecker langChecker;
  final Map<String, String> exceptions;

  LangSwitcherTokenizer(LangChecker langChecker) {
    this.langChecker = langChecker;

    try {
      this.exceptions = Resources.readLines(
          this.getClass().getResource("exceptions.csv"),
          Charsets.UTF_8,
          new ExceptionsLineProcessor());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public static LangSwitcherTokenizer create() {
    return new LangSwitcherTokenizer(LangChecker.create());
  }

  @Override
  public TokenizerResponse tokenize(String input) {
    List<Integer> uppercasePositions = uppercasePositions(input);
    String canonical = canonical(input);
    List<Token> allTokens = split(canonical);
    List<String> wordTokens = FluentIterable.from(allTokens)
        .filter(TokenFunctions.isWord())
        .transform(TokenFunctions.corrected())
        .toList();
    String corrected = restoreUppercase(
        JOINER.join(Lists.transform(allTokens, TokenFunctions.corrected())),
        uppercasePositions);

    return ImmutableTokenizerResponse.builder()
        .original(input)
        .addAllTokens(wordTokens)
        .corrected(corrected.equals(canonical)
            ? Optional.<String>absent()
            : Optional.of(corrected))
        .build();
  }

  List<Token> split(String input) {
    List<Token> tokens = Lists.newArrayList();
    for (Token token : splitBySpecificSeparators(input, isSeparator(), true)) {
      tokens.addAll(splitPossibleSubTokens(token));
    }
    return ImmutableList.copyOf(tokens);
  }

  List<Token> splitBySpecificSeparators(String original, Predicate<Character> isSeparator, boolean useExceptions) {
    if (original.isEmpty()) {
      return ImmutableList.of();
    }

    List<Token> tokens = Lists.newArrayList();

    char[] chars = original.toCharArray();
    boolean isPrevSeparator = false;
    boolean isPrevDigit = false;
    int start = 0;
    CharType.Set charTypes = CharType.createSet();

    for (int i = 0; i < chars.length; i++) {
      char ch = chars[i];

      boolean isCurrentSeparator = isSeparator.apply(ch);
      boolean isCurrentDigit = Character.isDigit(ch);

      if (i > 0 && (isCurrentSeparator ^ isPrevSeparator || isCurrentDigit ^ isPrevDigit)) {
        String token = original.substring(start, i);
        tokens.add(buildToken(TokenType.of(isPrevSeparator), token, token, token, charTypes, useExceptions));

        start = i;
        charTypes = CharType.createSet();
      }

      isPrevSeparator = isCurrentSeparator;
      isPrevDigit = isCurrentDigit;
      charTypes.add(CharType.of(Character.toLowerCase(ch)));
    }

    String token = original.substring(start);
    tokens.add(buildToken(TokenType.of(isPrevSeparator), token, token, token, charTypes, useExceptions));

    return ImmutableList.copyOf(tokens);
  }

  List<Token> splitPossibleSubTokens(Token token) {
    CharType.Set charTypes = token.charTypes();

    if (charTypes.containsOnly(CharType.EN_OR_POSSIBLE_RU)) {
      return enOrPossibleRu(token);
    }

    if (charTypes.containsOnlyFirstOrBoth(CharType.SEPARATOR_OR_POSSIBLE_RU, CharType.EN_OR_POSSIBLE_RU)) {
      return separatorOrPossibleEn(token);
    }

    if (charTypes.containsOnly(CharType.RU_OR_POSSIBLE_EN)) {
      return ruOrPossibleEn(token);
    }

    if (charTypes.containsOnlyFirstOrBoth(CharType.RU_OR_POSSIBLE_SEPARATOR, CharType.RU_OR_POSSIBLE_EN)) {
      return ruOrPossibleSeparator(token);
    }

    return ImmutableList.of(token);
  }

  private List<Token> enOrPossibleRu(Token token) {
    String corrected = token.canonical();
    if (!langChecker.check(Lang.EN, token.canonical())) {
      String switched = Characters.switchLang(token.canonical(), Lang.RU);
      if (langChecker.check(Lang.RU, switched)) {
        corrected = switched;
      }
    }
    return ImmutableList.of(buildToken(token.type(), token.corrected(), token.canonical(), corrected));
  }

  private List<Token> separatorOrPossibleEn(Token token) {
    if (isAbbreviation(token.canonical())) {
      return splitBySpecificSeparators(token.canonical(), isSeparatorOrPossibleRu(), false);
    } else {
      String switched = Characters.switchLang(token.canonical(), Lang.RU);
      if (langChecker.check(Lang.RU, switched)) {
        return ImmutableList.of(buildToken(token.type(), token.corrected(), token.canonical(), switched));
      } else {
        return splitBySpecificSeparators(token.canonical(), isSeparatorOrPossibleRu(), true);
      }
    }
  }

  private List<Token> ruOrPossibleEn(Token token) {
    String corrected = token.canonical();
    if (!langChecker.check(Lang.RU, token.canonical())) {
      String switched = Characters.switchLang(token.canonical(), Lang.EN);
      if (langChecker.check(Lang.EN, switched)) {
        corrected = switched;
      }
    }
    return ImmutableList.of(buildToken(token.type(), token.corrected(), token.canonical(), corrected));
  }

  private List<Token> ruOrPossibleSeparator(Token token) {
    boolean correct = langChecker.check(Lang.RU, token.canonical());
    List<Token> splitByPossibleSeparators = ImmutableList.of();

    if (!correct) {
      splitByPossibleSeparators =
          splitBySpecificSeparators(Characters.switchLang(token.canonical(), Lang.EN), isSeparatorOrPossibleRu(), true);
      correct = !checkAll(splitByPossibleSeparators, Lang.EN);
    }

    if (correct) {
      return ImmutableList.of(buildToken(token.type(), token.corrected(), token.canonical(), token.canonical()));
    } else {
      return splitByPossibleSeparators;
    }
  }

  private String canonical(String candidate) {
    return candidate
        .replace(APOSTROPHE_1, APOSTROPHE)
        .toLowerCase();
  }

  private Token buildToken(
      TokenType tokenType,
      String original,
      String canonical,
      String corrected) {
    return buildToken(tokenType, original, canonical, corrected, CharType.createSet());
  }

  private Token buildToken(
      TokenType tokenType,
      String original,
      String canonical,
      String corrected,
      CharType.Set charTypes) {
    return buildToken(tokenType, original, canonical, corrected, charTypes, true);
  }

  private Token buildToken(
      TokenType tokenType,
      String original,
      String canonical,
      String corrected,
      CharType.Set charTypes,
      boolean useException) {

    return ImmutableToken.builder()
        .type(tokenType)
        .original(original)
        .canonical(canonical)
        .corrected(useException
            ? Objects.firstNonNull(exceptions.get(canonical), corrected)
            : corrected)
        .charTypes(charTypes)
        .build();
  }

  private boolean checkAll(Iterable<Token> tokens, Lang lang) {
    for (Token token : tokens) {
      if (!langChecker.check(lang, token.corrected())) {
        return false;
      }
    }
    return true;
  }

  private static final class ExceptionsLineProcessor implements LineProcessor<Map<String, String>> {
    static final Splitter SPLITTER = Splitter.on("|").trimResults();
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

    @Override
    public boolean processLine(String line) throws IOException {
      Iterator<String> iterator = SPLITTER.split(line).iterator();
      builder.put(iterator.next(), iterator.next());
      return true;
    }

    @Override
    public Map<String, String> getResult() {
      return builder.build();
    }
  }
}
