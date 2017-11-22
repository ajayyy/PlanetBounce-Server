package com.ajayinkingston.planets.server;

public class Shot {
	public long frame;
	public float projectileangle;
	
	public Player player;
	public Shot(Player player, float projectileangle, long frame){
		this.player = player;
		this.frame = frame;
		this.projectileangle = projectileangle;
	}
}
