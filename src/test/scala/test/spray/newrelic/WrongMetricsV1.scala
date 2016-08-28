package test.spray.newrelic


class WrongMetricsV1 extends WrongMetricsBaseSimulation {
	override protected val servicePath = "v1"
	doSetUp()
}
