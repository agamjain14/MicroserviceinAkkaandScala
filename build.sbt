
lazy val baseSettings = Seq(
  organization := "net.cs",
  scalaVersion := "2.12.7",
  parallelExecution in Test := false,
  sources in (Compile,doc) := Nil,
  resolvers ++= Seq(
    Resolver.typesafeRepo("releases"),
    Resolver.typesafeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"))
)

lazy val commonSettings = baseSettings

lazy val csCommon = (project in file("cs-common"))
  .settings(commonSettings, libraryDependencies ++= Dependencies.csCommon)


lazy val csCoreServices = (project in file("cs-core-services"))
  .settings(commonSettings, libraryDependencies ++= Dependencies.csCoreServices)
  .dependsOn(csCommon)

lazy val root = (project in file("."))
.aggregate(
  csCommon,
  csCoreServices
)
.settings(commonSettings)