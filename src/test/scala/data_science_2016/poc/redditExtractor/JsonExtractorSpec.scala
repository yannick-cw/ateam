package data_science_2016.poc.redditExtractor

import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{MustMatchers, WordSpecLike}
import akka.actor.ActorSystem


/**
  * Created by yannick on 10.05.16.
  */
class JsonExtractorSpec extends TestKit(ActorSystem())
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {

  "The JsonExtractor" must {
    "extract the right groups from an example input String and return them as RawDocs" in {
      val extractor = system.actorOf(JsonExtractor.props(testActor))
      val msg =  InputString(TestData.basicInput)

      extractor ! msg
      expectMsg(RawDoc("Republican", 8, """There's nothing wrong with people interacting with each other.\n\nFor people on the right to imply there's something wrong with people interacting or having money involved with their politics is one of the most hypocrical things I've ever read.\n\nIt's laughable for anyone on the right to claim there's something wrong with this while the GOP just finished bringing in billions in foreign donations and funnelling them through the chamber of commerce."""))
      expectMsg(RawDoc("Republican", 2, """How dare they. Democrats are not supposed to talk to wealthy people. Oh, wait, these were not billionaires, so they are not doing the Republican's work. """))
      expectMsg(RawDoc("Republican", 2, """And whats wrong with this?"""))
      expectMsg(RawDoc("Republican", 1, """lol"""))
      expectMsg(RawDoc("Republican", 2, """Shame on her, how dare anyone try to cross classes"""))
      expectMsg(RawDoc("Republican", 1, """I fail to see the point of this post? """))
      expectMsg(RawDoc("Republican",0, "Valerie Jarrett reportedly took Barack and Michelle Obama under her wing, \\u201cintroduc[ing] them to a wealthier and better-connected Chicago than their own.\\\""))
    }

    "extract all docs from a bigger json" in {
      val extractor = system.actorOf(JsonExtractor.props(testActor))
      val msg =  InputString(TestData.bigFile)

      extractor ! msg
      receiveN(117)
    }
  }

}
