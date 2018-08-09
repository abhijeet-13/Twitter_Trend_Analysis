package project.tweetanalysis.analyze

import org.apache.kafka.clients.consumer._
import java.util.{Collections, HashMap, UUID}
import scala.collection.JavaConversions._
import org.apache.spark.sql.SparkSession
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.ml.feature.{CountVectorizer, RegexTokenizer, StopWordsRemover, Tokenizer}
import org.apache.spark.ml.clustering.LDA
import scala.collection.mutable.WrappedArray



// ANalyze the topics using batch processing
object TopicAnalyzer{
  def main(args : Array[String]): Unit ={

    if(args.length < 1){
      println("USAGE: <executalbe> <MINIMUM_TWEETS_FOR_LDA>")
      return
    }
    val MIN_TWEETS_FOR_LDA = args(0).toInt

    // obtain spark context
    val spark = SparkSession.builder().appName("TweetAnalysis").getOrCreate()
    import spark.implicits._
    val sc = spark.sparkContext

    // preprocessing pipeline
    val tokenizer = new  RegexTokenizer().setInputCol("tweets").setOutputCol("words").setMinTokenLength(5).setPattern("\\W")
    val stopWordsRemover = new StopWordsRemover().setInputCol("words").setOutputCol("clean_words")
    val count_vectorizer = new CountVectorizer().setInputCol("clean_words").setOutputCol("features")


    // Read in all the tweets from the Kafka cluster
    val kafkaBrokers = "35.224.49.130:9092,35.224.49.130:9093"

    // Obtain a Kafka consumer and subscribe to the "TweetAnalysis" topic
    val consumer_props = new HashMap[String, Object]()
    consumer_props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString);
    consumer_props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
    consumer_props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    consumer_props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    // consumer_props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    val KafkaConsumer = new KafkaConsumer[String, String](consumer_props)
    KafkaConsumer.subscribe(Collections.singletonList("tweetanalysis"))


    // Obtain a Kafka producer and bind to the "outputtopics" topic
    val producer_props = new HashMap[String, Object]()
    producer_props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
    producer_props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    producer_props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](producer_props)


    var lda_input_tweets = Seq("").toDF("tweets")
    var count_so_far = 0
    while(true){
      val read_records = KafkaConsumer.poll(10000)
      count_so_far += read_records.count()
      lda_input_tweets = sc.parallelize(read_records.map(_.value).toSeq).toDF("tweets").union(lda_input_tweets)

      println(s"Number of records in lda tweets is ${count_so_far}")


      if(count_so_far >= MIN_TWEETS_FOR_LDA){
        // perform lda on lda_input_tweets
        lda_input_tweets = lda_input_tweets.limit(MIN_TWEETS_FOR_LDA)
        count_so_far = MIN_TWEETS_FOR_LDA

        println("Clustering will begin")
        val lda_input = stopWordsRemover.transform(tokenizer.transform(lda_input_tweets))

        val cv_model = count_vectorizer.fit(lda_input)
        val tweets_cv = cv_model.transform(lda_input)
        val lda = new LDA().setK(3).setMaxIter(10).setFeaturesCol("features")
        val lda_model = lda.fit(tweets_cv)
        println("Clustering has ended")

        // Describe topics
        val v = lda_model.describeTopics(6).map(x => (x.get(1).asInstanceOf[WrappedArray[Int]]).map(cv_model.vocabulary(_))).toDF("value").collect
        v(0)(0).asInstanceOf[WrappedArray[String]].foreach( x =>
          producer.send(new ProducerRecord[String, String]("output_topic_0", null, x))
        )
        v(1)(0).asInstanceOf[WrappedArray[String]].foreach( x =>
          producer.send(new ProducerRecord[String, String]("output_topic_1", null, x))
        )
        v(2)(0).asInstanceOf[WrappedArray[String]].foreach( x =>
          producer.send(new ProducerRecord[String, String]("output_topic_2", null, x))
        )

        spark.sqlContext.clearCache()
        // v.show(false)
      }


    }

  }
}