package engine

class JeopardyQuestion (var raw_category: String, var question: String, var raw_answer: String){

  def getCategory(): String = {
    var clean_category = ""
    for (cha <- raw_category){
      if (cha.isUpper || cha == ' ')
        clean_category += cha
    }
    clean_category
  }

  def getAnswer(): Array[String] = {
    raw_answer.split('|')
  }
  override def toString: String = {
    raw_category+"\n"+question+"\n"+raw_answer+"\n"
  }
}
