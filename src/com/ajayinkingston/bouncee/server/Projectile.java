package com.ajayinkingston.bouncee.server;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;

public class Projectile extends Entity{
	int radius;
	long frame; //frame currently taken place
	
	double distance;
	long start;
	
	ArrayList<OldState> oldstates = new ArrayList<>();
	
	boolean dead;
	long deadframe;
	
	public Projectile(double x, double y, int radius, double angle, float speed){
		this.x = (float) x;
		this.y = (float) y;
		this.radius = radius;
		this.xspeed = (float) (Math.cos(angle) * speed);
		this.yspeed = (float) (Math.sin(angle) * speed);
		
		friction = 150;
		
		start = System.currentTimeMillis();
	}
	
	public void update(Main main, double delta){
		frame++;

		if(Math.abs(xspeed) < friction*delta) xspeed = 0;
		else if(xspeed>0) xspeed-=friction*delta;
		else if(xspeed<0) xspeed+=friction*delta; //XXX IF YOU CHANGE THIS CHANGE CLIENT TOO
		if(Math.abs(yspeed) < friction*delta) yspeed = 0;
		else if(yspeed>0) yspeed-=friction*delta;
		else if(yspeed<0) yspeed+=friction*delta;
		
		ArrayList<Planet> closestplanets = main.getClosestPlanets(this);
		float gravityx = 0;
		float gravityy = 0;
		for(Planet planet:closestplanets){
//			System.out.println((this == null) + " " + (planet == null));
			double angle = Math.atan2((y) - (planet.y), (x) - (planet.x));
			
			float multiplier = 350;
			
			if (distance > 3000 || System.currentTimeMillis() - start > 3500) friction = 400;
			
			gravityx += Math.cos(angle) * planet.gravityhelperconstant / ((Math.sqrt(Math.pow((y) - (planet.y), 2) + Math.pow((x) - (planet.x), 2))) - radius - planet.radius + 300) * multiplier;//XXX: IF YOU CHANGE THIS CHANGE IT IN PLANET CLASS AND SERVER PROJECT TOO
			gravityy += Math.sin(angle) * planet.gravityhelperconstant / ((Math.sqrt(Math.pow((y) - (planet.y), 2) + Math.pow((x) - (planet.x), 2))) - radius - planet.radius + 300) * multiplier;
		}
		xspeed += gravityx * delta;
		yspeed += gravityy * delta;
		
		x+=xspeed*delta;
		y+=yspeed*delta;
		distance += Math.abs(xspeed*delta) + Math.abs(yspeed*delta);
	
		oldstates.add(new OldState(x, y, xspeed, yspeed, frame, false, false, false, 0));
	}
	
	
	public int getSize(){
		return getRadius()*2;
	}
	
	public int getRadius(){
		return radius;
	}
}
