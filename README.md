# test-spray-newrelic

The idea of this repo is replicate some issues that I'm having in production applications and then report the issue in the NewRelic support forum.  
Currently I'm using the NewRelic java agent with Scala 2.11 and [Spray](http://spray.io/) as HTTP server and client.

For reporting into NewRelic a license_key is needed.  
I define the license_key as a VM option in the run configuration:
    -Dnewrelic.environment=dev  
    -Dnewrelic.config.file=&lt;path-to-repo&gt;/test-spray-newrelic/src/main/resources/newrelic.yml  
    -Dnewrelic.config.license_key=&lt;license_key&gt;  
    -javaagent:<path-to-ivy-cache>/com.newrelic.agent.java/newrelic-agent/jars/newrelic-agent-3.31.1.jar  

Test scenarios are defined using [gatling](http://gatling.io/) in the test project files.

Also, some tests need to do HTTP requests to replicate the production behavior.  
The mocks.js file defines mocks for this services using nodejs [express module](https://expressjs.com/).


## Wrong Metrics Issue
The first issue that I can isolate is about the [reported web transaction times](https://discuss.newrelic.com/t/wrong-web-transaction-ui-metrics-with-scala-2-11-and-spray/40500).  
One of my applications is a provider proxy.  
It receives a request, invoke two data services (150 ms avg each one), transforms the input, and then do a request to the target app (5 seconds avg).  
Then, the web transaction time in the NewRelic UI should be about 5 seconds, but it shows 250 ms instead.

The test services for this issue are defined in the WrongMetricsRoutes class. This class defines 5 services:
* GET /test/spray/newrelic/wrong-metrics/v1
* GET /test/spray/newrelic/wrong-metrics/v2
* GET /test/spray/newrelic/wrong-metrics/v3
* GET /test/spray/newrelic/wrong-metrics/v4
* GET /test/spray/newrelic/wrong-metrics/target-only

The v1 is the ideal. I currently use this in production.  
The v2 is the same but using flatMap instead of for comprehension (just for test if this change something).  
The v3 is a workaround test, but it also reports wrong metrics.  
The v4 is not the same of the others because the data services are not invoked in parallel. It has the same trouble.  
And the target-only do not invoke any data service. Just invokes directly the target service. In this case the metrics are well reported.

The UI issue can be seen in this [permalik](https://rpm.newrelic.com/accounts/1408561/applications/22459112?tw%5Bend%5D=1472394530&tw%5Bstart%5D=1472391831).

