name := "poetrader"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.9"

libraryDependencies += "org.mashupbots.socko" %% "socko-webserver" % "0.6.0"

libraryDependencies += "com.twitter" %% "chill" % "0.6.0"

libraryDependencies += "org.pircbotx" % "pircbotx" % "2.0.1"

libraryDependencies += "org.jsoup" % "jsoup" % "1.8.3"

mainClass in Compile := Some("org.trade.TraderMain")