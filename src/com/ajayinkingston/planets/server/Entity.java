package com.ajayinkingston.planets.server;

public class Entity {
	public float x;
	public float y;
	public float xspeed,yspeed;
	public int mass;
	
	public int friction = 100;
	
	public int getSize(){
		return mass; //in the future it will be more complicated than just this
	}
	
	public int getRadius(){
		return getSize()/2;
	}
	
	//util methods
	public double getDotProduct(double x1, double y1, double x2, double y2){
		return x1 * x2 + y1 * y2;
	}
	
	public boolean collided(Entity player){
		return collideDist(player) < getRadius() + player.getRadius();
	}
	
	public double collideDist(Entity player){
		float xdist = x - player.x;
		float ydist = y - player.y;
		double dist = Math.sqrt(Math.pow(xdist, 2) + Math.pow(ydist, 2)); //distance between circles (xdist and ydist and a and b of triangle)
	
		return dist;
	}
}
