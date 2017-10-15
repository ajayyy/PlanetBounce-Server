package com.ajayinkingston.planets.server;

public interface ClientMessageReceiver {
	public void onMessageRecieved(String message, int id);
	
	public void onConnected(int id);
	
	public void onDisconnected(int id);
}
