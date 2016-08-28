package test.spray.newrelic

import akka.actor.ActorSystem
import com.typesafe.config.Config
import spray.client.pipelining.{Get, WithTransformerConcatenation, addHeader, sendReceive, unmarshal}
import spray.http.StatusCodes
import spray.routing._
import spray.routing.directives.LoggingMagnet

import scala.concurrent.Future
import scala.util.{Failure, Success}


class WrongMetricsRoutes(config: Config)(implicit actorSystem: ActorSystem)
	extends CommonDirectives
		with RouteConcatenation
		with StrictLogging {

	implicit val ec = actorSystem.dispatcher

	def doRequest(url: String, requestId: String): Future[String] = {
		val pipeline = addHeader("X-RequestId", requestId) ~> sendReceive ~> unmarshal[String]
		pipeline(Get(url))
	}

	val dataService01Url: String = config.getString("urls.data-01")
	val dataService02Url: String = config.getString("urls.data-02")
	val targetServiceUrl: String = config.getString("urls.target")

	def doServiceV1(requestId: String): Future[String] = {
		val fDataReq01 = doRequest(dataService01Url, requestId)
		val fDataReq02 = doRequest(dataService02Url, requestId)
		for {
			dataReq01 <- fDataReq01
			dataReq02 <- fDataReq02
			result <- doRequest(targetServiceUrl, requestId)
		} yield result
	}

	def doServiceV2(requestId: String): Future[String] = {
		val fDataReq01 = doRequest(dataService01Url, requestId)
		val fDataReq02 = doRequest(dataService02Url, requestId)
		fDataReq01.flatMap { dataReq01 =>
			fDataReq02.flatMap { dataReq02 =>
				doRequest(targetServiceUrl, requestId)
			}
		}
	}

	def doServiceV3(requestId: String): Future[String] = {
		Future {
			val fDataReq01 = doRequest(dataService01Url, requestId)
			val fDataReq02 = doRequest(dataService02Url, requestId)
			for {
				dataReq01 <- fDataReq01
				dataReq02 <- fDataReq02
			} yield (dataReq01, dataReq02)
		}.flatMap(_.flatMap { _ =>
			doRequest(targetServiceUrl, requestId)
		})
	}

	def doServiceV4(requestId: String): Future[String] = {
		doRequest(dataService01Url, requestId).flatMap { dataReq01 =>
			doRequest(dataService02Url, requestId).flatMap { dataReq02 =>
				doRequest(targetServiceUrl, requestId)
			}
		}
	}

	def doTargetOnly(requestId: String): Future[String] = {
		doRequest(targetServiceUrl, requestId)
	}

	def serviceRoute(serviceName: String, service: String => Future[String]): Route = {
		path(serviceName) {
			withStartTime { startTime =>
				traceName(serviceName) {
					withRequestId { requestId =>
						logRequest(LoggingMagnet(r => logger.debug(s"[$requestId] Request: $r"))) {
							logResponse(LoggingMagnet(r => logger.debug(s"[$requestId] Response: ${System.currentTimeMillis() - startTime} - $r"))) {
								onComplete(service(requestId)) {
									case Success(str) =>
										complete(StatusCodes.OK -> str)
									case Failure(ex) =>
										failWith(ex)
								}
							}
						}
					}
				}
			}
		}
	}

	val route: Route = get {
		pathPrefix("test" / "spray" / "newrelic" / "wrong-metrics") {
			serviceRoute("v1", doServiceV1) ~
				serviceRoute("v2", doServiceV2) ~
				serviceRoute("v3", doServiceV3) ~
				serviceRoute("v4", doServiceV4) ~
				serviceRoute("target-only", doTargetOnly)
		}
	}

}
