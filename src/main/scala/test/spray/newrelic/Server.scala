package test.spray.newrelic

import akka.actor._
import akka.io.IO
import akka.pattern.{AskSupport, ask}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import spray.can.Http

import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.sys.ShutdownHookThread


object Server {

	case class Start(host: String, port: Int)

	case class Stop(timeout: Duration)

	case object Started

	case object Stopping

	case object Stopped

	val defaultTimeout = 5 minutes
}

class Server(val host: String, val port: Int, val system: ActorSystem, serviceFactory: Props) extends StrictLogging {

	def this(name: String, host: String, port: Int, serviceFactory: Props, config: Option[Config] = None) = {
		this(host, port, ActorSystem(name, config.getOrElse(ConfigFactory.load())), serviceFactory)
	}

	val server = system.actorOf(Props(new ServerActor(serviceFactory)), s"${system.name}-server-actor")

	var shutdownHook: Option[ShutdownHookThread] = None

	def start(timeout: FiniteDuration = Server.defaultTimeout): Future[Server.Started.type] = {
		shutdownHook = Option(sys.addShutdownHook {
			logger.info("Preparing for shutdown...")
			awaitStopped(timeout)
		})

		server.ask(Server.Start(host, port))(Timeout(timeout)).mapTo[Server.Started.type]
	}

	def startNow(timeout: FiniteDuration = Server.defaultTimeout): Unit = {
		Await.ready(start(timeout), timeout)
	}

	def stop(timeout: FiniteDuration = Server.defaultTimeout): Future[Server.Stopped.type] = {
		removeShutdownHook()
		stopInternal(timeout)
	}

	private def stopInternal(timeout: FiniteDuration): Future[Server.Stopped.type] = {
		server.ask(Server.Stop(timeout))(Timeout(timeout)).mapTo[Server.Stopped.type]
	}

	private def awaitStopped(timeout: FiniteDuration = Server.defaultTimeout): Unit = Await.ready(stopInternal(timeout), timeout)

	private def removeShutdownHook(): Unit = {
		shutdownHook.map(_.remove())
		shutdownHook = None
	}

	def stopNow(timeout: FiniteDuration = Server.defaultTimeout): Unit = {
		removeShutdownHook()
		val stopAsk = server.ask(Server.Stop(Duration.Zero))(Timeout(timeout))
		Await.ready(stopAsk, timeout)
	}
}

class ServerActor(serviceFactory: Props) extends Actor with AskSupport with ActorLogging {

	import context.system

	val service = context.actorOf(serviceFactory, s"${context.system.name}-server-service")

	def receive: Receive = stopped orElse unknown("stopped")

	def unknown(state: String): Receive = {
		case someUnknown: Any => log.warning(s"${context.system.name} server got unexpected message [$someUnknown] while in state [$state]")
	}

	val stopped: Receive = {
		case Server.Start(host, port) =>
			IO(Http) ! Http.Bind(service, host, port)
			become("starting", starting(sender()))
	}

	def starting(starter: ActorRef): Receive = {
		case Http.Bound(address) =>
			val listener = sender()
			context.watch(listener)
			starter ! Server.Started
			service ! Server.Started
			become("started", started(listener))
	}

	def started(listener: ActorRef): Receive = {
		case Server.Stop(timeout) =>
			listener ! Http.Unbind(timeout)
			service ! Server.Stopping
			become("stopping", stopping(sender()))
	}

	def stopping(stopper: ActorRef): Receive = {
		case Http.Unbound =>
			log.info("Unbound service actor")

		case Terminated(who) =>
			log.info("HTTP Listener is dead...we now can shutdown")
			context.system.shutdown()
			stopper ! Server.Stopped
			become("stopped", stopped)

	}

	def become(state: String, receive: Receive) = context.become(receive orElse unknown(state))

}
