package com.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.data.DataManager;
import com.data.SourceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.util.Msg;

@WebSocket
public class WSHandler extends WebSocketHandler {
	private Session mSession;
	private static ArrayList<WSHandler> sessions = new ArrayList<WSHandler>();
	private SourceManager mSourceManager;

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
    	mSourceManager = DataManager.getSourceManager();
    	
    	mSession = session;
    	sessions.add(this);
    	session.setIdleTimeout(0);
    	
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        
        JsonObject jsonObj = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        
        String[] sources = mSourceManager.getSources();
        if (sources != null) {
        	for (String source : sources) {
        		jsonArray.add(new JsonPrimitive(source));
        	}
        }
        
        jsonObj.add(Msg.MSG_SOURCES, jsonArray);

        String s = jsonObj.toString();
        
        try {
			session.getRemote().sendString(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
    	JsonParser parser = new JsonParser();
    	boolean isJSON = true;
    	JsonElement element = null;
    	try {
    		element =  parser.parse(message);
    	}
    	catch (JsonParseException e) {
    		System.out.println("exception: " + e);
    		isJSON = false;
    	}
    	
        if (isJSON && element != null) {
        	JsonObject obj = element.getAsJsonObject();
        	element = obj.get(Msg.MSG_MESSAGE);
        	if (element != null) {
        		switch (element.getAsString()) {
        		case Msg.MSG_SOURCE:
        			int index = obj.get(Msg.MSG_INDEX).getAsInt();
        			mSourceManager.acquireImage(index);
        			break;
        		}
        	}
        }

        System.out.println("Message: " + message);
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