package test.spray.newrelic

import scala.language.postfixOps


class WrongMetricsTargetOnly extends WrongMetricsBaseSimulation {
	override protected val servicePath = "target-only"
	doSetUp()
}
