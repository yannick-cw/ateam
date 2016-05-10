package redditExtractor

import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by yannick on 10.05.16.
  */
object ExtractMaster {
  val props = Props(new ExtractMaster)
}

class ExtractMaster extends Actor {
  val fileReader = context.actorOf(FileReader.props(self), "reader")
  val jsonExtractor = context.actorOf(JsonExtractor.props(self), "jsonExtractor")

  def receive = waiting(0,0)

  def waiting(fileCount: Int, rawDocCount: Int): Receive = {
    case dtr@DirToRead(_) => fileReader ! dtr
    case ip@InputString(_) =>
      jsonExtractor ! ip
      println(s"files: $fileCount")
      context become waiting(fileCount + 1, rawDocCount)
    case rd@RawDoc(_,_,_) =>
      println(rd)
      println(s"docs: $rawDocCount")
      context become waiting(fileCount, rawDocCount + 1)
  }
}

object Test extends App {
  ActorSystem("test").actorOf(ExtractMaster.props) ! DirToRead("""/home/yannick/Desktop/testCrawl/subredditarchive/republican 01-01-2011 10-05-2016""")
}
