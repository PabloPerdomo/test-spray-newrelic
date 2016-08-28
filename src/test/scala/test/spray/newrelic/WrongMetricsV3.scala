package test.spray.newrelic


class WrongMetricsV3 extends WrongMetricsBaseSimulation {
	override protected val servicePath = "v3"
	doSetUp()
}
