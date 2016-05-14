package util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future

/**
  * Created by yannick on 07.05.16.
  */
trait HttpRequester {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  //todo create settings
  lazy val connecFlow = Http().outgoingConnection("172.17.0.2", 9200)

  def futureHttpResponse(req: HttpRequest): Future[HttpResponse] = {
    Source.single(req)
      .via(connecFlow)
      .runWith(Sink.head)
  }
}
