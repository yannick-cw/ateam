package reddit_Extractor

import reddit_Extractor.ImportStream.RawDoc

import scala.io.Source

/**
  * Created by yannick on 10.05.16.
  */
object TestData {
  val basicInput = Source.fromFile(getClass.getResource("/standardFile.json").getPath).mkString

  val basicResult = List(RawDoc("Republican", 8, """There's nothing wrong with people interacting with each other.\n\nFor people on the right to imply there's something wrong with people interacting or having money involved with their politics is one of the most hypocrical things I've ever read.\n\nIt's laughable for anyone on the right to claim there's something wrong with this while the GOP just finished bringing in billions in foreign donations and funnelling them through the chamber of commerce."""),
  RawDoc("Republican", 2, """How dare they. Democrats are not supposed to talk to wealthy people. Oh, wait, these were not billionaires, so they are not doing the Republican's work. """),
  RawDoc("Republican", 2, """And whats wrong with this?"""),
  RawDoc("Republican", 1, """lol"""),
  RawDoc("Republican", 2, """Shame on her, how dare anyone try to cross classes"""),
  RawDoc("Republican", 1, """I fail to see the point of this post? """),
  RawDoc("Republican",0, "Valerie Jarrett reportedly took Barack and Michelle Obama under her wing, \\u201cintroduc[ing] them to a wealthier and better-connected Chicago than their own.\\\""))

  val bigFile = Source.fromFile(getClass.getResource("/bigFile.json").getPath).mkString
}