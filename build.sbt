name := "test-spray-newrelic"

version := "1.0"

scalaVersion := "2.11.8"

val sprayVersion = "1.3.2"
val akkaVersion = "2.3.9"

libraryDependencies ++= Seq(
	// akka
	"com.typesafe.akka" %% "akka-actor" % akkaVersion,
	"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
	// spray
	"io.spray" %% "spray-can" % sprayVersion,
	"io.spray" %% "spray-routing" % sprayVersion,
	"io.spray" %% "spray-httpx" % sprayVersion,
	"io.spray" %% "spray-http" % sprayVersion,
	"io.spray" %% "spray-client" % sprayVersion,
	// logback
	"ch.qos.logback" % "logback-classic" % "1.1.2",
	// NewRelic:
	"com.newrelic.agent.java" % "newrelic-api" % "3.31.1"
)

val test = project.in(file("."))
	.enablePlugins(GatlingPlugin)
	.settings(
		libraryDependencies ++= Seq(
			"io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.0" % "test",
			"io.gatling"            % "gatling-test-framework"    % "2.2.0" % "test"
		)
	)
