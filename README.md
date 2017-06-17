Krunch
======

Parser/Combinator framework for Kotlin, inspired by [CakeParse](https://github.com/sargunster/cakeparse),
[Scala Parser Combinators](https://github.com/scala/scala-parser-combinators), and 
[Parsec](https://wiki.haskell.org/Parsec).

### Installation

Add this to your root `build.gradle`:

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

Add the following dependency:

    dependencies {
        compile 'com.github.ibelieve:krunch:-SNAPSHOT'
    }
   
See https://jitpack.io/ for more details.
    
### Features

 * No separate lexing step by default, allowing for context-sensitive tokens. A separate lexing step can be built, 
   if desired.
 * Offers simple infix methods for combinators, such as `and`, `or`, `then`, and `before`, as well as functions like
   `optional`, `oneOf`, `atLeast`, `some`, and `many`.
 * Create tokens from strings, characters, and regexes using `literal`, `match`, or `oneOf`.
 * Optionally skip whitespace to simplify parsers.
 * Easily map deeply nested `Pair` results from `and` to objects using `flatMap` in a type-safe manner. 
    
### Sample Parsers

There are two sample parsers: a basic CalculatorParser example (see the tutorial), and a full parser for 
[Ledger](http://ledger-cli.org) journal files.

### Tutorial

Let's build a simple infix calculator parser with support for operator precedence and nested expressions in parenthesis. 
Let's start by defining how it will work: an expression will consist of two or more terms separated by either the 
addition (+) or subtraction (-) operators, our lowest precedence operators. A term will consist of two or more 
factors separated by either multiplication (*) or division (/) operators, our next higher precedence operators. Finally,
a factor can either be a simple number or another expression wrapped in parenthesis.

To start, for building a parser without a separate lexer step, we create a singleton object extending `RegexParsers`:

```kotlin
object CalculatorParser : RegexParsers<Double>() {
    override val goal = // final parser, returning a double
}
``` 

Now, let's define our most basic token, the number:

```kotlin
val number = literal("[\\d-.]+".toRegex()) map { it.toDouble() }
```

This does three things:

 1. Grabs a given regex from the stream of input
 2. Uses an infix function to map the resulting regex match to a Double
 3. By default, `CharParsers`, from which `RegexParsers` inherits, will skip whitespace leading up to token. 
 
So now we can parse numbers surrounded by optional whitespace. This number token will be the basis for factors in a 
calculator expression. But a factor can also be a full expression wrapped in parenthesis. There's a special feature in 
Krunch for dealing with parsers in between literals, the `.between()` method:

```kotlin
val parens = expression.between('(', ')')
```

Except... this won't work because `expression` hasn't been defined yet. So we use a `ref` to refer to something that 
will be defined later, allowing us to create recursive parsers:

```kotlin
val expressionRef: Parser<CharReader, Double> = ref { expression }

val parens = expressionRef.between('(', ')')
```
 
Note how Kotlin requires us to state the type of `expressionRef` due to recursion. Ok, so now we can define our 
`factor` parser:

```kotlin
val factor = oneOf(number, parens)
```

Pretty simple, just a combinator choosing between one of two existing parsers. Now for the `term` parser:

```kotlin
val term = factor and some(oneOf('*', '/') and factor) map {
    it.second.fold(it.first) { left, (op, right) ->
        when (op) {
            '*' -> left * right
            '/' -> left / right
            else -> throw IllegalStateException("Unrecognized operator: ${it.first}")
        }
    }
}
```

This one has some neat features. What we're doing here is grabbing one factor and then one or more operator/factor pairs. 
So it will grab `1 * 2 / 3` from `1 * 2 / 3 - 4`, leaving `- 4` to be parsed by the next parser. This parser then takes 
the sequence of operator/factor pairs and folds them with the first factor using the actual mathematical operators to 
get the final `Double` result.

Now for the final `expression` parser:

```kotlin
val expression = term and some(oneOf('+', '-') and term) map {
    it.second.fold(it.first) { left, (op, right) ->
        when (op) {
            '+' -> left + right
            '-' -> left - right
            else -> throw IllegalStateException("Unrecognized operator: ${it.first}")
        }
    }
}
```

This is basically the same as the `term` parser, but using `term`s and the plus and minus operators. One final step: our
goal in parsing is to parse an expression, so we assign our `expression` parser to the overridden `goal` val:

```kotlin
override val goal = expression
```

Now we're done! The completed calculator parser should look like:

```kotlin
object CalculatorParser : RegexParsers<Double>() {
    val expressionRef: Parser<CharReader, Double> = ref { expression }

    val number = literal("[\\d-.]+".toRegex()) map { it.toDouble() }

    val parens = expressionRef.between('(', ')')
    val factor = oneOf(number, parens)

    val term = factor and some(oneOf('*', '/') and factor) map {
        it.second.fold(it.first) { left, (op, right) ->
            when (op) {
                '*' -> left * right
                '/' -> left / right
                else -> throw IllegalStateException("Unrecognized operator: ${it.first}")
            }
        }
    }

    val expression = term and some(oneOf('+', '-') and term) map {
        it.second.fold(it.first) { left, (op, right) ->
            when (op) {
                '+' -> left + right
                '-' -> left - right
                else -> throw IllegalStateException("Unrecognized operator: ${it.first}")
            }
        }
    }

    override val goal = expression
}
```

You can use it like this:

```kotlin
println(CalculatorParser.parse("10 * 3 - 1 * (3 + 2)")) // Prints 25
```


### License


> Copyright 2017 Michael Spencer <sonrisesoftware@gmail.com> (https://github.com/iBelieve)
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
>    http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
