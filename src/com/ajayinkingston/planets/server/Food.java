package com.ajayinkingston.planets.server;

public class Food {
	public double angle;//the angle it is located on the planet
	public int amount;//point amount
	public boolean enabled; //if someone has collected it or something
	public Food(double angle, int amount){
		this.angle = angle;
		this.amount = amount;
		enabled = true;
	}
	
	public int getAmount(){
		return (int) (amount * 1.5);
	}
	
	public int getSize(){
		return (int) (amount * 4)+50;
	}
}