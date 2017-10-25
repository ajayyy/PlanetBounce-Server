package com.ajayinkingston.planets.server;

public class OldState {
	public float x,y,xspeed,yspeed,projectileAngle;
	public boolean left,right,shot;
	public long frame;
	public OldState(float x, float y, float xspeed, float yspeed, long frame, boolean left, boolean right, boolean shot, float projectileangle){
		this.x = x;
		this.y = y;
		this.xspeed = xspeed;
		this.yspeed = yspeed;
		this.frame = frame;
		this.right = right;
		this.left = left;
		this.shot = shot;
		this.projectileAngle = projectileangle;
	}
	
}
