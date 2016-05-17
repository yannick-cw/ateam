package elasticserach_API

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCode, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import elasticserach_API.ElasticSaveActor.{ElasticError, Saved, ServerError}
import elasticserach_API.Queries.CleanedDoc

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.duration._


/**
  * Created by Yannick on 16.05.16.
  */
object ElasticSaveActor {
  def props(master: ActorRef) = Props(new ElasticSaveActor(master))

  sealed trait ElasticResult

  case class Saved(cleanedDocs: List[CleanedDoc]) extends ElasticResult

  case class ElasticError(code: StatusCode, res: String) extends ElasticResult

  case class ServerError(ex: String) extends ElasticResult

}

class ElasticSaveActor(master: ActorRef) extends Actor with Requests {

  implicit val system = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val bufferSize: Int = 100

  def receive: Receive = bulkSaving(List.empty[CleanedDoc])

  def bulkSaving(buffer: List[CleanedDoc]): Receive = {
    case cleanedDoc: CleanedDoc =>
      val newBuffer = cleanedDoc +: buffer

      if (newBuffer.size == bufferSize) {

        val futureRes = bulkInsert(newBuffer)
        Await.ready(futureRes, 100 seconds)

        futureRes.onSuccess {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            //entity.dataBytes.runWith(Sink.head).map(_.utf8String).foreach(println)
            //needed for backpressure
            entity.dataBytes.runWith(Sink.ignore)
            master ! Saved(newBuffer)

          case HttpResponse(code, _, entity, _) =>
            val resString = entity.dataBytes.runWith(Sink.head).map(_.utf8String)
            master ! ElasticError(code, Await.result(resString, Duration.Inf))
        }

        futureRes.onFailure {
          case ex => master ! ServerError(ex.getMessage)
        }
        context become bulkSaving(List.empty[CleanedDoc])

      } else context become bulkSaving(newBuffer)
  }
}
