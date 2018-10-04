name := "nlp-workshop"
version := "0.1"
scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "net.sf.extjwnl" % "extjwnl" % "1.9.4",
  "net.sf.extjwnl" % "extjwnl-data-wn31" % "1.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1",
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)