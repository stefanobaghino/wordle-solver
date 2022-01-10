package me.baghino

import scala.collection.{mutable}
import scala.io.Source
import scala.util.Using

final class Wordle(
    dictionary: mutable.SortedSet[String],
    fuzzyMatch: mutable.Set[Char],
    exactMatch: mutable.Map[Int, Char],
) {

  private val fullDictionary: Set[String] = dictionary.toSet
  private var currentGuess: Option[String] = None
  private var remainingAttempts: Int = 6

  nextGuess()

  def reset(): Unit = {
    fuzzyMatch.clear()
    exactMatch.clear()
    dictionary.addAll(fullDictionary)
    remainingAttempts = 6
    nextGuess()
  }

  def feedback(
      doesNotMatch: String = "",
      fuzzyMatch: String = "",
      at0: Char = ' ',
      at1: Char = ' ',
      at2: Char = ' ',
      at3: Char = ' ',
      at4: Char = ' ',
  ): Unit = {
    if (at0.isLetter) exactMatch(0) = at0.toLower
    if (at1.isLetter) exactMatch(1) = at1.toLower
    if (at2.isLetter) exactMatch(2) = at2.toLower
    if (at3.isLetter) exactMatch(3) = at3.toLower
    if (at4.isLetter) exactMatch(4) = at4.toLower
    val doesNotMatchSet = doesNotMatch.toSet
    dictionary.filterInPlace(!_.exists(doesNotMatchSet))
    dictionary.filterInPlace(word =>
      exactMatch.forall { case (index, letter) =>
        word(index) == letter
      }
    )
    currentGuess.foreach(dictionary -= _)
    this.fuzzyMatch ++= fuzzyMatch.toSet
    this.fuzzyMatch --= exactMatch.values.toSet

    nextGuess()
  }

  private def nextGuess(): Unit = {
    if (remainingAttempts < 1) {
      println("No more attempts left.")
    } else {
      remainingAttempts -= 1
      val remainingWords = educatedGuess()
      currentGuess =
        if (remainingWords.size < remainingAttempts || fuzzyMatch.size + exactMatch.size >= 5) {
          remainingWords.headOption
        } else {
          exploratoryGuess()
        }
      print(currentGuess.fold("I surrender.")(guess => s"My next guess is '$guess'."))
      println(s" $remainingAttempts remaining attempts.")
    }
  }

  private def educatedGuess(): collection.Set[String] = {
    dictionary.filter(word =>
      exactMatch.forall { case (index, letter) =>
        word(index) == letter
      } && fuzzyMatch.forall(word.contains(_))
    )
  }

  private def exploratoryGuess(): Option[String] = {
    val exclusion = exactMatch.values.toSet ++ fuzzyMatch
    dictionary.find(!_.exists(exclusion)).orElse(dictionary.headOption)
  }

}

object Wordle {

  def loadAndCleanDictionary(path: String): Wordle =
    new Wordle(
      doLoadAndCleanDictionary(path),
      mutable.Set.empty,
      mutable.Map.empty,
    )

  private def doLoadAndCleanDictionary(path: String): mutable.SortedSet[String] =
    Using(Source.fromFile(path))(file => {
      val words =
        file.getLines.collect {
          case word if valid(word) => word.toLowerCase
        }.toSet
      val weights = words.view.reduce(_ + _).groupMapReduce(identity)(_ => 1)(_ + _)
      mutable.SortedSet.from(words)(Ordering.by(-_.distinct.map(weights).sum))
    }).get

  private def valid(word: String): Boolean =
    word.length == 5 && word.forall(_.isLetter)

}
