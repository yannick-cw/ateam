package elasticserach_API

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import elasticserach_API.Queries.CleanedDoc
import spray.json.{RootJsonFormat, _}
import util._

import scala.concurrent.Future

/**
  * Created by yannick on 07.05.16.
  */
trait Requests extends HttpRequester with Protocols {
  val (rep, dem) = ("republican", "democrat")

  def insert(cleanedDoc: CleanedDoc): Future[HttpResponse] = {
    val index = matchIndex(cleanedDoc)
    val docType = cleanedDoc.src.toLowerCase

    val request = RequestBuilding.Post(s"/$index/$docType/",
      entity = HttpEntity(ContentTypes.`application/json`, cleanedDoc.toJson.compactPrint))
    futureHttpResponse(request)
  }

  private def matchIndex(cleanedDoc: CleanedDoc): String = cleanedDoc.src match {
    case "Republicans" => rep
    case "Republican" => rep
    case "Democrats" => dem
    case "Democrat" => dem
  }
}
