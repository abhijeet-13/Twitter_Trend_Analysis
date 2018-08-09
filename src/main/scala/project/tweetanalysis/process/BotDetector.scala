package project.tweetanalysis.process

import org.graphframes._
import org.apache.spark.sql.SparkSession
import scala.collection.JavaConversions._

// Detetc bots and clean influencers list
object BotDetector {

  def main(args : Array[String]):Unit = {
    if(args.length != 2){
      println("USAGE: <executable> <INPUT FILE PATH CONTAINING EDGES>  <OUTPUT FILE PATH CONTAINING RELEVANT INFLUENCER IDS>")
    }

    // Obtain spark session and read the edges from the HDFS
    val spark = SparkSession.builder().appName("TweetAnalysis").getOrCreate()
    val sc = spark.sparkContext
    val tweet_data = spark.read.csv(args(0))

    // Create the graph from the data
    val tweet_edges = tweet_data.withColumnRenamed("_c0", "dst").withColumnRenamed("_c1", "src")
    val tweet_vertices = tweet_edges.select("src").union(tweet_edges.select("dst")).distinct.withColumnRenamed("src", "id")
    val tweet_graph = GraphFrame(tweet_vertices, tweet_edges)

    println(s"The total number of nodes in graph ${tweet_vertices.count}")
    // tweet_graph.outDegrees.orderBy(desc("outDegree")).show


    // READ EXISTING INFLUENCER RDD
    val influencer_ids = sc.textFile(args(1))


    // PROCESS HERE

    val inDegrees = tweet_graph.inDegrees
    val outDegrees = tweet_graph.outDegrees

    val inOutDegreesJoin = inDegrees.join(outDegrees, inDegrees("id") === outDegrees("id"))


    import org.apache.spark.sql.functions._
    val bot_ids = inOutDegreesJoin.select("id", "inDegree", "outDegree").rdd.map(x => (x.get(0).asInstanceOf[String], x.get(2).asInstanceOf[String].toDouble / x.get(1).asInstanceOf[String].toDouble)).filter(_._2 > 1000).map{case(x, y) => x}


    // save the list to the HDFS
    val clean_influencer_ids_rdd = sc.parallelize(Seq(bot_ids))
    clean_influencer_ids_rdd.saveAsTextFile(args(2))

  }



}
