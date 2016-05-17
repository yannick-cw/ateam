package util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by yannick on 07.05.16.
  */
trait HttpRequester {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  def futureHttpResponse(req: HttpRequest, host: String, port: Int): Future[HttpResponse] = {

    import scala.concurrent.ExecutionContext.Implicits.global
    val connecFlow = Http().outgoingConnection(host, port)
    val pool = Http().cachedHostConnectionPool[Int](host, port)

    val simpleFlow = Source.single(req)
      .via(connecFlow)
      .runWith(Sink.head)


//    val poolVariant = Source.single(req -> 1)
//      .via(pool)
//      .runWith(Sink.head)
//      .flatMap{
//        case (Success(res), 1) => Future.successful(res)
//        case (Failure(f),1) => Future.failed(f)
//      }
//    poolVariant
    simpleFlow
  }
}
