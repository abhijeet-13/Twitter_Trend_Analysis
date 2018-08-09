package project.tweetanalysis.analyze

import org.apache.spark.sql.SparkSession
import twitter4j.{FilterQuery, StatusListener, TwitterFactory, TwitterStreamFactory}
import twitter4j.conf.ConfigurationBuilder
import java.util.HashMap
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}


// Listen to Tweets in real-time
class TweetStatusListener extends StatusListener{
  val topic = "tweetanalysis"
  private val kafkaBrokers = "35.224.49.130:9092,35.224.49.130:9093"
  val props = new HashMap[String, Object]()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  private val producer = new KafkaProducer[String, String](props)

  // every time a Tweet is read
  def onStatus(status: twitter4j.Status): Unit = {
    // send to Kafka cluster
    println(status.getText)
    producer.send(new ProducerRecord[String, String](topic, null, status.getText))
  }

  def onScrubGeo(x$1: scala.Long, x$2: scala.Long): Unit = {}

  def onDeletionNotice(x1: twitter4j.StatusDeletionNotice): Unit = {}

  def onStallWarning(x1: twitter4j.StallWarning): Unit = {}

  def onTrackLimitationNotice(x1: Int): Unit = {}

  def onException(x1: Exception): Unit = {}
}


// Entry point for module
object TweetListener {
  def main(args : Array[String]):Unit ={

    if(args.length < 1){
      println("USAGE: <executalbe> <INPUT FILE PATH CONTAINING INFLUENCER IDS>")
      return
    }

    val spark = SparkSession.builder().appName("TweetAnalysis").getOrCreate()
    val sc = spark.sparkContext

    // Obtain Twitter stream using the Twitter library
    val cb = new ConfigurationBuilder();
    cb.setOAuthConsumerKey("VPtgN7USgUklD3pWdFkOXwM3l")
      .setOAuthConsumerSecret("P5dziHUHGfLgK31lnIhMl0epcAYtsS2mZUz6KGeK8S67dNJHeF")
      .setOAuthAccessToken("4836763242-ebMUE1PzZwqJNkDhpdn9uXP4lkY3xQWiwQXwUl8")
      .setOAuthAccessTokenSecret("UK95geHtrwu1CwcFjwiEaeFuHL0gy8nGycGULOc44uyom")
    val conf = cb.build()
    val tf = new TwitterFactory(conf)
    val twitter = tf.getInstance()
    val tsf = new TwitterStreamFactory(conf)
    val tstream = tsf.getInstance
    var collected_data: String = ""
    tstream.addListener(new TweetStatusListener)

    // Filter the tstream
    val influencer_ids = sc.textFile(args(0)).collect().toArray.map(_.toLong).asInstanceOf[Array[Long]]
    val influencer_filter = new FilterQuery
    influencer_filter.language("en")
    influencer_filter.follow(influencer_ids: _*)
    tstream.filter(influencer_filter)



  }
}
