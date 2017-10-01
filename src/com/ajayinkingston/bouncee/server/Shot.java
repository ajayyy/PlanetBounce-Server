package com.ajayinkingston.bouncee.server;

public class Shot {
	long frame;
	float projectileangle;
	
	Player player;
	public Shot(Player player, float projectileangle, long frame){
		this.player = player;
		this.frame = frame;
		this.projectileangle = projectileangle;
	}
}
