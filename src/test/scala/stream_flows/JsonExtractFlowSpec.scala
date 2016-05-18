package stream_flows

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.{MustMatchers, WordSpecLike}
import reddit_Extractor.ImportStream.RawDoc
import reddit_Extractor.TestData

import scala.concurrent.duration._
import scala.concurrent.Await

class JsonExtractFlowSpec extends WordSpecLike with JsonExtractFlow with MustMatchers {
  implicit val sytem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  "A JsonExtractFlow" must {
    "return the right RawDocs for a simple input" in {
      val input = TestData.basicInput
      val src = Source(List(input))
        .via(jsonExtraction)
        .mapConcat(identity)
        .runWith(Sink.seq)
      val result = Await.result(src, 100 millis)
      result must be(TestData.basicResult)
    }

    "return an empty list, if the input is broken" in {
      val input = "this is not okay"
      val src = Source(List(input))
        .via(jsonExtraction)
        .mapConcat(identity)
        .runWith(Sink.seq)
      val result = Await.result(src, 100 millis)
      result must be(List.empty[RawDoc])
    }
  }

}
