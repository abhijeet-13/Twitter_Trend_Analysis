---------------------
Compile instructions:
---------------------

1. The project is an intelliJ IDEA project. Load the project in the IDE and run the task 'package' or 'assembly' to build the executable jar.


-----------------
Run instructions:
-----------------

1. Running the Tweet Downloader from the input data to find the edges based on the retweet pattern:

	> scala -classpath <JAR> project.tweetanalysis.download.TweetDownloader <input file path containing tweet IDs> <HDFS output file path>


2. Detecting the influencers from the retweet-patterns by running PageRank on the input graph:

	> spark-submit --class project.tweetanalysis.process.InfluencerDetector TweetAnalysisProject-assembly-0.1.jar <Input_File_Containing_graph_edges> <output file containing influencer ids>


3. Start the Zookeeper and the Kafka services on your local cluster.


4. Start the ElasticSearch, the Kibana and the LogStash services.


5. For trend-analysis, run the Twitter Streaming module as well as the topic-modelling module as separate processes:

	> spark-submit --class project.tweetanalysis.analyze.TweetListener TweetAnalysisProject-assembly-0.1.jar <Input_File_Containing_Influencer_ids>
	
	> spark-submit --class project.tweetanalysis.analyze.TopicAnalyzer TweetAnalysisProject-assembly-0.1.jar 200
	
	
6. View the results on the following Kibana topics -> ["output_topic_0", "output_topic_1", "output_topic_2"]

-->
