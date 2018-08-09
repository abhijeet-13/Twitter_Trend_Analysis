# Twitter Trend Analysis
[Influencer detection on Twitter and real-time current trend analysis using twitter4j and Spark]

This work was done as part of the final project in a course on Big-Data Analytics at the University of Texas at Dallas, by the following contributors:
- Abhijeet Singh (Email: axs175531 [AT] utdallas.edu)
- Kartikey Gupta (Email: kxg173430 [AT] utdallas.edu)
- Marissa Miller


Influencer Detection: Influencers are detected by running a PageRank algorithm on a graph representing the retweet patterns for the tweets in the dataset. Bots are filtered by analyzing the graph for motif patterns like high out degree and cliques.

Trend Analysis: Twitter Streaming APIs are used to obtain real-time tweets from the influencers and LDA is performed for topic-modeling, and the results sent to a Kafka cluster to be used by Kibana for visualization.


For more details, please refer to the project wiki.
-->
