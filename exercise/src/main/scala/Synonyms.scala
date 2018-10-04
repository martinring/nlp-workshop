import net.sf.extjwnl.data.POS

import scala.io.StdIn
import net.sf.extjwnl.dictionary.Dictionary
import scala.collection.JavaConverters._

/**
  * First example from the workshop.
  *
  * We split sentences, tokenize and try to guess the part of speech.
  * Based on that we replace words with synonyms.
  */
object Synonyms {
  /**
    * Represents a sentence in a document
    * @param words The individual words of the sentence
    */
  case class Sentence(words: Seq[String]) {
    override def toString: String = words.mkString("[", " ", "]")
  }

  /**
    * The wordnet dictionary. Here we use the default instance which is located
    * in the resources of the jar dependencies
    */
  val dictionary = Dictionary.getDefaultResourceInstance()

  /**
    * The main entry point for the application
    */
  def main(args: Array[String]) = {
    // Our input is the infinite stream of lines from StdIn
    val input = Stream.continually(StdIn.readLine())

    val sentences = for {
      line <- input // for every line from input
      ssplit <- line.split("[.!?]+") // and every splitted sentence string from the line,
      sentence = Sentence(ssplit.trim.split("[^\\p{L}]+")) // we construct a sentence which consists of the individual words.
    } yield // we return a new sentence which has words replaced by synonyms
      Sentence(sentence.words.map { rawword =>
        val word = for {
          // We try to guess the POS by trial and error
          iw <- Option(dictionary.getMorphologicalProcessor().lookupBaseForm(POS.VERB, rawword)) orElse
                Option(dictionary.getMorphologicalProcessor().lookupBaseForm(POS.ADJECTIVE, rawword)) orElse
                Option(dictionary.getMorphologicalProcessor().lookupBaseForm(POS.ADVERB, rawword)) orElse
                Option(dictionary.getMorphologicalProcessor().lookupBaseForm(POS.NOUN, rawword))
          // if there is a word, we look for a synonym in the first synset.
          sense <- iw.getSenses.asScala.headOption
          synonym <- sense.getWords.asScala.find(w => w.getLemma != iw.getLemma)
        } yield synonym.getLemma
        // we either return the synonym or if we didnt find one, we return the original
        // word.
        word.getOrElse(rawword)
      })

    // now we print every modified sentence to StdOut
    sentences.foreach(println)
  }
}
