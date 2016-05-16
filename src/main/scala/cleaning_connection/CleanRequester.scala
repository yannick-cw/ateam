package cleaning_connection

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import elasticserach_API.ElasticSaveActor.{ElasticError, Saved, ServerError}
import elasticserach_API.Queries.{CleanedDoc, CleanedText, RawText}
import reddit_Extractor.RawDoc
import util.{HttpRequester, Protocols}
import spray.json._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
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
      Thread.sleep(100)

      val request = RequestBuilding.Post("/clean",
        entity = HttpEntity(ContentTypes.`application/json`, RawText(rawText).toJson.prettyPrint))
      //todo settings
      val futureRes = futureHttpResponse(request, "0.0.0.0", 4321)

      futureRes.onSuccess {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          val cleanedText = Unmarshal(entity).to[CleanedText]
          cleanedText.onComplete {
            case Success(cleaned) => master ! CleanedDoc(src, up, rawText, cleaned.cleanedText)
            case Failure(e) => println(e.getMessage)
          }

        case HttpResponse(code , _, entity, _) => println(s"Errorcode: $code with $entity")
      }

      futureRes.onFailure {
        case ex => println(ex.getMessage)
      }

  }

}
