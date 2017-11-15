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
	
}
