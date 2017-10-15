package com.ajayinkingston.planets.server;

import java.util.Random;

public class Planet {
	float x,y,radius;
	
	float gravityhelperconstant = -3000;//constant that helps calculate gravity (name has been fixed :) )
	
	float bounceheight;
	
	Food[] food;
	public Planet(float x, float y, float radius){
		this.x = x;
		this.y = y;
		this.radius = radius;
		
		double actualgravity = gravityhelperconstant / 300 * 350;
		
		if(radius>1500) radius = 1500; //only sets temp variable
		bounceheight = -(float) (actualgravity * ((-0.00000020563305192)*Math.pow(radius - 1191.62314342, 2) + 0.51676174503));//XXX IF YOU CHANGE THIS THEN MAKE SURE TO CHANGE IT CLIENT SIDE TOO

		//only use this linear one if were small (on reeeally big planets, we dont want 94% bounce rate
		if(radius<600) bounceheight = -(float) (actualgravity * (0.00019941857907*radius + 0.320988385581));//XXX IF YOU CHANGE THIS THEN MAKE SURE TO CHANGE IT CLIENT SIDE TOO
		
		//on even smaller planets, use a different calculation
		if(radius<400) bounceheight = -(float) (actualgravity * (0.000233139509302*radius + 0.292662804186));//XXX IF YOU CHANGE THIS THEN MAKE SURE TO CHANGE IT CLIENT SIDE TOO
		
		
		Random rand = new Random();
//		food = new Food[rand.nextInt(7)+1];
		food = new Food[0];
		for(int i=0;i<food.length;i++){
			food[i] = new Food(rand.nextFloat()*(Math.PI*2), 5);
		}
	}
}
