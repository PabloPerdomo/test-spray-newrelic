var express = require('express');
var random = require("random-js")();

var listenPort = 9399;
var app = express();

var mockService = function(minTimeMs, maxTimeMs) {
	return function(req, resp) {
		var respTime = random.integer(minTimeMs, maxTimeMs);
		var body = random.string(10);
		console.log("Request received in path '%s'. Response time is %d and body will be '%s'", req.path, respTime, body);
		setTimeout(function() {
			resp.set('Content-Type', 'text/plain');
			resp.send(body);
		}, respTime);
	};
};

app.get('/mock/wrong-metrics/target', mockService(4000, 7000));
app.get('/mock/wrong-metrics/data-01', mockService(50, 150));
app.get('/mock/wrong-metrics/data-02', mockService(150, 250));

app.listen(listenPort);
console.log('Listening on port %s ...', listenPort);
