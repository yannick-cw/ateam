package cleaning

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import elasticserach_API.Queries.CleanedDoc
import org.scalatest.{MustMatchers, WordSpecLike}
import reddit_Extractor.{RawDoc, StopSystemAfterAll}

class CleanActorSpec extends TestKit(ActorSystem("testSys"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {


  "CleanActor" must {
    "return string with stems of words of inputstring" in {
      val cleanActor = system.actorOf(CleanActor.props)

      cleanActor ! RawDoc("any",10, " darping derpIng      fucking hugging            killing")
      expectMsg(CleanedDoc("any", 10, " darping derpIng      fucking hugging            killing", "darp derp fuck hug kill"))
    }
  }
}
