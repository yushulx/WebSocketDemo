WebSocket Sample
=========
All sample code is based on [SuperWebSocket][1] and [Dynamic .NET TWAIN][2]


Code
-----------

##### WebSocketImageSend
1. Create a WebSocketServer
2. Load a PNG image with Dynamic .NET TWAIN
3. Convert PNG to BMP
4. Send ArrayBuffer to JavaScript Web Client
5. Create a new image element, and draw ArrayBuffer to the canvas
6. Append the image element to web page

##### WebSocketBarcodeGenerator
1. Install [Dynamsoft Barcode SDK for .NET][3]
2. Create a new project, and add DynamicBarcode.dll
3. Encode the message, which is sent from a web client, to generate a barcode, and draw the barcode onto a background image
4. Send the mixed image back to the web client

##### WebSocketScanDocument
1. Query scanner sources with Dynamic .NET TWAIN component on WebSocket server side
2. Send the JSON data of source list to client
3. Scan documents on server side, and send the captured image to the Web client

[1]:http://superwebsocket.codeplex.com/
[2]:https://www.dynamsoft.com/Secure/Register_ClientInfo.aspx?productName=NetTWAIN&from=FromDownload
[3]:http://www.dynamsoft.com/Products/.net-barcode-detection-decode-sdk.aspx
