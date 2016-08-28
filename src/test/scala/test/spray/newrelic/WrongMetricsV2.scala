package test.spray.newrelic


class WrongMetricsV2 extends WrongMetricsBaseSimulation {
	override protected val servicePath = "v2"
	doSetUp()
}
