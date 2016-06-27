package reddit_Extractor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy, Supervision}
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
  implicit val system = context.system
  val decider: Supervision.Decider = {
    case _: StringIndexOutOfBoundsException => Supervision.Resume
    case any =>
      any.printStackTrace()
      Supervision.Stop
  }
  val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  val (actorSource, res) = Source.actorRef[String](100000, OverflowStrategy.dropHead)
    .via(jsonExtraction)
    .filterNot(_.text.trim.isEmpty)
    .via(stemming)
    .grouped(elasticBulkSize)
    .via(saveBulkToElastic)
    .toMat(Sink.seq)(Keep.both)
    .run()(materializer)

  val fileReader = context.actorOf(FileReader.props(actorSource), "reader")

  import scala.concurrent.ExecutionContext.Implicits.global
  res.onFailure{ case ex => ex.printStackTrace() }

  val successResult = res.flatMap{ seq => Future.sequence(seq)}
  successResult.onComplete{ case a => println("finished")}
  successResult.onFailure{ case ex => ex.printStackTrace() }



  def receive: Receive = {
    case dtr@DirToRead(_) => fileReader ! dtr
  }
}


object Test extends App {
  private val importer: ActorRef = ActorSystem("test").actorOf(Props(new ImportStream()))
//  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/liberal""")
//  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/republican""")
//  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/republicans""")
//  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/obama""")
//  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/donald""")
//  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/hillary""")
  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/conservative""")
  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/AskTrump""")
//  importer ! DirToRead("""/Users/437580/otherWS/poc/raw_ressources/SandersForPresident""")
}
