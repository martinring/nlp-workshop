import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.{PartOfSpeechAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLPClient}
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation
import edu.stanford.nlp.trees.GrammaticalRelation
import net.sf.extjwnl.dictionary.Dictionary
import net.sf.extjwnl.data.{IndexWord, POS, PointerType, Synset}

import scala.collection.JavaConverters._
import scala.io.StdIn

/**
  * We try to detect insults which are either adjectives bound to noun phrases
  * (e.g. Martin is stupid) or nouns bound by copola words "Martin is a dingbat".
  */
object Stanford {

  /**
    * The wordnet dictionary. Here we use the default instance which is located
    * in the resources of the jar dependencies
    */
  val dictionary = Dictionary.getDefaultResourceInstance.getMorphologicalProcessor

  /**
    * A client to the stanford parses. The stanford parses must be started
    * locally on port 9000.
    *
    * This can be achieved by calling
    *
    * java -mx6g -cp "*" edu.stanford.nlp.pipeline.StanfordCoreNLPServer -port 9000 -timeout 15000
    *
    * in the CoreNLP root directory
    */
  val client = {
    val props = new Properties()
    // we want the parser to tokenize do part of speech tagging, sentence splitting,
    // lemmatization and dependency parsing.
    props.setProperty("annotators", "tokenize, pos, ssplit, lemma, parse")
    new StanfordCoreNLPClient(props, "http://localhost", 9000, 4)
  }

  /**
    * Determines if a word may be an insult
    * @param pos The part of speech
    * @param word The word itself
    * @param neg Whether the word is negated
    * @return bool indicating if a word might be an insult
    */
  def isInsult(pos: String, word: Synset, neg: Boolean): Boolean = pos match {
    case "NN" =>
      // if we look at a noun we determine if the noun is a hyponym of the
      // following words.
      val hypernyms = if (neg) Set("leader") else Set("unwelcome person","simpleton")
      hypernyms.exists(word.containsWord) ||
        word.getPointers(PointerType.HYPERNYM).asScala.exists(x => isInsult(pos,x.getTarget.getSynset,neg))
    case _ =>
      val similar = if (neg) "smart" else "stupid"
      // if the word is an adjective we determine if it is similar to "stupid"
      word.containsWord(similar) ||
        word.getPointers(PointerType.SIMILAR_TO).asScala.exists(x => x.getTargetSynset.containsWord(similar))
  }

  def main(args: Array[String]) = {
    // Our input is the infinite stream of lines from StdIn
    val input = Stream.continually(StdIn.readLine())

    // For every line of input we try to detect insuls
    input.foreach { line =>

      // We create a new annotation for the line
      val document = new Annotation(line)

      // and let the server annotate the document
      client.annotate(document)

      // We retrieve the sentences.
      val sentences = document.get(classOf[SentencesAnnotation]).asScala

      // And for every sentence, ...
      sentences.foreach { sentence =>
        // ... we look at the dependency graph.
        val dependencyGraph = sentence.get(classOf[EnhancedPlusPlusDependenciesAnnotation])
        // If there is a "nsubj" relation for which ...
        dependencyGraph.findAllRelns(GrammaticalRelation.valueOf("nsubj")).asScala.foreach { edge =>
          val source = edge.getSource
          val pos = sentence.get(classOf[TokensAnnotation]).get(source.index() - 1).get(classOf[PartOfSpeechAnnotation])
          val adj = Option(dictionary.lookupBaseForm(if (pos == "NN") POS.NOUN else POS.ADJECTIVE, source.lemma())).map(_.getSenses.asScala)
          // we determined if it is negated (by counting negations and determining if the number is odd) and
          val neg = dependencyGraph.findAllRelns(GrammaticalRelation.valueOf("neg"))
            .asScala
            .filter(edge => edge.getSource.index() == source.index())
            .size % 2 == 1
          adj.foreach { adj =>
            // ... the target is an insult. We print a contrary message about the source.
            if (adj.exists(isInsult(pos,_,neg))) {
              if (neg) {
                println("Yes he is!")
              } else {
                println(s"No, ${edge.getTarget.lemma()} is great!")
              }
            }
          }
        }
      }
    }
  }
}
