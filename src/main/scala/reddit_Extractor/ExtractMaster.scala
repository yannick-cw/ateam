package reddit_Extractor

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import cleaning_connection.CleanRequester
import elasticserach_API.ElasticSaveActor
import elasticserach_API.ElasticSaveActor.{ElasticError, ElasticResult, Saved, ServerError}
import elasticserach_API.Queries.CleanedDoc

/**
  * Created by yannick on 10.05.16.
  */
object ExtractMaster {
  val props = Props(new ExtractMaster)
}

class ExtractMaster extends Actor {
  val fileReader = context.actorOf(FileReader.props(self), "reader")
  val jsonExtractor = context.actorOf(JsonExtractor.props(self), "jsonExtractor")
  val cleanRequester = context.actorOf(CleanRequester.props(self)) //.withRouter(RoundRobinPool(16)), "cleanRequester")
  val elasticSaver = context.actorOf(ElasticSaveActor.props(self).withRouter(RoundRobinPool(16)), "elasticSaver")

  def receive = waiting(0,0, RawDoc("",0,""),1, 1)

  def waiting(fileCount: Int, rawDocCount: Int, mostUpvoted: RawDoc, elastiSaved: Int, cleaned: Int): Receive = {
    case dtr@DirToRead(_) => fileReader ! dtr
    case ip@InputString(_) =>
      jsonExtractor ! ip
//      println(s"files: $fileCount")
      context become waiting(fileCount + 1, rawDocCount, mostUpvoted,elastiSaved, cleaned)
    case rd@RawDoc(_,_,_) =>
      cleanRequester ! rd
//      println(rd)
//      println(s"docs: $rawDocCount")
//      println(rd)
      context become waiting(fileCount, rawDocCount + 1, if(mostUpvoted.up > rd.up) mostUpvoted else {println(s"most: $rd");rd},elastiSaved, cleaned)
    case cd@CleanedDoc(_,_,_,_) =>
      println(s"cleaned: $cleaned")
//      elasticSaver ! cd
      context become waiting(fileCount, rawDocCount, mostUpvoted, elastiSaved, cleaned + 1)
    case res: ElasticResult => res match {
      case Saved(cleanedDoc) => println(s"saved number: $elastiSaved")
        context become waiting(fileCount, rawDocCount, mostUpvoted, elastiSaved + 1, cleaned)
      case ElasticError(code, resp) => println(s"Errorcode: $code with $resp")
      case ServerError(ex) => println(ex)
    }
  }
}

object Test extends App {
  ActorSystem("test").actorOf(ExtractMaster.props) ! DirToRead("""/Users/Yannick/Google Drive/Uni/SS2016/Data_Science/republican 01-01-2011 10-05-2016""")
}
