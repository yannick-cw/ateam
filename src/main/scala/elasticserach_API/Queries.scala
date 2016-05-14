package elasticserach_API

/**
  * Created by yannick on 14.05.16.
  */
object Queries {
  case class RawText(text: String)
  case class CleanedText(cleanedText: String)
  case class CleanedDoc(src: String, ups: Int, rawText: String, cleanedText: String)
}
