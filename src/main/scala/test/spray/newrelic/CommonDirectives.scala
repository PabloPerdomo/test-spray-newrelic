package test.spray.newrelic

import java.util.UUID

import com.newrelic.api.agent.NewRelic
import spray.routing.{Directive0, Directive1, Directives}


trait CommonDirectives
	extends Directives {

	def withStartTime: Directive1[Long] = {
		extract(_ => System.currentTimeMillis())
	}

	def withRequestId: Directive1[String] = {
		extract(_ => UUID.randomUUID().toString)
	}

	def traceName(name: String): Directive0 = {
		mapRequestContext { ctx =>
			NewRelic.setTransactionName(null, name)
			ctx
		}
	}

}
