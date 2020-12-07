package engine

import org.apache.lucene.analysis.util.TokenizerFactory
import org.apache.lucene.search.similarities.ClassicSimilarity

import java.io.File
import java.util.Scanner
import scala.collection.mutable.ArrayBuffer

object JeopardyApp extends App {
  val index = new WikiIndex("/wiki_files/", "Stemmer")
  val questions = loadQuestions()


  var bm25_correct_at_10 = 0d
  var bm25_correct_at_1 = 0d
  val num_questions = questions.size.toDouble
  for (question <- questions){
    val correct_answers = question.getAnswer
    println("Question: "+question.question)
    println("Answer: "+correct_answers.mkString(" | "))
    val bm25_answers = index.runQuery(question)
    if (bm25_answers.size > 0 && correct_answers.contains(bm25_answers(0)))
      bm25_correct_at_1 += 1
    for (ans <- bm25_answers){
      println("  Retrieved: "+ans)
      if (correct_answers contains ans){
        bm25_correct_at_10 += 1
      }
    }
  }
  println("\nBM25 Final Accuracy@10: "+(bm25_correct_at_10 / num_questions * 100))
  println("BM25 Final Accuracy@1: "+(bm25_correct_at_1 / num_questions * 100))

  var tfidf_correct_at_10 = 0d
  var tfidf_correct_at_1 = 0d
  for (question <- questions){
    val correct_answers = question.getAnswer
    //println("Question: "+question.question)
    //println("Answer: "+correct_answers.mkString(" | "))
    val tfidf_answers = index.runQuery(question, new ClassicSimilarity())
    if (tfidf_answers.size > 0 && correct_answers.contains(tfidf_answers(0)))
      tfidf_correct_at_1 += 1
    for (ans <- tfidf_answers){
      //println("  Retrieved: "+ans)
      if (correct_answers contains ans){
        tfidf_correct_at_10 += 1
      }
    }
  }
  println("\nTFIDF Final Accuracy@10: "+(tfidf_correct_at_10 / num_questions * 100))
  println("TFIDF Final Accuracy@1: "+(tfidf_correct_at_1 / num_questions * 100))


  def loadQuestions(): ArrayBuffer[JeopardyQuestion] = {
    val results = new ArrayBuffer[JeopardyQuestion]()
    val file_stream = new Scanner(new File(getClass.getResource("/questions.txt").getPath))
    while (file_stream.hasNextLine){
      val category = file_stream.nextLine
      val question = file_stream.nextLine
      val answer = file_stream.nextLine
      file_stream.nextLine // Burn empty line
      results.addOne(new JeopardyQuestion(category, question, answer))
    }
    results
  }
}
