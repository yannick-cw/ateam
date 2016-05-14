package reddit_Extractor

import scala.io.Source

/**
  * Created by yannick on 10.05.16.
  */
object TestData {
  val basicInput = Source.fromFile(getClass.getResource("/standardFile.json").getPath).mkString
  val bigFile = Source.fromFile(getClass.getResource("/bigFile.json").getPath).mkString
}