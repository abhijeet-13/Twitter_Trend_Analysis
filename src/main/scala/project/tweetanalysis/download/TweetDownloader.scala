package project.tweetanalysis.download


import java.io.FileWriter
import twitter4j.{ResponseList, Status, Twitter, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder
import scala.collection.JavaConversions._
import scala.io.Source

// Download initial data from the Internet
object TweetDownloader {

  val CONSUMER_KEY = ""
  val CONSUMER_SECRET = ""
  val ACCESS_TOKEN = ""
  val ACCESS_TOKEN_SECRET = ""

  // write to an external file
  def writeToFiles(tweetStatusList: ResponseList[Status], tweetUserEdgeFilePath: String) = {
    val tweetUserEdgeFileWriter = new FileWriter(tweetUserEdgeFilePath, true)
    tweetStatusList.foreach(status => {
      if (status.isRetweet) {
        val userId = status.getUser.getId
        val originalUserId = status.getRetweetedStatus.getUser.getId
        tweetUserEdgeFileWriter.write(String.format("%s, %s", originalUserId.toString, userId.toString))
        tweetUserEdgeFileWriter.write("\n")
      }
    })
    tweetUserEdgeFileWriter.close()
  }

  // entry point for module
  def main(args: Array[String]): Unit = {

    // usage
    if(args.length < 2){
      println("USAGE: <executable> <INPUT FILE PATH CONTAINING HARVARD DATA> <OUTPUT FILE PATH FOR THE GRAPH EDGES>")
    }

    val tweetIdFilePath = args(0)             // Input
    val tweetUserEdgeFilePath = args(1)       // Output

    // Create a twitter object
    val cb: ConfigurationBuilder = new ConfigurationBuilder
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(CONSUMER_KEY)
      .setOAuthConsumerSecret(CONSUMER_SECRET)
      .setOAuthAccessToken(ACCESS_TOKEN)
      .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET)
    val tf: TwitterFactory = new TwitterFactory(cb.build)
    val twitter: Twitter = tf.getInstance()

    // Wait for the rate limit to reset if the remaining lookups is < 900
    val lookupRateLimitStatus = twitter.getRateLimitStatus().get("/statuses/lookup")
    if (lookupRateLimitStatus.getRemaining != 900) {
      val waitTime = lookupRateLimitStatus.getSecondsUntilReset + 10
      println(String.format("Waiting for %s seconds to reset the rate limit...", waitTime.toString))
      (1 to waitTime).foreach(t => {
        print(String.format("\r%s   ", t.toString))
        Thread.sleep(1000)
      })
      println("\rStarting...")
    }


    // Read the tweet IDs from the input file
    var tweetIdList: List[Long] = List()
    val bufferedSource = Source.fromFile(tweetIdFilePath)
    println("Started reading from the file...")
    var tweetIdCount: Long = 0
    for (tweetId <- bufferedSource.getLines) {
      if (tweetIdCount % 1000 == 0) {
        println(String.format("-- %s tweets read", tweetIdCount.toString))
        // Check for the rate limit before making the twitter API call
        val lookupRateLimitStatus = twitter.getRateLimitStatus().get("/statuses/lookup")
        println(String.format("Remaining calls : %s", lookupRateLimitStatus.getRemaining.toString))
        if (lookupRateLimitStatus.getRemaining < 10) {
          val waitTime = lookupRateLimitStatus.getSecondsUntilReset + 60
          println(String.format("Waiting for %s seconds to reset the rate limit...", waitTime.toString))
          (1 to waitTime).foreach(t => {
            print(String.format("\r%s   ", t.toString))
            Thread.sleep(1000)
          })
          println("\rStarting again...")
        }
      }

      // Add the tweet ID to the list
      tweetIdList = tweetIdList.:+(tweetId.toLong)
      tweetIdCount += 1
      if (tweetIdCount % 100 == 0) {

        // Get the tweet status objects for all the tweet IDs in the tweetIdList
        // and write them to the output files
        var numberOfTries: Int = 10
        while (numberOfTries > 0) {
          try {
            val tweetStatusList = twitter.lookup(tweetIdList: _*)
            writeToFiles(tweetStatusList, tweetUserEdgeFilePath)
            numberOfTries = 0
          } catch {
            case exception: Throwable => println("Exception thrown while fetching tweets\n" + exception)
              val waitTime = 60
              println(String.format("Wait for %s seconds before trying again...", waitTime.toString))
              (1 to waitTime).foreach(t => {
                print(String.format("\r%s   ", t.toString))
                Thread.sleep(1000)
              })
              println("\rTrying again...")
              numberOfTries -= 1
          }
        }
        // Empty the list
        tweetIdList = List()
      }
    }
    bufferedSource.close
  }
}
