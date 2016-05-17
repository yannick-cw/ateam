package reddit_Extractor

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import cleaning.CleanActor
import elasticserach_API.ElasticSaveActor
import elasticserach_API.ElasticSaveActor.{ElasticError, ElasticResult, Saved, ServerError}
import elasticserach_API.Queries.CleanedDoc

object ExtractMaster {
  val props = Props(new ExtractMaster)
}

class ExtractMaster extends Actor {
  val fileReader = context.actorOf(FileReader.props(self), "reader")
  val jsonExtractor = context.actorOf(JsonExtractor.props(self), "jsonExtractor")
  val cleaner = context.actorOf(CleanActor.props.withRouter(RoundRobinPool(8)),"cleaner")
  val elasticSaver = context.actorOf(ElasticSaveActor.props(self))

  def receive = waiting(0,0, 1, 1)

  def waiting(fileCount: Int, rawDocCount: Int, elastiSaved: Int, cleaned: Int): Receive = {
    case dtr@DirToRead(_) => fileReader ! dtr

    case ip@InputString(_) =>
      jsonExtractor ! ip
//      println(s"files: $fileCount")
      context become waiting(fileCount + 1, rawDocCount, elastiSaved, cleaned)

    case rd@RawDoc(_,_,_) =>
      cleaner ! rd
//      println(s"docs: $rawDocCount")
      context become waiting(fileCount, rawDocCount + 1, elastiSaved, cleaned)

    case cd@CleanedDoc(_,_,_,_) =>
//      println(s"cleaned: $cleaned")
      elasticSaver ! cd
      context become waiting(fileCount, rawDocCount, elastiSaved, cleaned + 1)

    case res: ElasticResult => res match {
      case Saved(cleanedDoc) => println(s"saved number: $elastiSaved")
        context become waiting(fileCount, rawDocCount, elastiSaved + 1, cleaned)
      case ElasticError(code, resp) => println(s"Errorcode: $code with $resp")
      case ServerError(ex) => println(ex)
    }
  }
}

object Test extends App {
  ActorSystem("test").actorOf(ExtractMaster.props) ! DirToRead("""/home/yannick/Desktop/testCrawl/subredditarchive/republican 01-01-2011 10-05-2016""")
}
