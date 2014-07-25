var MSG_SOURCES = "Sources";
var MSG_SOURCE = "Source";

//var ws = new WebSocket("ws://127.0.0.1:2014/");
var ws = new WebSocket("ws://192.168.8.84:2014/");
ws.binaryType = "arraybuffer";

ws.onopen = function() {
	updateStatus('status', "Opened");
    var msg = JSON.stringify({"client":"I'm client"});
    ws.send(msg);
};

ws.onmessage = function (evt) { 
    var data = evt.data;
    if (data instanceof ArrayBuffer) {
    	var bytes = new Uint8Array(data);
    	var data = "";
        var len = bytes.byteLength;
        for (var i = 0; i < len; ++i) {
        	data += String.fromCharCode(bytes[i]);
        }
        var img = document.getElementById("image");
        img.src = "data:image/png;base64,"+window.btoa(data);
    }
    else {
    	var json = JSON.parse(data);
    	var value = json[MSG_SOURCES];
    	showSources(value);
    }
    
};

ws.onclose = function() {
	updateStatus('status', "Closed");
};

ws.onerror = function(err) {
	updateStatus('status', "Error: " + err);
};

var button = document.getElementById("button");
button.onclick = function() {
	var sources = document.querySelectorAll('option');
    for (var i in sources) {
        if (sources[i].selected) {
        	var json = {};
        	json.Message = MSG_SOURCE;
        	json.Index = i;
        	var msg = JSON.stringify(json);
            ws.send(msg);
            break;
        };
    }
	
};

function showSources(values) {
    var sources = document.getElementById('sources');
    var option;

    var count = values.length;
    if (count == 0) {
        option = document.createElement('option');
        option.text = "N/A";
        sources.appendChild(option);
    }
    else {
        for (var i = 0; i < count; i++) {
            option = document.createElement('option');
            option.text = values[i];
            sources.appendChild(option);
        };
    }
}

function updateStatus(id, value) {
	document.getElementById(id).innerHTML = value;
}
