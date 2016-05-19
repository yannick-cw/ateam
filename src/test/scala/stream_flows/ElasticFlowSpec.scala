package stream_flows

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import elasticserach_API.Queries.CleanedDoc
import org.scalatest.{MustMatchers, WordSpecLike}
import reddit_Extractor.ImportStream.RawDoc

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Yannick on 19.05.16.
  */
class ElasticFlowSpec extends WordSpecLike with MustMatchers {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  "An elasticFlow" must {
    "report back with saved bulk successful if server response is 200" in {

      val flowToTest = new ElasticFlow {
        override implicit val materializer: ActorMaterializer = materializer
        override implicit val system: ActorSystem = system

        override def futureHttpResponse(req: HttpRequest, host: String, port: Int): Future[HttpResponse] = {
          Future.successful(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,"ignore")))
        }
      }.saveBulkToElastic

      val bulkOne = Seq(CleanedDoc("Democrats", 22, "wuut", "yes"), CleanedDoc("Democrats", 13, "nope", "no"))
      val bulkTwo = Seq(CleanedDoc("Democrats", 22, "wuut", "yes"), CleanedDoc("Democrats", 13, "nope", "no"))

      val src = Source(List(bulkOne,bulkTwo))

      val futureRes = src.via(flowToTest).runWith(Sink.seq)
      val result = Await.result(futureRes, 1000 millis)
      result.foreach(_.foreach(_ must be("saved bulk successful")))
    }

    "report back with the entity if code is not 200" in {

      val flowToTest = new ElasticFlow {
        override implicit val materializer: ActorMaterializer = materializer
        override implicit val system: ActorSystem = system

        override def futureHttpResponse(req: HttpRequest, host: String, port: Int): Future[HttpResponse] = {
          Future.successful(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,"you fucked up"), status = StatusCodes.BadRequest))
        }
      }.saveBulkToElastic

      val bulkOne = Seq(CleanedDoc("Democrats", 22, "wuut", "yes"), CleanedDoc("Democrats", 13, "nope", "no"))

      val src = Source(List(bulkOne))

      val futureRes = src.via(flowToTest).runWith(Sink.seq)
      val result = Await.result(futureRes, 1000 millis)
      result.foreach(_.foreach(_ must be("you fucked up")))
    }

    "Let the future fail, if the s" in {

      val flowToTest = new ElasticFlow {
        override implicit val materializer: ActorMaterializer = materializer
        override implicit val system: ActorSystem = system

        override def futureHttpResponse(req: HttpRequest, host: String, port: Int): Future[HttpResponse] = {
          Future.failed(new IllegalArgumentException)
        }
      }.saveBulkToElastic

      val bulkOne = Seq(CleanedDoc("Democrats", 22, "wuut", "yes"), CleanedDoc("Democrats", 13, "nope", "no"))

      val src = Source(List(bulkOne))

      val futureRes = src.via(flowToTest).runWith(Sink.head)
      val res = Await.result(futureRes, 1 seconds)
      res.onFailure{
        case ex => ex.isInstanceOf[IllegalArgumentException]
      }
    }
  }
}
