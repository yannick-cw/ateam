package stream_flows

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.scaladsl.{Flow, Sink, Source}
import elasticserach_API.Queries.CleanedDoc
import elasticserach_API.Requests

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait ElasticFlow extends Requests {
  val saveBulkToElastic = Flow[Seq[CleanedDoc]].map { docs =>
    val futureRes = bulkInsert(docs)
    Await.ready(futureRes, 2000 seconds)
    import scala.concurrent.ExecutionContext.Implicits.global

    futureRes.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        //entity.dataBytes.runWith(Sink.head).map(_.utf8String).foreach(println)

        //needed for backpressure
        //todo maybe filter not 201 jsons from entity
        entity.dataBytes.runWith(Sink.ignore)
        println("saved one more to elasti")
        Future("saved bulk successful")

      case HttpResponse(code, _, entity, _) =>
        import scala.concurrent.ExecutionContext.Implicits.global
        entity.dataBytes.runWith(Sink.head).map(_.utf8String)
    }
  }
}
