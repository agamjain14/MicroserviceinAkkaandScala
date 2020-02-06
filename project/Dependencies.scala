import sbt.{ModuleID, _}


/**
  * Defines all the library dependencies.
  */
object Dependencies {


  private val scalaTest = "org.scalatest" %% "scalatest" % "3.0.7" % Test

  private val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"

  private val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"


  private object Akka {
    private val version = "2.5.26"

    private val akkaHttpVersion = "10.0.8"

    val actors: ModuleID = "com.typesafe.akka" %% "akka-actor" % version
    val cluster: ModuleID = "com.typesafe.akka" %% "akka-cluster" % version
    val clusterMetrics: ModuleID = "com.typesafe.akka" %% "akka-cluster-metrics" % version
    val remote: ModuleID = "com.typesafe.akka" %% "akka-remote" % version
    val slf4j: ModuleID = "com.typesafe.akka" %% "akka-slf4j" % version
    val testkit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % version
    val multiNodeKit: ModuleID = "com.typesafe.akka" %% "akka-multi-node-testkit" % version
    val clusterTools: ModuleID = "com.typesafe.akka" %% "akka-cluster-tools" % version
    val clusterSharding: ModuleID = "com.typesafe.akka" %% "akka-cluster-sharding" % version

    val akkaHTTP: ModuleID = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    val akkaStream: ModuleID = "com.typesafe.akka" %% "akka-stream" % version
    val akkaHTTPSprayJSON: ModuleID = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
    val akkaHTTPCore: ModuleID ="com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion

  }

  private object SprayJson {
    val sprayJson = "io.spray" %%  "spray-json" % "1.3.4"
  }

  private object HashCodeBuilder {
    val hashCodeBuilder = "org.apache.commons" % "commons-lang3" % "3.9"
  }

  private object Ficus {
    val ficus = "com.iheart" %% "ficus" % "1.3.4"
  }

  private object ReflectionApi {
    val reflection = "org.scala-lang" % "scala-reflect" % "2.12.6"
  }

  val csCommon = Seq(
    logback,
    Akka.actors,
    Akka.akkaHTTP,
    Akka.akkaStream,
    Akka.akkaHTTPSprayJSON,
    scalaTest,
    SprayJson.sprayJson,
    HashCodeBuilder.hashCodeBuilder
  )
  

  val csCoreServices = Seq(
    Akka.actors,
    Akka.akkaHTTP,
    Akka.remote,
    Akka.cluster,
    Akka.clusterMetrics,
    Akka.clusterTools,
    Akka.clusterSharding,
    Akka.multiNodeKit,
    scalaTest,
    Ficus.ficus,
    ReflectionApi.reflection
  )

}
