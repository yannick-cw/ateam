package reddit_Extractor

import akka.actor.{Actor, ActorRef, Props}

import scala.util.Try

/**
  * Created by yannick on 10.05.16.
  */
case class RawDoc(party: String, up: Int, text: String)
case class InputString(json: String)

object JsonExtractor {
  def props(master: ActorRef) = Props(new JsonExtractor(master))
}

class JsonExtractor(master: ActorRef) extends Actor {
  val commentsMatcher = """"body": "(.*?[^\\])".*?"ups": (-?\d*)""".r
  val subredditMatcher = """"subreddit": "([a-zA-Z]*)"""".r
  val titleMatcher = """"title": "(.*?[^\\])".*?"ups": (\d*)""".r

  def receive: Receive = {
    case InputString(json) => extractRawDocs(json).foreach(master ! _)
  }

  def extractRawDocs(input: String): Seq[RawDoc] = {
    //todo make failsafe
    val subreddit = subredditMatcher.findFirstIn(input).get.split(":")(1).trim.replace("\"","")
    commentsMatcher.findAllIn(input).matchData.map{ m => RawDoc(subreddit, m.group(2).toInt, m.group(1))}.toSeq ++
      titleMatcher.findAllIn(input).matchData.map{ m => RawDoc(subreddit, m.group(2).toInt, m.group(1))}.toSeq
  }
}
