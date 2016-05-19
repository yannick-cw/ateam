package stream_flows

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.scaladsl.{Flow, Sink, Source}
import elasticserach_API.Queries.CleanedDoc
import elasticserach_API.Requests

import scala.concurrent.duration._
import scala.concurrent.Await

trait ElasticFlow extends Requests {
  val insertBulkElastic = Flow[Seq[CleanedDoc]].map { docs =>
    val futureRes = bulkInsert(docs)
    Await.ready(futureRes, 10 seconds)
    import scala.concurrent.ExecutionContext.Implicits.global

    futureRes.onSuccess {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        //entity.dataBytes.runWith(Sink.head).map(_.utf8String).foreach(println)
        //needed for backpressure
//        println("saved bulk successful")
        entity.dataBytes.runWith(Sink.ignore)

      case HttpResponse(code, _, entity, _) =>
        import scala.concurrent.ExecutionContext.Implicits.global
        val resString = entity.dataBytes.runWith(Sink.head).map(_.utf8String)
        println(resString)
    }
  }
}
