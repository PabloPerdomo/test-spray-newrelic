package test.spray.newrelic

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


abstract class WrongMetricsBaseSimulation extends Simulation {

	val httpConf = http
		.baseURL("http://localhost:9398/test/spray/newrelic/wrong-metrics")
		.doNotTrackHeader("1")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.acceptEncodingHeader("gzip, deflate")
		.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

	protected def servicePath: String

	protected def doSetUp(): Unit = {
		val scn = scenario(s"WrongMetrics-$servicePath").exec(
			exec {
				http(s"request-$servicePath").get(s"/$servicePath")
			}
		)

		setUp(
			scn.inject(
				rampUsers(3) over (10 seconds),
				nothingFor(5 seconds),
				rampUsers(40) over (2 minutes)
			).protocols(httpConf)
		)
	}

}
