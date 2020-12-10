package engine

import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.core.{LowerCaseFilterFactory, StopFilterFactory}
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.analysis.en.EnglishMinimalStemFilterFactory
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory
import org.apache.lucene.analysis.standard.{StandardAnalyzer, StandardTokenizer, StandardTokenizerFactory}

import java.io.File
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.{MultiFieldQueryParser, QueryParser}
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder
import org.apache.lucene.search.similarities.{BM25Similarity, ClassicSimilarity, Similarity}
import org.apache.lucene.search.{BooleanClause, BooleanQuery, IndexSearcher}
import org.apache.lucene.store.{Directory, FSDirectory, RAMDirectory}

import java.nio.file.Paths
import scala.collection.mutable.ArrayBuffer

class WikiIndex (val file_path: String, val analyzer_type: String){
  var analyzer = if (analyzer_type == "Standard"){
    CustomAnalyzer.builder()
      .withTokenizer("standard")
      .addTokenFilter("lowercase")
      .addTokenFilter("stop")
      .build()
  } else if (analyzer_type == "Lemma"){
    new LemmatizingAnalyzer()
  } else {
    CustomAnalyzer.builder()
      .withTokenizer("standard")
      .addTokenFilter("lowercase")
      .addTokenFilter("stop")
      .addTokenFilter(classOf[SnowballPorterFilterFactory])
      .build()
  }

  val directory = FSDirectory.open(Paths.get("index.lucene"))
  if (DirectoryReader.indexExists(directory) == false) {
    val index_writer_config = new IndexWriterConfig(analyzer)
    val index_writer = new IndexWriter(directory, index_writer_config)

    val parser = new WikiParser()
    val resource_path = getClass.getResource(file_path)
    val wiki_directory = new File(resource_path.getPath)
    if (wiki_directory.exists && wiki_directory.isDirectory) {
      for (wiki_file <- wiki_directory.listFiles.toList) {
        print("Indexing: " + wiki_file.toString.split("/")(11))
        val start_time = System.currentTimeMillis
        val parsed_docs = parser.parseRawWikiFile(wiki_file)
        for (doc <- parsed_docs) {
          index_writer.addDocument(doc)
        }
        index_writer.commit
        print(" | "+(((System.currentTimeMillis - start_time) / 1000).toString)+" sec\n")
      }
    }
  }

  def cleanQuestion(question: String): String = {
    question.replaceAll("[:!&;-]", " ")
  }

  def runQuery(query: JeopardyQuestion, sim: Similarity = new BM25Similarity()): ArrayBuffer[String] = {
    val result = new ArrayBuffer[String]
    val reader = DirectoryReader.open(directory)
    val question_parser = new QueryParser("body", analyzer)
    val category_parser = new QueryParser("categories", analyzer)
    val query_builder = new BooleanQuery.Builder
    query_builder.add(question_parser.parse(cleanQuestion(query.question)), BooleanClause.Occur.SHOULD)
    query_builder.add(category_parser.parse(query.getCategory), BooleanClause.Occur.SHOULD)
    val final_query = query_builder.build

    val searcher = new IndexSearcher(reader)
    searcher.setSimilarity(sim)
    val top_docs = searcher.search(final_query, 10)
    for (score_doc <- top_docs.scoreDocs){
      val doc = searcher.doc(score_doc.doc)
      result.append(doc.get("title"))
    }
    result
  }
}
