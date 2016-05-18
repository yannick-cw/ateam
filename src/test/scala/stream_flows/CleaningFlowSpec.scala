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
      val output = CleanedDoc("Rep", 2, "darping derpIng      fucking hugging            what", "darp derp fuck hug what")

      val src = Source(List(input))
        .via(cleaning)
        .runWith(Sink.head)

      val result = Await.result(src, 100 millis)
      result must be(output)
    }
  }
}
