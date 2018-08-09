========================================
Final Project (Tweet Influencer Analysis):

Submitted by:
-------------
Miller, Marissa [mam093220@utdallas.edu]
Gupta, Kartikey [kxg173430@utdallas.edu]
Singh, Abhijeet [axs175531@utdallas.edu]

========================================


-----------------------
Compiling Instructions:
-----------------------

1. Extract the zip-file and load the project in IntelliJ IDEA.

2. Run the sbt-task 'assembly' to build 'TweetAnalysisProject-assembly-0.1.jar'.


-----------------
Run instructions:
-----------------

Run the following commands in the given order to achieve results.

1. Downloader Module:
	> scala -classpath <JAR> project.tweetanalysis.download.TweetDownloader <input file path containing tweet IDs> <HDFS output file path>

2. Influencer Detector Module:
	> spark-submit --class project.tweetanalysis.process.InfluencerDetector TweetAnalysisProject-assembly-0.1.jar <Input_File_Containing_graph_edges> <output file containing influencer ids>

3. Start the Zookeeper and the Kafka services

4. Run the ElasticSearch, Kibana and LogStash services.

5. Tweet Analyser Module:
	> spark-submit --class project.tweetanalysis.analyze.TweetListener TweetAnalysisProject-assembly-0.1.jar <Input_File_Containing_Influencer_ids>
	> spark-submit --class project.tweetanalysis.analyze.TopicAnalyzer TweetAnalysisProject-assembly-0.1.jar 200
	
6. View the results data on the following Kibana topics -> ["output_topic_0", "output_topic_1", "output_topic_2"]

-->