package com.ajayinkingston.bouncee.server;

public class Movement {
	/**
	 * true is right. false is left
	 */
	boolean direction;
	
	boolean disabled;
	long frame;
	
	Player player;
	
	public Movement(Player player, boolean disabled, boolean direction, long frame){
		this.player = player;
		this.disabled = disabled;
		this.direction = direction;
		this.frame = frame;
	}
}
