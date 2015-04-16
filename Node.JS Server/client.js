//Just for testing.
var WebSocket = require('ws');
var ws = new WebSocket('ws://localhost:8080/gencode');

ws.on('open', function open() {
  
	//Send hello Message to server.
	 var hello = { op:'hello'};
	 ws.send(JSON.stringify(hello));  
});

ws.on('message', function(data, flags) {
  var obj = JSON.parse(data);

  console.log("Receivied:" +JSON.stringify(obj));
  
  if(obj.op == 'hello')
  {
  	console.log("### Got hello token "+obj.token);
  }
  else if(obj.op == 'authdone')
  {
  	console.log("### Got auth token "+obj.accessToken);
  	ws.close();
  }
  
});