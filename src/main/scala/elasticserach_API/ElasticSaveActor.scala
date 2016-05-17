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
  case class Saved(cleanedDoc: CleanedDoc) extends ElasticResult
  case class ElasticError(code: StatusCode, res: String) extends ElasticResult
  case class ServerError(ex: String) extends ElasticResult
}

class ElasticSaveActor(master: ActorRef) extends Actor with Requests {

  implicit val system = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  var docs = List.empty[CleanedDoc]

  def receive: Receive = {
    case cd@CleanedDoc(_, _, _, _) =>

      docs = cd +: docs

      if(docs.size == 100)
        {
          val futureRes = bulkInsert(docs)
          println("waiting")
          Await.ready(futureRes, 100 seconds)
          println("ready")
          futureRes.onSuccess {
            case HttpResponse(StatusCodes.OK, _, entity, _) =>
//                        entity.dataBytes.runWith(Sink.head).map(_.utf8String).foreach(println)
              //needed for backpressure
              entity.dataBytes.runWith(Sink.ignore)
              master ! Saved(cd)

            case HttpResponse(code , _, entity, _) =>
              val resString = entity.dataBytes.runWith(Sink.head).map(_.utf8String)
              master ! ElasticError(code, Await.result(resString, Duration.Inf))
          }

          futureRes.onFailure {
            case ex => master ! ServerError(ex.getMessage)
          }
          docs = List.empty[CleanedDoc]
        }

//      val futureRes: Future[HttpResponse] = insert(cd)

//      Await.ready(futureRes, 20 seconds)

  }

}
