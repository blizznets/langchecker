# LangChecker
 
LangChecker is the implementation in Java programming language of well known approach to determine wrong keyboard layout.
Supported languages are __Russian__ and __English__.
Used approach, called [n-gram](http://en.wikipedia.org/wiki/N-gram), based on vocabularies with nonexistent combination of letters.
Algorithm works as good as carefully vocabularies were built (test results with accuracy of algorithm you can find below at Tests section).

LangChecker implemented as tokenizer.
Why? because some letters in Russian layout are separators in English layout (for example: _ыендубьгышс --> style,music_, _cj,snbt --> событие_).
LangChecker able to check not only single word, but phrase.

Implementation has dependency on [Immutables.org](http://immutables.org/).

## Usage

    Tokenizer tokenizer = LangSwitcherTokenizer.create();
    System.out.println(tokenizer.tokenize("hello word руддщ цщкв"));
    System.out.println(tokenizer.tokenize("примет мир ghbdtn vbh"));

Result of `tokenize(String input)` method is instance of `TokenizerResponse`.
It contains original phrase, corrected phrase and list of tokens(parts of the phrase that recognized as words).

## Tests

This test shows how good algorithm can detect wrong or correct words.
Vocabularies with 109582 english and 92453 russian words were used for tests.

|                | EN        | RU        |                                                          |
|----------------|-----------|-----------|----------------------------------------------------------|
| positive       | 99.89%    | 99.94%    | amount of correct words, that were recognized as correct |
| false negative |  0.11%    |  0.06%    | amount of correct words, thar were recognized as wrong   |
| negative       | 92.97%    | 95.91%    | amount of wrong words, that were recognized as wrong     |
| false positive |  7.03%    |  4.09%    | amount of wrong words, that were recognized as correct   |

_correct words_ - words from vocabulary, _wrong words_ - words from vocabulary in wrong keyboard layout

## Licence

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
