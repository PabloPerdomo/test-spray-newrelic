package test.spray.newrelic

import org.slf4j.{Logger, LoggerFactory}


trait StrictLogging {

	val logger: Logger = LoggerFactory.getLogger(this.getClass)

}
