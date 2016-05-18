package stream_flows

import akka.stream.scaladsl.Flow
import reddit_Extractor.ImportStream.RawDoc

trait JsonExtractFlow {
  val jsonExtraction = Flow[String].map { str =>
    val commentsMatcher = """"body": "(.*?[^\\])".*?"ups": (-?\d*)""".r
    val subredditMatcher = """"subreddit": "([a-zA-Z]*)"""".r
    val titleMatcher = """"title": "(.*?[^\\])".*?"ups": (\d*)""".r


    def extractRawDocs(input: String): Option[List[RawDoc]] = {
      subredditMatcher.findFirstIn(input).map { subStr =>
        val subreddit = subStr.split(":")(1).trim.replace("\"", "")
        commentsMatcher.findAllIn(input).matchData.map { m => RawDoc(subreddit, m.group(2).toInt, m.group(1)) }.toList ++
          titleMatcher.findAllIn(input).matchData.map { m => RawDoc(subreddit, m.group(2).toInt, m.group(1)) }.toList
      }
    }
    extractRawDocs(str).getOrElse(List.empty[RawDoc])
  }
}
