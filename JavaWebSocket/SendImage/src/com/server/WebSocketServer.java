package com.server;

import org.eclipse.jetty.server.Server;

public class WebSocketServer {

    public static void main(String[] args) throws Exception {
        final Server server = new Server(2014);
        server.setHandler(new WSHandler());
        server.setStopTimeout(0);
        server.start();
        server.join();
    }
}
