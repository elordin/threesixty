name := "Threesixty"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-encoding", "UTF-8"
)

// taken from: http://github.com/scala/scala-module-dependency-sample
libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, add dependency on scala-xml module
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value ++ Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
      )
    case _ =>
      libraryDependencies.value
  }
}

libraryDependencies ++= Seq(
    "com.typesafe.akka"             %% "akka-actor"         % "2.4.1",
    "com.typesafe.akka"             %% "akka-testkit"       % "2.4.1",
    "org.skife.com.typesafe.config" %  "typesafe-config"    % "0.3.0",
    "io.spray"                      %% "spray-can"          % "1.3.3",
    "io.spray"                      %% "spray-util"         % "1.3.3",
    "io.spray"                      %% "spray-caching"      % "1.3.3",
    "io.spray"                      %% "spray-json"         % "1.3.2",
    "org.scalatest"                 %  "scalatest_2.11"     % "2.2.4"   % "test"
)

resolvers ++= Seq(
  "Typesafe repository snapshots"    at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases"     at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  "Websudos releases"                at "https://dl.bintray.com/websudos/oss-releases/"
)

libraryDependencies ++= Seq(
  "com.websudos"  %% "phantom-dsl"                   % "1.18.1",
  "com.websudos"  %% "phantom-testkit"               % "1.18.1"
)

libraryDependencies ++= Seq(
  "io.spray" %% "spray-routing-shapeless2" % "1.3.3"
)

libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.3.4"
)

//Build Deploymenttools
enablePlugins(JavaServerAppPackaging)
enablePlugins(JavaAppPackaging)
enablePlugins(SbtNativePackager)
enablePlugins(UniversalPlugin)

packageSummary in Linux := "360° - My personal health and fitness monitor"
packageSummary in Windows := "360° - My personal health and fitness monitor"
packageDescription := "Visualization engine for fitness data"

maintainer in Windows := "Heroes of SE <se@openinnovator.net>"
maintainer in Debian := "Heroes of SE <se@openinnovator.net>"




//Windows Build
enablePlugins(WindowsPlugin)

mappings in Windows := (mappings in Universal).value

wixProductId := "AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA"
wixProductUpgradeId := "AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA"


//wixFile := File("deployment/wixconfig.xml")

//Command creating it
//sbt windows:packageBin

//DEBIAN/Linus
enablePlugins(DebianPlugin)


//Creates universal zip
//sbt universal:packageBin
