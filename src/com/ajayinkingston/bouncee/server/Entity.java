package com.ajayinkingston.bouncee.server;

public class Entity {
	float x,y;
	float xspeed,yspeed;
	int mass;
	
	int friction = 100;
	
	public int getSize(){
		return mass; //in the future it will be more complicated than just this
	}
	
	public int getRadius(){
		return getSize()/2;
	}
}
