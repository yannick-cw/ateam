package elasticserach_API

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import elasticserach_API.Queries.CleanedDoc
import spray.json._
import util._

import scala.concurrent.Future
import scala.util.Failure

/**
  * Created by yannick on 07.05.16.
  */
trait Requests extends HttpRequester with Protocols {
  val (rep, dem) = ("rep", "dem")

  def bulkInsert(docs: Seq[CleanedDoc]): Future[HttpResponse] = {
    val querie = docs.map{ doc =>
      //todo
      val index = matchIndex(doc).get
      val docType = doc.src.toLowerCase
      s"""{ "index": { "_index": "$index", "_type": "$docType" }}\n${doc.toJson.compactPrint}\n"""
    }.mkString

    val request = RequestBuilding.Post(s"/_bulk",
      entity = HttpEntity(ContentTypes.`application/json`, querie))
    futureHttpResponse(request, "localhost", 9200)
  }

  private def matchIndex(cleanedDoc: CleanedDoc): Option[String] = {

    val subRedditToIndex = Map(
      "republicans" -> rep,
      "Republican" -> rep,
      "Conservative" -> rep,
      "democrats" -> dem,
      "Liberal" -> dem,
      "Democrat" -> dem,
      "SandersForPresident" -> dem
    )

    subRedditToIndex.get(cleanedDoc.src)
  }
}
