name := "Threesixty"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
    "com.typesafe.akka"             %% "akka-actor"         % "2.4.1",
    "com.typesafe.akka"             %% "akka-testkit"       % "2.4.1",
    "org.skife.com.typesafe.config" %  "typesafe-config"    % "0.3.0",
    "io.spray"                      %% "spray-can"          % "1.3.3",
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
