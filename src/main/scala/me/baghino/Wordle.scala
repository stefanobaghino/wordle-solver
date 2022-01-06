package me.baghino

import scala.collection.mutable
import scala.io.Source
import scala.util.Using

final class Wordle(
    dictionary: mutable.Set[String],
    fuzzyMatch: mutable.Set[Char],
    exactMatch: mutable.Map[Int, Char],
) {

  private val fullDictionary: Set[String] = dictionary.toSet
  private var currentGuess: Option[String] = None

  nextGuess()

  def reset(): Unit = {
    fuzzyMatch.clear()
    exactMatch.clear()
    dictionary.addAll(fullDictionary)
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
    currentGuess = if (fuzzyMatch.size + exactMatch.size >= 5) {
      educatedGuess().headOption
    } else {
      exploratoryGuess()
    }
    println(currentGuess.fold("I surrender.")(guess => s"My next guess is '$guess'."))
  }

  private def educatedGuess(): collection.Set[String] = {
    dictionary.filter(word =>
      exactMatch.forall { case (index, letter) =>
        word(index) == letter
      } && fuzzyMatch.forall(word.contains(_))
    )
  }

  private def exploratoryGuess(): Option[String] = {
    val weights = weigh(dictionary)
    val exclusion = exactMatch.values.toSet ++ fuzzyMatch
    dictionary
      .filterNot(_.exists(exclusion))
      .maxByOption(_.distinct.map(weights).sum)
  }

  private def weigh(dictionary: collection.Set[String]): Map[Char, Int] =
    dictionary.view.reduce(_ + _).groupMapReduce(identity)(_ => 1)(_ + _)

}

object Wordle {

  def loadAndCleanDictionary(path: String): Wordle =
    new Wordle(
      doLoadAndCleanDictionary(path),
      mutable.Set.empty,
      mutable.Map.empty,
    )

  private def doLoadAndCleanDictionary(path: String): mutable.Set[String] =
    Using(Source.fromFile(path))(
      _.getLines
        .collect {
          case word if valid(word) => word.toLowerCase
        }
        .to(mutable.Set)
    ).get

  private def valid(word: String): Boolean =
    word.length == 5 && word.forall(_.isLetter)

}
