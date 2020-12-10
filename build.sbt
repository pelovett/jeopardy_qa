name := "jeopardy_qa"

version := "0.1"

libraryDependencies += "org.apache.lucene" % "lucene-core" % "8.0.0"
libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % "8.0.0"
libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % "8.0.0"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "4.0.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp"))

scalaVersion := "2.13.4"
