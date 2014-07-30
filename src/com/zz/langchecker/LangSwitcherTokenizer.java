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

// TODO check char types carefully (mixed lang case)
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
    List<Token> allTokens = split(input);
    List<String> wordTokens = FluentIterable.from(allTokens)
        .filter(TokenFunctions.isWord())
        .transform(TokenFunctions.corrected())
        .toList();
    String corrected = JOINER.join(Lists.transform(allTokens, TokenFunctions.corrected()));

    return ImmutableTokenizerResponse.builder()
        .original(input)
        .addAllTokens(wordTokens)
        .corrected(corrected.equals(canonical(input))
            ? Optional.<String>absent()
            : Optional.of(corrected))
        .build();
  }

  List<Token> split(String input) {
    List<Token> tokens = Lists.newArrayList();
    for (Token token : splitBySpecificSeparators(input, isSeparator())) {
      tokens.addAll(splitPossibleSubTokens(token));
    }
    return ImmutableList.copyOf(tokens);
  }

  List<Token> splitBySpecificSeparators(String original, Predicate<Character> isSeparator) {
    if (original.isEmpty()) {
      return ImmutableList.of();
    }

    List<Token> tokens = Lists.newArrayList();

    char[] chars = original.toCharArray();
    boolean isPrevSeparator = false;
    int start = 0;
    CharType.Set charTypes = CharType.createSet();

    for (int i = 0; i < chars.length; i++) {
      char ch = chars[i];

      boolean isCurrentSeparator = isSeparator.apply(ch);

      if (i > 0 && isCurrentSeparator ^ isPrevSeparator) {
        String token = original.substring(start, i);
        tokens.add(buildToken(TokenType.of(isPrevSeparator), token, token, token, charTypes));

        start = i;
        charTypes = CharType.createSet();
      }

      isPrevSeparator = isCurrentSeparator;
      charTypes.add(CharType.of(Character.toLowerCase(ch)));
    }

    String token = original.substring(start);
    tokens.add(buildToken(TokenType.of(isPrevSeparator), token, token, token, charTypes));

    return ImmutableList.copyOf(tokens);
  }

  List<Token> splitPossibleSubTokens(Token token) {
    String candidate = token.corrected();
    String canonical = token.canonical();
    CharType.Set charTypes = token.charTypes();
    TokenType tokenType = token.type();

    // check different lang groups

    if (charTypes.containsOnly(CharType.EN_OR_POSSIBLE_RU)) {
      String corrected = canonical;
      if (!langChecker.check(Lang.EN, canonical)) {
        String switched = Characters.switchLang(canonical, Lang.RU);
        if (langChecker.check(Lang.RU, switched)) {
          corrected = switched;
        }
      }
      return ImmutableList.of(buildToken(tokenType, candidate, canonical, corrected));
    }

    if (charTypes.containsOnlyFirstOrBoth(CharType.SEPARATOR_OR_POSSIBLE_RU, CharType.EN_OR_POSSIBLE_RU)) {
      String switched = Characters.switchLang(canonical, Lang.RU);
      if (langChecker.check(Lang.RU, switched)) {
        return ImmutableList.of(buildToken(tokenType, candidate, canonical, switched));
      } else {
        return splitBySpecificSeparators(canonical, isSeparatorOrPossibleRu());
      }
    }

    if (charTypes.containsOnly(CharType.RU_OR_POSSIBLE_EN)) {
      String corrected = canonical;
      if (!langChecker.check(Lang.RU, canonical)) {
        String switched = Characters.switchLang(canonical, Lang.EN);
        if (langChecker.check(Lang.EN, switched)) {
          corrected = switched;
        }
      }
      return ImmutableList.of(buildToken(tokenType, candidate, canonical, corrected));
    }

    if (charTypes.containsOnlyFirstOrBoth(CharType.RU_OR_POSSIBLE_SEPARATOR, CharType.RU_OR_POSSIBLE_EN)) {
      boolean correct = langChecker.check(Lang.RU, canonical);
      List<Token> splitByPossibleSeparators = ImmutableList.of();

      if (!correct) {
        splitByPossibleSeparators =
            splitBySpecificSeparators(Characters.switchLang(canonical, Lang.EN), isSeparatorOrPossibleRu());
        correct = !checkAll(splitByPossibleSeparators, Lang.EN);
      }

      if (correct) {
        return ImmutableList.of(buildToken(tokenType, candidate, canonical, canonical));
      } else if (!splitByPossibleSeparators.isEmpty()) {
        return splitByPossibleSeparators;
      }
    }

    return ImmutableList.of(token);
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

    return ImmutableToken.builder()
        .type(tokenType)
        .original(original)
        .canonical(canonical)
        .corrected(Objects.firstNonNull(exceptions.get(canonical), corrected))
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

  public static void main(String[] args) {
    Tokenizer tokenizer = LangSwitcherTokenizer.create();

    System.out.println(tokenizer.tokenize("eckeuf"));
    System.out.println();
  }
}
