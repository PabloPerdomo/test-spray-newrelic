package test.spray.newrelic


class WrongMetricsV4 extends WrongMetricsBaseSimulation {
	override protected val servicePath = "v4"
	doSetUp()
}
