package com.ajayinkingston.planets.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Player extends Entity{
	public int id;
	
	public boolean left,right;//now going to be used
	
	public long frames; //amount of frames passed
	public long lastChecked = System.currentTimeMillis();//last time checked client if they have proper x
	
	public ArrayList<OldState> oldStates = new ArrayList<>();
	
	public boolean paused;
	public int pausedStack;
	
//	boolean shot;//shot this frame   DEPRECATED NOW THERE IS A METHOD
//	float projectileangle;//the angle of that shot if it happened
	
//	long rightstart = -1;
	
//	final float xspeed,yspeed;
	
	public Player(int id, float x, float y, int mass){
		this.id = id;
		this.x = x;
		this.y = y;
		this.mass = mass;
		
		lastChecked = System.currentTimeMillis();
	}
	
	public void sendDataToClient(Main main, Data data){
//		if(System.currentTimeMillis() - lastChecked >= 1000){//1 seconds since last checked?
		if(true){
			main.messenger.sendMessageToAll("CHECK " + id + " " + x + " " + y + " " + xspeed + " " + yspeed + " " + frames);
			lastChecked = System.currentTimeMillis();
		}
		
		
		Planet planet = Main.getClosestPlanet(this, data.planets);
		for(int i=0;i<planet.food.length;i++){
			Food food = planet.food[i];
			if(!food.enabled) continue;
			int foodx = (int) (Math.cos(food.angle)*(planet.radius+food.getSize()/2+5) + planet.x);
			int foody = (int) (Math.sin(food.angle)*(planet.radius+food.getSize()/2+5) + planet.y);
			if(x+getSize()>foodx && x<foodx+food.getSize() && y+getSize()>foody && y<foody+food.getSize()){
				food.enabled = false;
				main.messenger.sendMessageToAll("COLLECT " + id + " " + food.getAmount());
				main.messenger.sendMessageToAll("FOOD " + Main.getIndexOf(planet, data.planets) + " " + i + " " + false + " " + 0 + " " + 0);
				mass += food.getAmount();
			}
		}
	}
	
	public void update(Data data, double delta){
		frames++;
		if(paused){
			pausedStack++;
			return;
		}
//		if(frames > 60){
//			left = true;
//		}
////		if(frames > 150){
////			right = false;
////		}
////		if(frames > 200){
////			left = true;
////		}
////		if(frames > 250){
////			left = false;
////		}
////		if(frames > 300){
////			right = true;
////		}
//		if(frames > 350){
//			left = false;
//		}
//		if(frames > 500){
//			right = true;
//		}
//		if(frames > 650){
//			right = false;
//		}
		
		//gravity
		ArrayList<Planet> closestplanets = Main.getClosestPlanets(this, data.planets);
		float gravityx = 0;
		float gravityy = 0;
		for(Planet planet: closestplanets){
//			System.out.println((player == null) + " " + (planet == null)); 
			double angle = Math.atan2((y) - (planet.y), (x) - (planet.x));
			
//			gravityx += Math.cos(angle) * planet.gravityhelperconstant / ((Math.sqrt(Math.pow((y) - (planet.y), 2) + Math.pow((x) - (planet.x), 2))) - getRadius() - planet.radius + 300) * 350;//XXX: IF YOU CHANGE THIS CHANGE IT IN PLANET CLASS AND SERVER PROJECT TOO
//			gravityy += Math.sin(angle) * planet.gravityhelperconstant / ((Math.sqrt(Math.pow((y) - (planet.y), 2) + Math.pow((x) - (planet.x), 2))) - getRadius() - planet.radius + 300) * 350;
		}
		
		//bouncing
		Planet planet = Main.getClosestPlanet(this, data.planets);
		if(Main.isTouchingPlanet(this, planet)){
			System.out.println(frames + " frame bounced at");
			double angle = Math.atan2((y) - (planet.y), (x) - (planet.x));
			
			double ux = 2 * (getDotProduct(xspeed, yspeed, Math.cos(angle), Math.sin(angle))) * Math.cos(angle);
			double wx = xspeed - ux;
			double uy = 2 * (getDotProduct(xspeed, yspeed, Math.cos(angle), Math.sin(angle))) * Math.sin(angle);
			double wy = yspeed - uy;
			xspeed = (float) (wx - ux);
			yspeed = (float) (wy - uy);
			double finalangle = Math.atan2(yspeed, xspeed);
			xspeed = (float) (Math.cos(finalangle) * planet.bounceheight);
			yspeed = (float) (Math.sin(finalangle) * planet.bounceheight);
			
			double newx = planet.x + planet.radius * ((x - planet.x) / Math.sqrt(Math.pow(x - planet.x, 2) + Math.pow(y - planet.y, 2)));
			double newy = planet.y + planet.radius * ((y - planet.y) / Math.sqrt(Math.pow(x - planet.x, 2) + Math.pow(y - planet.y, 2)));
			x = (float) (newx + Math.cos(angle) * (getRadius()+2));
			y = (float) (newy + Math.sin(angle) * (getRadius()+2));
		}
		
		//add gravity speeds to speed
		xspeed += gravityx*delta;
		yspeed += gravityy*delta;
		
		//movement
		if(right){
			xspeed += Math.cos(Main.getClosestAngle(this, data.planets)+1.5708)*data.speed * delta;//1.5708 is 90 degrees in radians (half pi or quarter tau)
			yspeed += Math.sin(Main.getClosestAngle(this, data.planets)+1.5708)*data.speed * delta;//1.5708 is 90 degrees in radians (half pi or quarter tau)
			
//			System.out.println(frames + " MOVED BY X " + (Math.cos(main.getClosestAngle(this)+1.5708)*main.speed * delta) + " MOVED BY Y " + Math.sin(main.getClosestAngle(this)+1.5708)*main.speed * delta + " AT ANGLE " + main.getClosestAngle(this) + " X " + x + " Y " + y);
			
//			x+=Math.cos(0) * 500*delta;
//			y+=Math.sin(0) * 500*delta;
			
//			if(rightstart == -1){
//				rightstart = frames;
//			}
		}
		if(left){
			xspeed += Math.cos(Main.getClosestAngle(this, data.planets)-1.5708)*data.speed * delta;//1.5708 is 90 degrees in radians (half pi or quarter tau)
			yspeed += Math.sin(Main.getClosestAngle(this, data.planets)-1.5708)*data.speed * delta;//1.5708 is 90 degrees in radians (half pi or quarter tau)

//			x+=Math.cos(-Math.PI) * 500*delta;
//			y+=Math.sin(-Math.PI) * 500*delta;
		}
		
		
		addFriction(delta);
		
		//add all speeds
		x += xspeed*delta;
		y += yspeed*delta;
		
//		//save the shot oldstate in the previous frame DEPRECATED
//		if(oldStates.size()>1){
//			oldStates.get(oldStates.size()-1).shot = shot;
//			oldStates.get(oldStates.size()-1).projectileangle = projectileangle;
//		}
		
		//save old states
		oldStates.add(new OldState(x, y, xspeed, yspeed, frames, left, right, false, 0));
		if(oldStates.size() > 200) oldStates.remove(0);
		
//		if(shot){
//			shot = false;//reset it
//			projectileangle = 0;
//		}
	}
	
	public void addFriction(double delta){
		double movementAngle = Math.atan2(yspeed, xspeed);
		
		double xchange = - (Math.cos(movementAngle) * friction * delta);
		double ychange = - (Math.sin(movementAngle) * friction * delta);
		
		//if they are going to overshoot the goal, set it to zero (the goal), and don't move it
		if(xchange > 0 && xspeed + xchange > 0){
			xspeed = 0;
		}else if(xchange < 0 && xspeed + xchange < 0){
			xspeed = 0;
		}
		if(ychange > 0 && yspeed + ychange > 0){
			yspeed = 0;
		}else if(ychange < 0 && yspeed + ychange < 0){
			yspeed = 0;
		}
		
		if(xspeed != 0) xspeed += xchange;
		if(yspeed != 0) yspeed += ychange;
	}
	
	public void move(Data data, double delta){ //calls one frame of movement
		//movement
//		if(right){
//			xspeed += Math.cos(Main.getClosestAngle(this, data.planets)+1.5708)*data.speed * delta;//1.5708 is 90 degrees in radians (half pi or quarter tau)
//			yspeed += Math.sin(Main.getClosestAngle(this, data.planets)+1.5708)*data.speed * delta;//1.5708 is 90 degrees in radians (half pi or quarter tau)
//			
////					System.out.println(frames + " MOVED BY X " + (Math.cos(main.getClosestAngle(this)+1.5708)*main.speed * delta) + " MOVED BY Y " + Math.sin(main.getClosestAngle(this)+1.5708)*main.speed * delta + " AT ANGLE " + main.getClosestAngle(this) + " X " + x + " Y " + y);
//			
////					x+=Math.cos(0) * 500*delta;
////					y+=Math.sin(0) * 500*delta;
//			
////					if(rightstart == -1){
////						rightstart = frames;
////					}
//		}
//		if(left){
//			xspeed += Math.cos(Main.getClosestAngle(this, data.planets)-1.5708)*data.speed * delta;//1.5708 is 90 degrees in radians (half pi or quarter tau)
//			yspeed += Math.sin(Main.getClosestAngle(this, data.planets)-1.5708)*data.speed * delta;//1.5708 is 90 degrees in radians (half pi or quarter tau)
//
////					x+=Math.cos(-Math.PI) * 500*delta;
////					y+=Math.sin(-Math.PI) * 500*delta;
//		}
		
		addFriction(delta);
	}
	
	public void shoot(float projectileangle, ArrayList<Projectile> projectiles, Class<?> projectileClass){
		Constructor<?> constructor;
		Projectile addedProjectile = null;
		try {
			constructor = projectileClass.getConstructor(double.class, double.class, int.class, double.class, float.class);
			
			addedProjectile = (Projectile) constructor.newInstance(new Object[]{x + ((getSize() + Data.projectilesize/2) * Math.cos(projectileangle)), y + ((getSize() + Data.projectilesize/2) * Math.sin(projectileangle)), Data.projectilesize, projectileangle, Data.projectileSpeed});
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		if(addedProjectile == null){
			System.err.println("ADDED PROJECTILE IS NULL IN PLAYER SHOOT METHOD (SERVER)"); //this should never be called---------------
		}
		
		projectiles.add(addedProjectile);
		
		System.out.println("PLAYER SHOOTING" + id);
		
		xspeed -= (Math.cos(projectileangle) * Data.projectileSpeedChange);
		yspeed -= (Math.sin(projectileangle) * Data.projectileSpeedChange);
		
		oldStates.get(oldStates.size()-1).shot = true;
		oldStates.get(oldStates.size()-1).projectileAngle = projectileangle;
	}
	
	public boolean collided(Entity player){
	   float xdist = x - player.x;
	   float ydist = y - player.y;
	   double dist = Math.sqrt(Math.pow(xdist, 2) + Math.pow(ydist, 2)); //distance between circles (xdist and ydist and a and b of triangle)
	
		if(dist < player.getRadius() + getRadius()){
		    //collided
		    return true;
		}
		return false;
	}
	
}
