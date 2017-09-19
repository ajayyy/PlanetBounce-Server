package com.ajayinkingston.bouncee.server;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;


public class Main extends Canvas implements ClientMessageReceiver, Runnable{
	private static final long serialVersionUID = 7131472967027976118L;

	WebSocketServerMessenger messenger;
	
	ArrayList<Player> players = new ArrayList<>();
	
	Planet[] planets = new Planet[1];
	
	int mapsize = 30000;
	Random random = new Random();
	
	int playerstartsize = 50;
	float speed = 500, maxspeed = 500;
	
	int targetfps = 60;
	long timeperupdate = 1000/targetfps,last;
	public double delta;
	
	long lastcap = System.currentTimeMillis();//last variable for capping fps
	long diff = System.currentTimeMillis();
	
	float projectileSpeedChange = 250;
	float projectileSpeed = 1000;
	int projectilesize = 5;
	ArrayList<Projectile> projectiles = new ArrayList<>();
	
	double leftoverdelta; //delta left over after the 40fps
	double futureleftoverdelta; //first this variable is set, then leftoverdelta becomes this by the end of the frame
	double fps = 40;//fps to update at
	
	long fpscount;
	int framestaken;
	
	public static void main(String[] args){
		new Main();
	}
	
	public Main(){
		messenger = new WebSocketServerMessenger(2492, this);
		
		//generate planets
        ArrayList<Planet> planetlist = new ArrayList<>();
        Planet center = new Planet(0, 0, random.nextInt(300)+200);
        
        planetlist.add(center);
//        planetlist.add(new Planet((int) -center.radius*2, (int)  center.radius*2, random.nextInt(200)+100));
        float x = center.x;
//        float y = center.y;
        float size = center.radius;//TODO SPAWN CENTERS
        float bufferdist = 400;
        for(float y=center.y;y<mapsize;y+=size*12){
	        x = center.x;
	        while(x > -mapsize){
	        	float nsize = random.nextInt(200)+100;
	        	x = -size*6-nsize + x;
	//        	y = size*6-nsize + y;
	        	planetlist.add(new Planet(x,y,nsize));
	        	System.out.println("sdalsadmlkkldsa" + x + " " + mapsize);
	        }
	        x = center.x;
//	        y = center.y;
	        size = center.radius;
	        while(x < mapsize){
	        	float nsize = random.nextInt(200)+100;
	        	x = bufferdist*6-nsize + x;
	//        	y = bufferdist*6-nsize + y;
	        	planetlist.add(new Planet(x,y,nsize));
	        	System.out.println("sdalsadmlkkldsa" + x + " " + mapsize);
	        }
	        size = center.radius;
	        x = center.y + bufferdist*3;
	        float ny = y + bufferdist*6;
	        while(x > -mapsize){
	        	float nsize = random.nextInt(200)+100;
	        	x = -bufferdist*6-nsize + x;
	        	planetlist.add(new Planet(x,ny,nsize));
	        	System.out.println("sdalsadmlkkldsa" + x + " " + mapsize);
	        }
	        size = center.radius;
	        x = center.y + bufferdist*3;
	//        y += size*6;
	        while(x < mapsize){
	        	float nsize = random.nextInt(200)+100;
	        	x = bufferdist*6-nsize + x;
	        	planetlist.add(new Planet(x,ny,nsize));
	        	System.out.println("sdalsadmlkkldsa" + x + " " + mapsize);
	        }
        }
        
        planets = planetlist.toArray(new Planet[planetlist.size()]);
        
        Thread thread = new Thread(this);
        thread.start();
	}
	
	public int getIndexOf(Object toSearch, Object[] tab ){
	  for( int i=0; i< tab.length ; i ++ )
	    if( tab[ i ] == toSearch)
	     return i;

	  return -1;
	}
	
