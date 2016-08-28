package test.spray.newrelic

import akka.actor._
import spray.routing._

object ServiceRouteActor {
	def props: Props = Props[ServiceRouteActor]
}

class ServiceRouteActor
	extends HttpServiceActor
		with RouteConcatenation
		with ActorLogging {

	private val config = context.system.settings.config.getConfig("test.spray.newrelic")
	val routes = new WrongMetricsRoutes(config.getConfig("wrong-metrics"))(context.system)

	val routeRunning = runRoute(routes.route)

	def receive = routeRunning.orElse {
		case Server.Started =>
			log.info("Starting System...")
		case Server.Stopping =>
			log.info("Stopping System...")
	}

}
