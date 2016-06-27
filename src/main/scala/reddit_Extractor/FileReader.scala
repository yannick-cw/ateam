package reddit_Extractor

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import reddit_Extractor.ImportStream.InputFiles

import scala.io.Source

/**
  * Created by yannick on 10.05.16.
  */
object FileReader {
  def props(master: ActorRef) = Props(new FileReader(master))
}

case class DirToRead(dir: String)

class FileReader(actorSource: ActorRef) extends Actor {
  def receive: Receive = {
    case DirToRead(dir) =>
        getFiles(dir).foreach{f =>
//        println(s"currently at file $index of ${allFiles.size} total files.")
        val source = Source.fromFile(f)
          val text: String = try source.mkString finally source.close()
          actorSource ! text
      }
  }

  def getFiles(dir: String): List[File] = {
    val directory = new File(dir)
    if (directory.exists && directory.isDirectory) {
      directory.listFiles.filter(_.isFile).filter(_.getName.contains(".json")).toList
    } else {
      List.empty[File]
    }
  }
}
