package data_science_2016.poc.redditExtractor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by yannick on 10.05.16.
  */
class FileReaderSpec extends TestKit(ActorSystem())
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {

  "The FileReader" must {
    "read in all files in the give folder and return each as string" in {
      val msg = DirToRead(getClass.getResource("/testFolder").getPath)

      val fileReader = system.actorOf(FileReader.props(testActor))
      fileReader ! msg

      expectMsg(InputString("gude!\na\n"))
      expectMsg(InputString("lilalu\nschmu\n"))
      expectMsg(InputString("isAFile\n"))
    }
  }

  }
