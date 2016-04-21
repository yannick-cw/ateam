import twitter4j._
import twitter4j.conf.ConfigurationBuilder

object Util {
  var hashTagMap = Map.empty[Set[String], Int]
  var count = 0

  val config = new ConfigurationBuilder()
    .setOAuthConsumerKey("pc0lF2y9iyfTfvYR5Gg46LafM")
    .setOAuthConsumerSecret("MSiUGTYqcUyPdEUP7IEhcgj2IrlTpfhMVQbVju8DuwAgETZkhc")
    .setOAuthAccessToken("244498852-Z7M5NpbuPrp30PJe1MKDDbfcrI0qed5Qqd6GGehO")
    .setOAuthAccessTokenSecret("O2inePbGThLTOQLWG7yAjaEg9DgUMzLBV0R0uCIfXJGIi")
    .build

  def simpleStatusListener = new StatusListener() {
    def onStatus(status: Status) { workOnStatus(status) }
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
    def onException(ex: Exception) { ex.printStackTrace() }
    def onScrubGeo(arg0: Long, arg1: Long) {}
    def onStallWarning(warning: StallWarning) {}
  }

  def workOnStatus(status: Status): Unit =
    status.getHashtagEntities match {
    case hashTag if hashTag.size < 2 =>
    case hts =>
      val hashTags = hts.map(_.getText).toSet
      hashTagMap = hashTagMap.updated(hashTags, hashTagMap.getOrElse(hashTags, 1) + 1)
      count = count + 1
      if (count % 100 == 0) println(hashTagMap.toList.sortBy(_._2).reverse.filter(_._1.size > 1))
  }
}

object StatusStreamer {
  def main(args: Array[String]) {
    val twitterStream = new TwitterStreamFactory(Util.config).getInstance
    twitterStream.addListener(Util.simpleStatusListener)
//    val (l,r) = (Array(-126.562500,30.448674),Array(-61.171875,44.087585))
//    twitterStream.filter(new FilterQuery().locations(l,r))
    twitterStream.sample()
  }
}