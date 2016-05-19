package reddit_Extractor

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import elasticserach_API.Requests
import reddit_Extractor.ImportStream.InputFiles
import stream_flows.{CleaningFlow, ElasticFlow, JsonExtractFlow}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by yannick on 10.05.16.
  */
object ImportStream {
  val props = Props(new ImportStream())

  case class RawDoc(party: String, up: Int, text: String)

  case class InputFiles(files: List[String])

}

class ImportStream extends Actor with Requests with CleaningFlow with ElasticFlow with JsonExtractFlow {
  val fileReader = context.actorOf(FileReader.props(self), "reader")
  implicit val system = context.system
  //todo fix
  val decider: Supervision.Decider = { case _: StringIndexOutOfBoundsException => Supervision.Resume }
  val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))
  val elasticBulk: Int = 100


  def receive: Receive = {
    case dtr@DirToRead(_) => fileReader ! dtr
    case in: InputFiles =>
      val res = Source(in.files)
        .via(jsonExtraction)
        .via(cleaning)
        .grouped(elasticBulk)
        .via(insertBulkElastic)
        .runWith(Sink.ignore)(materializer)

      import scala.concurrent.ExecutionContext.Implicits.global
      res.onFailure{case ex => ex.printStackTrace()}
  }
}


object Test extends App {
  ActorSystem("test").actorOf(Props(new ImportStream())) ! DirToRead("""/home/yannick/Desktop/testCrawl/subredditarchive/republican 01-01-2011 10-05-2016""")
}
