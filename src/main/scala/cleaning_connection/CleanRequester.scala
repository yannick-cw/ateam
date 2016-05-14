package cleaning_connection

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import elasticserach_API.Queries.{CleanedDoc, CleanedText, RawText}
import reddit_Extractor.RawDoc
import util.{HttpRequester, Protocols}
import spray.json._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}


/**
  * Created by yannick on 14.05.16.
  */
object CleanRequester {
  def props(master: ActorRef) = Props(new CleanRequester(master))
}

class CleanRequester(master: ActorRef) extends Actor with Protocols with HttpRequester {
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  def receive: Receive = {
    case RawDoc(src, up, rawText) =>
      val request = RequestBuilding.Post("clean",
        entity = HttpEntity(ContentTypes.`application/json`, RawText(rawText).toJson.prettyPrint))
      //todo settings
      val futureRes = futureHttpResponse(request, "localhost", 4321)

      futureRes.onComplete {
        case Success(res) =>
          val cleanedText = Unmarshal(res.entity).to[CleanedText]
          cleanedText.foreach { clean => master ! CleanedDoc(src, up, rawText, clean.cleanedText) }
        case Failure(ex) => println(ex.getMessage)
      }
  }

}
