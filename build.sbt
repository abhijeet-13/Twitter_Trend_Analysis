name := "TweetAnalysisProject"

version := "0.1"

scalaVersion := "2.11.8"


// https://mvnrepository.com/artifact/org.twitter4j/twitter4j
libraryDependencies += "org.twitter4j" % "twitter4j" % "4.0.6"

// https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.6"

// https://mvnrepository.com/artifact/org.twitter4j/twitter4j-stream
libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "4.0.6"


resolvers ++= Seq(
  "apache-snapshots" at "http://repository.apache.org/snapshots/",
  "SparkPackages" at "https://dl.bintray.com/spark-packages/maven/"
)

val sparkVersion = "2.3.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
  "graphframes" % "graphframes" % "0.5.0-spark2.1-s_2.11"
)

libraryDependencies += "org.apache.kafka" % "kafka-streams" % "0.10.2.0"