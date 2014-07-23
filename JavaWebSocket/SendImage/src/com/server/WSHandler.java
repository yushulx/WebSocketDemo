package com.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@WebSocket
public class WSHandler extends WebSocketHandler {
	private Session mSession;
	private static ArrayList<WSHandler> sessions = new ArrayList<WSHandler>();

	public static ArrayList<WSHandler> getAllSessions() {
		return sessions;
	}
	
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        sessions.remove(this);
        System.out.println("Close: statusCode = " + statusCode + ", reason = " + reason + ", sessions = " + sessions.size());
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
    	mSession = session;
    	sessions.add(this);
    	
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        System.out.println("Message: " + message);
        if (message.equals("image")) {
        	System.out.println("session: " + mSession);
        	if (mSession != null) {
				try {
					File f = new File("image\\github.jpg");
					BufferedImage bi = ImageIO.read(f);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ImageIO.write(bi, "png", out);
					ByteBuffer byteBuffer = ByteBuffer.wrap(out.toByteArray());
					mSession.getRemote().sendBytes(byteBuffer);
					out.close();
					byteBuffer.clear();

				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
    }

	@Override
	public void configure(WebSocketServletFactory factory) {
		// TODO Auto-generated method stub
		factory.register(WSHandler.class);
	}
	
	public void sendImage(byte[] data) {
		if (mSession == null)
			return;
		
		try {        	
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            mSession.getRemote().sendBytes(byteBuffer);
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}