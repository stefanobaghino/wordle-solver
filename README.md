### Wordle Solver

Solver for [Wordle](https://www.powerlanguage.co.uk/wordle/).

#### How to use it

The easiest way is to run `sbt console`.

When in the Scala shell, first `import me.baghino.Wordle`.

Initialize a new game by loading a dictionary file, as in the following example:

```
val wordle = Wordle.loadAndCleanDictionary("/usr/share/dict/british-english")
```

The dictionary file is a list of words which in Linux systems is commonly found under `/usr/share/dict`. The
dictionary file is automatically cleaned for you (i.e. only 5-letter words are kept).

The constructor itself prints the first guess. Input the guess on the site and feed the information from the site back
into the program with the `feedback` method, which asks for a few optional inputs: letters that have been ruled out
("doesNotMatch"), letters that are in but at an unknown position ("fuzzyMatch") and letters which are already known to
be in the right spot ("at0" through "at4").

The following is a session I played in the past:

```
scala> import me.baghino.Wordle
import me.baghino.Wordle

scala> val wordle = Wordle.loadAndCleanDictionary("/usr/share/dict/british-english")
My next guess is 'arose'.
val wordle: me.baghino.Wordle = me.baghino.Wordle@5d7e4ab5

scala> wordle.feedback(doesNotMatch = "aos", fuzzyMatch = "re")
My next guess is 'until'.

scala> wordle.feedback(doesNotMatch = "unl", fuzzyMatch = "ti")
My next guess is 'pygmy'.

scala> wordle.feedback(doesNotMatch = "pymy", at2 = 'g')
My next guess is 'tiger'.
```

And voila: "tiger" is indeed the correct answer.

If you want to preserve the dictionary but start over, invoke the `reset` method.

#### How it works

Until we have five matching letters that we can use to make an educated guess, we make a few exploratory guesses, aimed
at cutting words from the dictionary based on letters that we know are not in the word. It also tries to maximize the
amount of information it gets at every round of exploratory guesses by explicitly removing words with known letters.
When we have five matching letters we try to make educated guesses. Luckily it doesn't look like there's a high number
of anagrams for any given set of letters, so this should be safe enough to give the right answer by turn 4.