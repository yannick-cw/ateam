package util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
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

    //todo lazy??
    val connecFlow = Http().outgoingConnection(host, port)
    val hostConnectionPool = Http().cachedHostConnectionPool[Int](host, port)

    import scala.concurrent.ExecutionContext.Implicits.global
    Source.single(req -> 1)
      .via(hostConnectionPool)
      .runWith(Sink.head)
      .flatMap{
        case (Success(res: HttpResponse), 1) => Future.successful(res)
        case (Failure(f),1) => Future.failed(f)
      }
  }
}
