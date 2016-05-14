package util

import elasticserach_API.Queries.{CleanedDoc, CleanedText, RawText}
import spray.json.DefaultJsonProtocol

trait Protocols extends DefaultJsonProtocol {
  implicit val rawTextForm = jsonFormat1(RawText.apply)
  implicit val cleanTextForm = jsonFormat1(CleanedText.apply)
  implicit val cleanedDocForm = jsonFormat4(CleanedDoc.apply)
}
