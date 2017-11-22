package com.ajayinkingston.planets.server;

import java.util.ArrayList;

/**
 * All the data here so it's the same on client and server
 */
public class Data {
	public ArrayList<Player> players = new ArrayList<>();

	public Planet[] planets = new Planet[0];

	public ArrayList<Projectile> projectiles = new ArrayList<>();
	
	public float speed = 500;
	
	public static int projectilesize = 5;
	public static float projectileSpeedChange = 250;
	public static float projectileSpeed = 1000;
	
	//util functions
	
	public static int getIndexOf(Object toSearch, Object[] tab ){
		for( int i=0; i< tab.length ; i ++ ) {
			if( tab[ i ] == toSearch){
				return i;
			}
		}
		
		return -1;
	}
	
	public static ArrayList<Planet> getClosestPlanets(Entity player, Planet[] planets) {
		ArrayList<Planet> closeplanets = new ArrayList<>();
		Planet closest = null;
		float closestdistance = 0;
		for(int i=0;i<planets.length;i++){
			if(planets[i] == null){
				System.out.println("Planet is null: " + i);
			}
//				if(Math.pow(Math.abs(player.x - planets[i].x), 2) + Math.pow(Math.abs(player.y - planets[i].y), 2) < Math.pow(player.mass/2 + planets[i].radius, 2)){
				//collided
//					double angle = Math.atan2((player.y) - (planets[i].y), (player.x) - (planets[i].x));
////					double angle = Math.atan2((splats.planets[i].y) - y, (splats.planets[i].x) - x);
//
//					player.yspeed = (float) (Math.sin(angle) * planets[i].bounceheight); //TODO BOUNCE HEIGHT A FORMULA FROM GRAVITY TO DIFFER IN EVERY PLANET AND FOR EVERY PLAYER WITH DIFFERENT MASS
//					player.xspeed = (float) (Math.cos(angle) * planets[i].bounceheight);
////					yspeed = -yspeed;
////					System.out.println((Math.cos(angle) * splats.planets[i].gravity / (Math.sqrt(Math.pow((y) - (splats.planets[i].y), 2) + Math.pow((x) - (splats.planets[i].x), 2))) * 400) + " " + (Math.sin(angle) * splats.planets[i].gravity / (Math.sqrt(Math.pow((y) - (splats.planets[i].y), 2) + Math.pow((x) - (splats.planets[i].x), 2))) * 400));
//					closeplanets.add(splats.planets[i]);
//					if(closest == null || Math.pow(Math.abs(x - splats.planets[i].x), 2) + Math.pow(Math.abs(y - splats.planets[i].y), 2) < closestdistance){
//						closest = splats.planets[i];
//						closestdistance = (float) (Math.pow(Math.abs(x - splats.planets[i].x), 2) + Math.pow(Math.abs(y - splats.planets[i].y), 2));
//					}
			if (Math.pow(Math.abs(player.x - planets[i].x), 2) + Math.pow(Math.abs(player.y - planets[i].y), 2) < Math.pow(player.getSize() / 2 + (planets[i].radius * 3.5f), 2)) {
				//close
				closeplanets.add(planets[i]);
				if(closest == null || Math.pow(Math.abs(player.x - planets[i].x), 2) + Math.pow(Math.abs(player.y - planets[i].y), 2) < closestdistance){
					closest = planets[i];
					closestdistance = (float) (Math.pow(Math.abs(player.x - planets[i].x), 2) + Math.pow(Math.abs(player.y - planets[i].y), 2));
				}
				if(planets[i] == null) System.out.print(i+"sdsadsadSADKLJAKLJADLKJDLKJADSKLoiurweiourweoi");
			}else if(closest == null || Math.pow(Math.abs(player.x - planets[i].x), 2) + Math.pow(Math.abs(player.y - planets[i].y), 2) < closestdistance){
				closest = planets[i];
				closestdistance = (float) (Math.pow(Math.abs(player.x - planets[i].x), 2) + Math.pow(Math.abs(player.y - planets[i].y), 2));
			}
		}
		
		closeplanets.remove(closest);
		closeplanets.add(0, closest);//Put it at the back of the list
		
		return closeplanets;
	}
	
	public static Planet getClosestPlanet(Entity player, Planet[] planets){
		ArrayList<Planet> closestplanets = getClosestPlanets(player, planets);
		return closestplanets.get(0);
	}
	
	public static double getClosestAngle(Entity player, Planet[] planets){
		Planet planet = getClosestPlanet(player, planets);
		double angle = Math.atan2((player.y) - (planet.y), (player.x) - (planet.x));
		double closestangle = angle - Math.PI;
		return closestangle;
	}
	
	public static boolean isTouchingPlanet(Entity player, Planet planet) {
		return Math.pow(Math.abs(player.x - planet.x), 2) + Math.pow(Math.abs(player.y - planet.y), 2) < Math.pow(player.getRadius() + planet.radius, 2);
	}
	
	public static double getAngleFromPlanet(Player player, Planet planet){
		double angle = Math.atan2((player.y) - (planet.y), (player.x) - (planet.x));
		double closestangle = angle - Math.PI;
		return closestangle;
	}
	
	
	
	public static Player getPlayer(int id, ArrayList<Player> players){
		for(Player player:players){
			if(player.id == id){
				return player;
			}
		}
		System.out.println("Player doesn't exist when getting player");
		return null;
	}

	public static OldState getOldStateAtFrame(ArrayList<OldState> oldStates, long frame){
		if(oldStates.size() == 0) return null;
		
		OldState atFrame = null;
		oldStates = new ArrayList<>(oldStates);
		for(OldState oldState: oldStates){
			if(oldState.frame == frame) atFrame = oldState;
		}
		
		if(atFrame == null) atFrame = oldStates.get(oldStates.size()-1);
		return atFrame;
	}
	
	public static ArrayList<OldState> removeFutureOldStatesFromFrame(ArrayList<OldState> oldStates, long frame){
		OldState cutoff = getOldStateAtFrame(oldStates, frame);
		
		return removeFutureOldStatesFromOldState(oldStates, cutoff);
	}
	
	public static ArrayList<OldState> removeFutureOldStatesFromOldState(ArrayList<OldState> oldStates, OldState cutoff){
		oldStates = new ArrayList<>(oldStates);
		
		while(oldStates.size() > oldStates.indexOf(cutoff)+1){
			oldStates.remove(oldStates.size()-1);
		}
		
		return oldStates;
	}
	
	public static ArrayList<OldState> getOldStatesAfterOldState(ArrayList<OldState> oldStates, OldState cutoff){
		ArrayList<OldState> newOldStates = new ArrayList<>(oldStates);
		
		while(oldStates.size() > oldStates.indexOf(cutoff)+1){
			newOldStates.add(oldStates.get(oldStates.indexOf(cutoff)+1));
			oldStates.remove(oldStates.get(oldStates.indexOf(cutoff)+1));
		}
		
		return newOldStates;
	}
}
