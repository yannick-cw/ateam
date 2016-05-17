package elasticserach_API

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.stream.StreamTcpException
import akka.testkit.{ImplicitSender, TestKit}
import elasticserach_API.ElasticSaveActor.{ElasticError, Saved, ServerError}
import elasticserach_API.Queries.CleanedDoc
import org.scalatest.{MustMatchers, WordSpecLike}
import reddit_Extractor.StopSystemAfterAll
import util.Protocols
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Yannick on 16.05.16.
  */
class ElasticSaveActorSpec extends TestKit(ActorSystem())
  with Protocols
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {

  val msg = CleanedDoc("Republican", 15, "dagddsf", "dasd f fsa fds")

  "A ElasticSaveActor" must {
    "respond with the CleanedDoc if the save worked" in {
      val elasticActorWithMockedHttp = system.actorOf(Props(new ElasticSaveActor(testActor){
        override def insert(cleanedDoc: CleanedDoc): Future[HttpResponse] = {
          Future(HttpResponse(StatusCodes.Created, entity = HttpEntity(ContentTypes.`application/json`, msg.toJson.prettyPrint.getBytes())))
        }
      }))

      elasticActorWithMockedHttp ! msg
      expectMsg(Saved(msg))
    }

    "respond with the error code and entity, if the save failed" in {
      val resEntity: Strict = HttpEntity(ContentTypes.`application/json`, "Error".toJson.prettyPrint.getBytes())


      val elasticActorWithMockedHttp = system.actorOf(Props(new ElasticSaveActor(testActor){
        override def insert(cleanedDoc: CleanedDoc): Future[HttpResponse] = {
          Future(HttpResponse(StatusCodes.BadRequest, entity = resEntity))
        }
      }))

      elasticActorWithMockedHttp ! msg
      expectMsg(ElasticError(StatusCodes.BadRequest, """"Error""""))
    }

    "respond with the throwable if there was an exception" in {

      val elasticActorWithMockedHttp = system.actorOf(Props(new ElasticSaveActor(testActor){
        override def insert(cleanedDoc: CleanedDoc): Future[HttpResponse] = {
          Future(throw new StreamTcpException("ups"))
        }
      }))

      elasticActorWithMockedHttp ! msg
      expectMsg(ServerError("ups"))
    }

    "respond with the throwable if the source string could not be matched to a known source" in {
      val elasticActorWithMockedHttp = system.actorOf(ElasticSaveActor.props(testActor))
      val invalidMSg = CleanedDoc("NotExistingSrc", 10, "", "")

      elasticActorWithMockedHttp ! invalidMSg
      expectMsg(ServerError("Undefined source"))
    }
  }
}
