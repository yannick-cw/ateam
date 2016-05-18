package util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import elasticserach_API.Queries.CleanedDoc
import spray.json.DefaultJsonProtocol

trait Protocols extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val cleanedDocForm = jsonFormat4(CleanedDoc.apply)
}
