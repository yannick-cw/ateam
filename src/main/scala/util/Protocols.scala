package util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import elasticserach_API.Queries.{CleanedDoc, CleanedText, RawText}
import spray.json.DefaultJsonProtocol

trait Protocols extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val rawTextForm = jsonFormat1(RawText.apply)
  implicit val cleanTextForm = jsonFormat1(CleanedText.apply)
  implicit val cleanedDocForm = jsonFormat4(CleanedDoc.apply)
}
