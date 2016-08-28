package test.spray.newrelic

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.DurationInt


object Main
	extends StrictLogging {

	def main(args: Array[String]): Unit = {
		val bindAddress: String = if (args.isDefinedAt(0)) args(0) else "0.0.0.0"
		val bindPort: Int = if (args.isDefinedAt(1)) args(1).toInt else 9398
		val config = ConfigFactory.load()


		logger.trace("Config: " + config.toString)
		val server = new Server("test", bindAddress, bindPort, ServiceRouteActor.props, Option(config))
		server.startNow(10.seconds)
		logger.info(s"Server started at $bindAddress:$bindPort")
	}
}
