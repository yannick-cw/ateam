package reddit_Extractor

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import elasticserach_API.Requests
import reddit_Extractor.ImportStream.{InputFiles, _}
import stream_flows.{CleaningFlow, ElasticFlow, JsonExtractFlow}

import scala.concurrent.Future

object ImportStream {
  val props = Props(new ImportStream())

  case class RawDoc(party: String, up: Int, text: String)

  case class InputFiles(files: List[String])

  val elasticBulkSize: Int = 100

}

class ImportStream extends Actor with Requests with CleaningFlow with ElasticFlow with JsonExtractFlow {
  val fileReader = context.actorOf(FileReader.props(self), "reader")
  implicit val system = context.system
  val decider: Supervision.Decider = {
    case _: StringIndexOutOfBoundsException => Supervision.Resume
    case any =>
      any.printStackTrace()
      Supervision.Stop
  }
  val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))


  def receive: Receive = {
    case dtr@DirToRead(_) => fileReader ! dtr
    case in: InputFiles =>
      val res: Future[Seq[Future[String]]] = Source(in.files)
        .via(jsonExtraction)
        .filterNot(_.text.trim.isEmpty)
        .via(stemming)
        .grouped(elasticBulkSize)
        .via(saveBulkToElastic)
        .runWith(Sink.seq)(materializer)

      import scala.concurrent.ExecutionContext.Implicits.global
      res.onFailure{ case ex => ex.printStackTrace() }

      val successResult = res.flatMap{ seq => Future.sequence(seq)}
      successResult.onComplete{ case a => println("finished")}
      successResult.onFailure{ case ex => ex.printStackTrace() }
  }
}


object Test extends App {
  ActorSystem("test").actorOf(Props(new ImportStream())) ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/liberal""")
}
