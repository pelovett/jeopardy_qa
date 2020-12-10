package engine

import java.io.{File, FileWriter}
import org.apache.lucene.document.{Document, Field, StringField, TextField}

import java.util.Scanner
import scala.collection.mutable.ArrayBuffer

class WikiParser {

  def parseRawWikiFile(wiki_file: File): ArrayBuffer[Document] = {
    val doc_list = new ArrayBuffer[Document]()
    val file_stream = new Scanner(wiki_file)
    var cur_line = ""
    var cur_doc_title = ""
    var cur_doc_body = ""
    var cur_doc_categories = ""
    while (file_stream.hasNext){
      cur_line = file_stream.nextLine()
      if (cur_line != "") {
        if (cur_line.startsWith("[[") && cur_line.startsWith("[[File:") == false) {
          if (cur_doc_title.size > 0){
            doc_list.append(createDoc(cur_doc_title, cur_doc_categories, cur_doc_body))
            cur_doc_body = ""
          }
          cur_doc_title = cur_line.substring(2, cur_line.length - 2)
        } else if (cur_line.startsWith("CATEGORIES")){
          cur_doc_categories = cur_line.substring(12)
          // For now we'll skip the redirect pages | Maybe create alternate titles in the future?
        } else if (cur_line.startsWith("==") == false &&
          cur_line.startsWith(" ") == false &&
          cur_line.startsWith("<") == false &&
          cur_line.startsWith("}}") == false &&
          cur_line.startsWith("|") == false &&
          cur_line.startsWith("#") == false){
          cur_doc_body += removeTPL(cur_line)
        }
      }
    }
    file_stream.close
    doc_list
  }

  def createDoc(title: String, categories: String, body: String): Document ={
    val doc = new Document
    doc.add(new StringField("title", title, Field.Store.YES))
    doc.add(new TextField("categories", categories, Field.Store.YES))
    doc.add(new TextField("body", body, Field.Store.NO))
    doc
  }

  def removeTPL(raw_text: String): String = {
    var output_text = ""
    var inside_tpl = false
    var inside_http = false
    var nesting_level = 0
    for (i <- 0 to raw_text.size-1) {
      val char = raw_text.charAt(i)
      if (i+4 < raw_text.size && raw_text.substring(i, i+4) == "[tpl") {
        nesting_level += 1
        inside_tpl = true
      }
      if (inside_tpl == false && i+4 < raw_text.size){
        if (raw_text.substring(i, i+4) == "http")
          inside_http = true
      }
      if (inside_tpl == false && inside_http == false)
        output_text += char

      if (inside_tpl && i > 5){
        if (raw_text.substring(i-6, i-1) == "/tpl]") {
          nesting_level -= 1
          if (nesting_level == 0)
            inside_tpl = false
        }
      }
      if (inside_http && char == ' ')
        inside_http = false
    }
    output_text
  }
}