	public ArrayList<Planet> getClosestPlanets(Entity player) {
		ArrayList<Planet> closeplanets = new ArrayList<>();
		Planet closest = null;
		float closestdistance = 0;
		for(int i=0;i<planets.length;i++){
			if(planets[i] == null){
				System.out.println("Planet is null: " + i);
			}
//			if(Math.pow(Math.abs(player.x - planets[i].x), 2) + Math.pow(Math.abs(player.y - planets[i].y), 2) < Math.pow(player.mass/2 + planets[i].radius, 2)){
				//collided
//				double angle = Math.atan2((player.y) - (planets[i].y), (player.x) - (planets[i].x));
////				double angle = Math.atan2((splats.planets[i].y) - y, (splats.planets[i].x) - x);
//
//				player.yspeed = (float) (Math.sin(angle) * planets[i].bounceheight); //TODO BOUNCE HEIGHT A FORMULA FROM GRAVITY TO DIFFER IN EVERY PLANET AND FOR EVERY PLAYER WITH DIFFERENT MASS
//				player.xspeed = (float) (Math.cos(angle) * planets[i].bounceheight);
////				yspeed = -yspeed;
////				System.out.println((Math.cos(angle) * splats.planets[i].gravity / (Math.sqrt(Math.pow((y) - (splats.planets[i].y), 2) + Math.pow((x) - (splats.planets[i].x), 2))) * 400) + " " + (Math.sin(angle) * splats.planets[i].gravity / (Math.sqrt(Math.pow((y) - (splats.planets[i].y), 2) + Math.pow((x) - (splats.planets[i].x), 2))) * 400));
//				closeplanets.add(splats.planets[i]);
//				if(closest == null || Math.pow(Math.abs(x - splats.planets[i].x), 2) + Math.pow(Math.abs(y - splats.planets[i].y), 2) < closestdistance){
//					closest = splats.planets[i];
//					closestdistance = (float) (Math.pow(Math.abs(x - splats.planets[i].x), 2) + Math.pow(Math.abs(y - splats.planets[i].y), 2));
//				}
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
	
	public Planet getClosestPlanet(Entity player){
		ArrayList<Planet> closestplanets = getClosestPlanets(player);
		return closestplanets.get(0);
	}
	
	public double getClosestAngle(Entity player){
		Planet planet = getClosestPlanet(player);
		double angle = Math.atan2((player.y) - (planet.y), (player.x) - (planet.x));
		double closestangle = angle - Math.PI;
		return closestangle;
	}
	
	public boolean isTouchingPlanet(Entity player, Planet planet){
		return Math.pow(Math.abs(player.x - planet.x), 2) + Math.pow(Math.abs(player.y - planet.y), 2) < Math.pow(player.getRadius() + planet.radius, 2);
	}
	
	public double getAngleFromPlanet(Player player, Planet planet){
		double angle = Math.atan2((player.y) - (planet.y), (player.x) - (planet.x));
		double closestangle = angle - Math.PI;
		return closestangle;
	}
	
	@Override
	public void run() {
		lastcap = System.currentTimeMillis();
		
		//debug
		JFrame frame = new JFrame();
		frame.setSize(1000,700);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.setVisible(true);
		while(true){
			long now = System.currentTimeMillis();
			long updateLength = now - last;
		    last = now;
//			delta = updateLength / ((double)timeperupdate);
		    delta = updateLength/1000d;
			if(delta > 0.05) delta = 0.05;
//			if(delta > 1) System.out.println("below 60 FPS");
			framestaken++;
			if(System.currentTimeMillis() - fpscount >= 1000){
				System.out.println(framestaken);
				framestaken = 0;
				fpscount = System.currentTimeMillis();
			}
			
			double fulldelta = delta + leftoverdelta;
			for(double i=fulldelta;i>=1/fps;i-=1/fps){
				update(1/fps);
			}
			futureleftoverdelta = fulldelta%(1/fps);

			BufferStrategy s = getBufferStrategy();
			if(s!=null){
				Graphics2D g = (Graphics2D) s.getDrawGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				for(Player player: new ArrayList<Player>(players)){
					g.fillOval((int) (player.x/10 + getWidth()/2), (int) (player.y/10 + 200), (int) (player.getSize()/5), (int) (player.getSize()/5));
				}
				for(Projectile projectile: projectiles){
					g.fillOval((int) (projectile.x/10 + getWidth()/2), (int) (projectile.y/10 + 200), (int) (projectile.getSize()/5), (int) (projectile.getSize()/5));
				}
				
				for(int i=0;i<planets.length;i++){
					g.fillOval((int) (planets[i].x/10-planets[i].radius/10 + getWidth()/2), (int) (planets[i].y/10-planets[i].radius/10 + 200), (int) (planets[i].radius/5), (int) (planets[i].radius/5));
				}
				
				s.show();
			}else{
				createBufferStrategy(3);
			}
			
			//cap at 60 fps, should it be like this on the clients too, or instead cap at 50 or 40 fps (currently relying on lib-gdx to do the capping client side
//			diff = System.currentTimeMillis() - lastcap;
//			long targetDelay = 1000/60;
//			if (diff < targetDelay) {
//				try {
//					Thread.sleep(targetDelay - diff);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			lastcap = System.currentTimeMillis();
			
			leftoverdelta = futureleftoverdelta;
		}
	}
	
	public void update(double delta){
		for(Player player: new ArrayList<Player>(players)){
			player.update(this, delta);
		}
		
		for(Projectile projectile: new ArrayList<>(projectiles)){
			projectile.update(this, delta);
			if(System.currentTimeMillis() - projectile.start > 4500 || isTouchingPlanet(projectile, getClosestPlanet(projectile))){
				projectiles.remove(projectile);
			}
		}
		
		//collision detection
		for(int i=0;i<players.size();i++){//for player to player
			for(int s=i+1;s<players.size();s++){
				if(players.get(i).collided(players.get(s))){
					//collided with player
					affectColidedPlayers(players.get(i),players.get(s));
				}
			}
		}
		//projectile collision
		for(Projectile projectile: new ArrayList<>(projectiles)){
			for(Player player: players){
				if(player.collided(projectile)){
					//collided
					affectColidedPlayers(player,projectile);
				}
			}
		}
	}
	
	public void affectColidedPlayers(Entity player1, Entity player2){//once the players are collided, this function will deal with them
	    //Calculate speeds
	    
//		float player1xspeed = (player1.xspeed * (player1.getSize() - player1.getSize()) + (2 * playerstartsize * player2.xspeed)) / (playerstartsize + playerstartsize);
//	    float player1yspeed = (player1.yspeed * (player1.getSize() - player1.getSize()) + (2 * playerstartsize * player2.yspeed)) / (playerstartsize + playerstartsize);
//	    float player2xspeed = (player2.xspeed * (player1.getSize() - player1.getSize()) + (2 * playerstartsize * player1.xspeed)) / (playerstartsize + playerstartsize);  ////https://gamedevelopment.tutsplus.com/tutorials/when-worlds-collide-simulating-circle-circle-collisions--gamedev-769
//	    float player2yspeed = (player2.yspeed * (player1.getSize() - player1.getSize()) + (2 * playerstartsize * player1.yspeed)) / (playerstartsize + playerstartsize);
		
	    //simplified to:
	    float player1xspeed = player2.xspeed;
	    float player1yspeed = player2.yspeed;
	    float player2xspeed = player1.xspeed;
	    float player2yspeed = player1.yspeed;
	    
	    //set speeds (to make sure that when calculating it is using the master copy)
	    player1.xspeed = player1xspeed;
	    player1.yspeed = player1yspeed;
	    player2.xspeed = player2xspeed;
	    player2.yspeed = player2yspeed;
	    
	    player1.x += player1xspeed * delta;
	    player1.y += player1yspeed * delta;
	    player2.x += player2xspeed * delta;
	    player2.y += player2yspeed * delta;
	}
	
	@Override
	public void onMessageRecieved(String message, int id) {
		if(message.split(" ").length<2) return;
		String omessage = message;//original message
		if(message.startsWith("s")){//click (shoot)
			Player player = getPlayer(id);
			if(player==null) return;
			float projectileangle = Float.parseFloat(message.split(" ")[1]);
			long currentFrame = player.frames;
			long frame = Long.parseLong(message.split(" ")[2]);// when the action happened
			
			//now do the thing
			if(frame > currentFrame){
				//ok something went wrong here
	//			player.start = (long) (currentTime - time);
				frame = currentFrame;
				System.err.println("HOUSTON, WE HAVE A HACKED CLIENT" + frame); //for now just do this, but in the future just wait until the frame is hit
	//			return;
			}
			
			player.paused = true;
			
			double frameDifference = currentFrame - frame;//difference between now, and when it was pressed
			
			OldState originalState = getOldStateAtFrame(new ArrayList<>(player.oldStates), frame);
			if(originalState == null){
				originalState = new OldState(player.x, player.y, player.xspeed, player.yspeed, currentFrame, player.left, player.right);
			}
			
			//make now like that old state
			player.x = originalState.x;
			player.y = originalState.y;
			player.xspeed = originalState.xspeed;
			player.yspeed = originalState.yspeed;
	
			
			//count the difference
			int amountremoved = player.oldStates.size() - (player.oldStates.indexOf(originalState) + 1);
			System.out.println("AMOUNT REMOVED " + amountremoved);
			int index = player.oldStates.indexOf(originalState);
			if(index==-1) index = 0;
			ArrayList<OldState> oldOldStates = new ArrayList<>();
			for(int i=0;i<amountremoved;i++){//remove all of the future ones
				oldOldStates.add(player.oldStates.get(index));
				player.oldStates.remove(index);
			}
			//insert new data
			
			//change xspeeds
			player.xspeed -= (float) (Math.cos(projectileangle) * projectileSpeedChange);
			player.yspeed -= (float) (Math.sin(projectileangle) * projectileSpeedChange);
			
			//create projectiles
			Projectile projectile = new Projectile(player.x + ((player.getSize() + projectilesize/2) * Math.cos(projectileangle)), player.y + ((player.getSize() + projectilesize/2) * Math.sin(projectileangle)), projectilesize, projectileangle, projectileSpeed);
			projectiles.add(projectile);

			//call player.update however many missed frames there were
			double leftoverdelta = 0;
			for(int i=0;i<amountremoved;i++){//remove all of the future ones
				player.left = oldOldStates.get(i).left;
				player.right = oldOldStates.get(i).right;
				if(oldOldStates.get(i).shot){
					player.xspeed -= (float) (Math.cos(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
					player.yspeed -= (float) (Math.sin(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
				}
				player.update(this, 1/fps);
			}
			
			//call projectile.update for each missing frame too
			for(int i=0;i<amountremoved;i++){
				projectile.update(this, 1/60.0);
			}

			//add to old states
			if(index<player.oldStates.size()-1){
				player.oldStates.get(index+1).projectileangle = projectileangle;
				player.oldStates.get(index+1).shot = true;
			}
			
//			System.out.println(timeDifference);
			
			player.paused = false;
			//replay all old frames
//			for(int i=0;i<player.pausedStack;i++){
//				player.update(this, player.pausedDeltaTimes.get(i));
//			}
			
			for(Player player2:players){
				if(player2 != player){
					messenger.sendMessageToClient(player2.id, "s " + player.id + " " + omessage);
				}
			}
			
			messenger.sendMessageToClient(id, "rs");
		}else{//left or right
			boolean disable = false;
			if(message.startsWith("d")){
				disable = true;
				message = message.substring(1);
			}
			Player player = getPlayer(id);
			if(player==null) return;
			int direction = Integer.parseInt(message.split(" ")[0]);
	//		double delta = Double.parseDouble(message.split(" ")[1]);// delta (how long frame took for client) should probably change to specific time so server 
			long currentFrame = player.frames;
			long frame = Long.parseLong(message.split(" ")[1]);// when the action happened
			long existingframes = frame;
//			if(disable && direction == 1) frame += player.rightstart;
			
			
			if(frame > currentFrame){
	//			player.start = (long) (currentTime - time);
				frame = currentFrame;  //todo make this actually wait and save this move into cue
	//			return;
			}
			player.paused = true;
			
			OldState originalState = getOldStateAtFrame(new ArrayList<>(player.oldStates), frame);
			if(originalState == null){
				originalState = new OldState(player.x, player.y, player.xspeed, player.yspeed, currentFrame, player.left, player.right);
			}
			
			//make now like that old state
//			player.x = originalState.x;
//			player.y = originalState.y;
//			player.xspeed = originalState.xspeed;
//			player.yspeed = originalState.yspeed;
	
			
			//count the difference
//			int amountremoved = player.oldStates.size() - (player.oldStates.indexOf(originalState) + 1);
			long amountremoved = currentFrame - frame;
			int index = player.oldStates.indexOf(originalState);
			if(index==-1) index = 0;
			ArrayList<OldState> oldOldStates = new ArrayList<>();
			ArrayList<OldState> oldStates = new ArrayList<>(player.oldStates);
			for(int i=0;i<amountremoved;i++){//remove all of the future ones
				oldOldStates.add(oldStates.get(index));
				oldStates.remove(index);
			}
			player.oldStates = oldStates;
			//insert new data
			boolean rightchange = false;
			boolean leftchange = false;
			if(direction>0){
				player.right = !disable;
				rightchange = true;
			}else{
				player.left = !disable;
				leftchange = true;
			}
			//call player.update however many missed frames there were
			player.frames = frame;
			amountremoved = 0;
			for(int i=0;i<amountremoved;i++){//remove all of the future ones
				if(!leftchange){
					player.left = oldOldStates.get(i).left;
				}
				if(!rightchange){
					player.right = oldOldStates.get(i).right;
				}
				if(oldOldStates.get(i).shot){
					player.xspeed -= (float) (Math.cos(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
					player.yspeed -= (float) (Math.sin(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
				}
				
				player.update(this, 1/fps);
			}
			
			player.paused = false;
			for(int i=0;i<player.pausedStack;i++){
				player.update(this, 1/fps);
			}
			player.pausedStack = 0;
			
//			System.out.println(timeDifference);
			
	//		if(!disable){//ignore delta for now when disable true
	//			player.xspeed += Math.cos(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * timeDifference;
	//			player.yspeed += Math.sin(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * timeDifference;
	//			System.out.println(getClosestAngle(player));	
	//		}else{
	//			//if disabled TODO MAKE THIS WORK
	//			player.xspeed -= Math.cos(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * timeDifference;
	//			player.yspeed -= Math.sin(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * timeDifference;
	//		}
			
			for(Player player2:players){
				if(player2 != player){
					System.out.println("hwhsakjsadkj" + player2 + " " + player);
					messenger.sendMessageToClient(player2.id, player.id + " " + omessage);
	//				messenger.sendMessageToClient(player2.id, player.id + " " + (Math.cos(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * delta) + " " + (Math.sin(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * delta));
				}
			}
			messenger.sendMessageToClient(id, "rm");//received move
			
	//		for(Player player1: players){
	//			if(player1==player) break;
	//			if(Math.pow(Math.abs(player.x - player1.x), 2) + Math.pow(Math.abs(player.y - player1.y), 2) < Math.pow(player.mass/2 + player.mass/2, 2)){
	//				
	//				float xspeed1 = 0;
	//				float xspeed2 = 0;
	//				float yspeed1 = 0;
	//				float yspeed2 = 0;
	//				
	//				if(player.x>player1.x){
	//					xspeed1 = player.xspeed+player1.xspeed;
	//					xspeed2 = -(player.xspeed+player1.xspeed);
	////					player.x = player1.x+player.mass/2;
	//				}else{
	//					xspeed1 = -(player.xspeed+player1.xspeed);
	//					xspeed2 = player.xspeed+player1.xspeed;
	////					player1.x = player1.x+player.mass/2;
	//				}
	//				if(player.y>player1.y){
	//					yspeed1 = player.yspeed+player1.yspeed;
	//					yspeed2 = -(player.yspeed+player1.yspeed);
	////					player.y = player1.y+player.mass/2;
	//				}else{
	//					yspeed1 = -(player.yspeed+player1.yspeed);
	//					yspeed2 = player.yspeed+player1.yspeed;
	////					player1.y = player1.y+player.mass/2;
	//				}
	//				
	////				player.x+=player.xspeed*0.1f;
	////				player.y+=player.yspeed*0.1f;
	////				player1.x+=player1.xspeed*0.1f;
	////				player1.y+=player1.yspeed*0.1f;
	//				
	//				messenger.sendMessageToClient(player.id, "SPEEDCHANGE " + xspeed1 + " " + yspeed1);
	//				messenger.sendMessageToClient(player1.id, "SPEEDCHANGE " + xspeed2 + " " + yspeed2);
	//				messenger.sendMessageToClient(player.id, "POSCHANGE " + player.x + " " + player.y);
	//				messenger.sendMessageToClient(player1.id, "POSCHANGE " + player1.x + " " + player1.y);
	//			}
	//		}
		}
	}
	
	public Player getPlayer(int id){
		for(Player player:players){
			if(player.id == id){
				return player;
			}
		}
		System.out.println("Player doesn't exist when getting player");
		return null;
	}

	public OldState getOldStateAtFrame(ArrayList<OldState> oldStates, long frame){
		OldState atFrame = null;
		oldStates = new ArrayList<>(oldStates);
		for(OldState oldState: oldStates){
			if(oldState.frame == frame) atFrame = oldState;
		}
		if(atFrame == null) atFrame = oldStates.get(oldStates.size()-1);
		return atFrame;
	}
	
	/*public OldState getOldStateAtTime(ArrayList<OldState> oldStates, long time){
		long smallest = System.currentTimeMillis();
		OldState smallestOldState = null;
		for(OldState oldState: oldStates){
			System.out.println("Asdsadsadadssadsadsadadssad");
			long difference = Math.abs(time - oldState.when);
			if(difference < smallest){
				smallest = difference;
				smallestOldState = oldState;
			}
		}
		if(smallestOldState == null){
			if(!oldStates.isEmpty()){
//				smallestOldState = new OldState(0,0,0,0,0,false,false);
//			}else{
				smallestOldState = oldStates.get(0);
			}
		}
		return smallestOldState;
	}*/

	@Override
	public void onConnected(int id) {
		Player newplayer = new Player(id, 0, 800, playerstartsize);
		for(Player player:players){
			messenger.sendMessageToClient(player.id, "CONNECTED " + id + " " + newplayer.x + " " + newplayer.y + " " + newplayer.xspeed + " " + newplayer.yspeed + " " + newplayer.mass);
		}
		for(Player player:players){
			messenger.sendMessageToClient(id, "CONNECTED " + player.id + " " + player.x + " " + player.y + " " + player.xspeed + " " + player.yspeed + " " + player.mass);
		}
		for(int i=0;i<planets.length;i++){
			messenger.sendMessageToClient(id, "PLANET " + planets[i].x + " " + planets[i].y + " " + planets[i].radius + " " + planets[i].food.length);
			for(int s=0;s<planets[i].food.length;s++){
				messenger.sendMessageToClient(id, "FOOD " + i + " " + s + " " + planets[i].food[s].enabled + " " + planets[i].food[s].angle + " " + planets[i].food[s].getAmount());
			}
		}
		messenger.sendMessageToClient(id, "START");
		players.add(newplayer);
	}

	@Override
	public void onDisconnected(int id) {
		players.remove(getPlayer(id));
		for(Player player:players){
			messenger.sendMessageToClient(player.id, "DISCONNECTED " + id);
		}
	}
	
	//util methods
	public double getDotProduct(double x1, double y1, double x2, double y2){
		return x1 * x2 + y1 * y2;
	}

}
