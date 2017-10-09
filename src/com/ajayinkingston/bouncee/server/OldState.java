package com.ajayinkingston.bouncee.server;

public class OldState {
	float x,y,xspeed,yspeed,projectileangle;
	boolean left,right,shot;
	long frame;
	public OldState(float x, float y, float xspeed, float yspeed, long frame, boolean left, boolean right, boolean shot, float projectileangle){
		this.x = x;
		this.y = y;
		this.xspeed = xspeed;
		this.yspeed = yspeed;
		this.frame = frame;
		this.right = right;
		this.left = left;
		this.shot = shot;
		this.projectileangle = projectileangle;
	}
	
}
