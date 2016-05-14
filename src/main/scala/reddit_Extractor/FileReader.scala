package reddit_Extractor

import java.io.File

import akka.actor.{Actor, ActorRef, Props}

import scala.io.Source

/**
  * Created by yannick on 10.05.16.
  */
object FileReader {
  def props(master: ActorRef) = Props(new FileReader(master))
}

case class DirToRead(dir: String)

class FileReader(master: ActorRef) extends Actor {
  def receive: Receive = {
    case DirToRead(dir) =>
      val files = getFiles(dir).map{f =>
        val source = Source.fromFile(f)
        try source.mkString finally source.close()
      }
      files.foreach(master ! InputString(_))
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
