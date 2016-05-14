package cleaning_connection

import akka.actor.{ActorSystem, Props}
import akka.http.javadsl.model.ResponseEntity
import akka.http.scaladsl.model._
import akka.testkit.{ImplicitSender, TestKit}
import elasticserach_API.Queries.CleanedDoc
import org.scalatest.{MustMatchers, WordSpecLike}
import reddit_Extractor.{RawDoc, StopSystemAfterAll}
import util.Protocols
import spray.json._
import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future

/**
  * Created by yannick on 14.05.16.
  */
class CleanRequesterSpec extends TestKit(ActorSystem())
with Protocols
with WordSpecLike
with MustMatchers
with ImplicitSender
with StopSystemAfterAll {

  "A CleanRequester" must {
    "response with an CleanendDoc to a RawDoc message" in {
      val msg = RawDoc("Republicans", 15, "this is old")
      val res = CleanedDoc("Republicans", 15, "this is old", "new text")

      val cleanReq = system.actorOf(Props(new CleanRequester(testActor){
        override def futureHttpResponse(req: HttpRequest, host: String, port: Int): Future[HttpResponse] = {
          Future(HttpResponse(200, entity = HttpEntity(ContentTypes.`application/json`, res.toJson.prettyPrint.getBytes())))
        }
      }))

      cleanReq ! msg
      expectMsg(res)

    }
  }

}