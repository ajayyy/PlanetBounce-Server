package com.ajayinkingston.bouncee.server;

public class WebSocketServerMessenger
{
	WSServer wss;
	
	ClientMessageReceiver reciever;
	
	//For Bidirectional Communication mode	
	public WebSocketServerMessenger(int port, ClientMessageReceiver s)
	{
		wss = new WSServer(port, this);
		this.reciever = s; //To call the methods of the the upper level class			
	}
	
	//Please, read Message codes in the ClientMSG comments.
	public void onMessage(String message, int id)
	{
		reciever.onMessageRecieved(message, id);
	}
	
	//Please, read Message codes in the ClientMSG comments.
	public boolean sendMessageToClient(int clientID, String message)
	{		
		return (wss.sendToClient(clientID, message));
		 
	}
	
	public void sendMessageToAll(String message)
	{
		wss.sendToAll(message);
	}
	
	//one method for each messages / actions that the server can do
	
	public void close()
	{
		wss.stop();
	}
}