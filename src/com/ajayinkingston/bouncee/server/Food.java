package com.ajayinkingston.bouncee.server;

public class Food {
	double angle;//the angle it is located on the planet
	private int amount;//point amount
	boolean enabled; //if someone has collected it or something
	public Food(double angle, int amount){
		this.angle = angle;
		this.amount = amount;
		enabled = true;
	}
	
	public int getAmount(){
		return (int) (amount * 1.5);
	}
	
	public int getSize(){
		return (int) (amount * 4)+70;
	}
}