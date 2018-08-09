package project.tweetanalysis.process

import org.graphframes._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession

// Detect influencers from edges data
object InfluencerDetector {

  def main(args : Array[String]):Unit = {
    if(args.length != 2){
      println("USAGE: <executable> <INPUT FILE PATH CONTAINING EDGES> <OUTPUT FILE PATH CONTAINING INFLUENCER IDS>")
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


    // Run Page-rank analysis and obtain influencers
    val pagerank_results = tweet_graph.pageRank.resetProbability(0.1).maxIter(10).run
    val influencer_ids = pagerank_results.vertices.orderBy(desc("pagerank")).limit(1000).orderBy(asc("pagerank")).take(700).map {
      _.get(0).asInstanceOf[String].toLong
    }


    // save the list to the HDFS
    val influencer_ids_rdd = sc.parallelize(influencer_ids)
    influencer_ids_rdd.saveAsTextFile(args(1))

  }



}
