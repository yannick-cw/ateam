package stream_flows

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import elasticserach_API.Queries.CleanedDoc
import org.scalatest.{MustMatchers, WordSpecLike}
import reddit_Extractor.ImportStream.RawDoc

import scala.concurrent.duration._
import scala.concurrent.Await

/**
  * Created by yannick on 18.05.16.
  */
class CleaningFlowSpec extends WordSpecLike with CleaningFlow with MustMatchers {
  implicit val sytem = ActorSystem()
  implicit val materializer = ActorMaterializer()


  "A CleaningFlow" must {
    "clean up a simple input" in {
      val input = RawDoc("Rep", 2,"darping derpIng      fucking hugging            what")
      val output = CleanedDoc("Rep", 2, input.text, "darp derp fuck hug")

      val src = Source(List(input))
        .via(stemming)
        .runWith(Sink.head)

      val result = Await.result(src, 100 millis)
      result must be(output)
    }

    "do not break with leading spaces" in {
      val input = RawDoc("Rep", 2,"  test what")
      val output = CleanedDoc("Rep", 2, input.text, "test")

      val src = Source(List(input))
        .via(stemming)
        .runWith(Sink.head)

      val result = Await.result(src, 100 millis)
      result must be(output)
    }

    "remove obvious stopwords" in {
      val input = RawDoc("Rep", 2,"am the this that when where not")
      val output = CleanedDoc("Rep", 2, input.text, "")

      val src = Source(List(input))
        .via(stemming)
        .runWith(Sink.head)

      val result = Await.result(src, 100 millis)
      result must be(output)
    }

    "stem to the same word" in {
      val input = RawDoc("Rep", 2,"stems stemming stemmed stem")
      val output = CleanedDoc("Rep", 2, input.text, "stem stem stem stem")

      val src = Source(List(input))
        .via(stemming)
        .runWith(Sink.head)

      val result = Await.result(src, 100 millis)
      result must be(output)
    }

    "stem a single s" in {
      val input = RawDoc("Rep", 2,"s")
      val output = CleanedDoc("Rep", 2, input.text, "s")

      val src = Source(List(input))
        .via(stemming)
        .runWith(Sink.head)

      val result = Await.result(src, 100 millis)
      result must be(output)
    }
    "stem a ls" in {
      val input = RawDoc("Rep", 2,"ls")
      val output = CleanedDoc("Rep", 2, input.text, "ls")

      val src = Source(List(input))
        .via(stemming)
        .runWith(Sink.head)

      val result = Await.result(src, 100 millis)
      result must be(output)
    }
  }
}
