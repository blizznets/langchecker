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

import org.immutables.annotation.GenerateFunction;
import org.immutables.annotation.GenerateImmutable;
import org.immutables.annotation.GeneratePredicate;

@GenerateImmutable
abstract class Token {
  public abstract TokenType type();

  public abstract String original();

  public abstract String canonical();

  @GenerateFunction
  public abstract String corrected();

  public abstract CharType.Set charTypes();

  @GeneratePredicate
  public boolean isWord() {
    return TokenType.WORD.equals(type());
  }

  @Override
  public String toString() {
    return type() + ":" + "'" + original() + "'";
  }
}
