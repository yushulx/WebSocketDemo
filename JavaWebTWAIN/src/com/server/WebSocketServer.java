package com.server;

import org.eclipse.jetty.server.Server;

public class WebSocketServer extends Thread{
    
    @Override
    public void run() {
    	super.run();

        try {
        	Server server = new Server(2014);
        	server.setHandler(new WSHandler());
        	server.setStopTimeout(0);
        	server.start();
        	server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
