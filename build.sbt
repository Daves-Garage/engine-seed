
name              := "engine-seed"
organization      := "Daves Garage"
version           := "1.0"
scalaVersion      := "2.11.5"

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-stream-experimental" % "1.0-M4",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0-M4",
  "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0-M4"
	)

resolvers += "Sonotype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "TypeSafe" at "https://repo.typesafe.com/typesafe/releases/"




