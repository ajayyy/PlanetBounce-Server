package com.ajayinkingston.planets.server;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import com.badlogic.gdx.scenes.scene2d.actions.Remove;
import com.badlogic.gdx.utils.Array;


public class Main extends Canvas implements ClientMessageReceiver, Runnable{
	private static final long serialVersionUID = 7131472967027976118L;

	WebSocketServerMessenger messenger;
	
	int mapsize = 30000;
	Random random = new Random();
	
	int playerStartSize = 50;
	float maxspeed = 500;
	
	int targetfps = 60;
	long timeperupdate = 1000/targetfps,last;
	public double delta;
	
	long lastcap = System.currentTimeMillis();//last variable for capping fps
	long diff = System.currentTimeMillis();
	
	double leftoverdelta; //delta left over after the 40fps
	double futureleftoverdelta; //first this variable is set, then leftoverdelta becomes this by the end of the frame
	double fps = 40;//fps to update at
	
	long fpscount;
	int framestaken;
	
	ArrayList<Movement> movements = new ArrayList<>(); //movements made by clients, added to this list so that they can all be processed in the same frame;
	ArrayList<Shot> shots = new ArrayList<>();
	
	Data data; // where data is saved
	
	//for compatibility purposes, just the variables from data
	public ArrayList<Player> players = new ArrayList<>();
	public Planet[] planets = new Planet[0];
	public ArrayList<Projectile> projectiles = new ArrayList<>();
	
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
	        }
	        x = center.x;
//	        y = center.y;
	        size = center.radius;
	        while(x < mapsize){
	        	float nsize = random.nextInt(200)+100;
	        	x = bufferdist*6-nsize + x;
	//        	y = bufferdist*6-nsize + y;
	        	planetlist.add(new Planet(x,y,nsize));
	        }
	        size = center.radius;
	        x = center.y + bufferdist*3;
	        float ny = y + bufferdist*6;
	        while(x > -mapsize){
	        	float nsize = random.nextInt(200)+100;
	        	x = -bufferdist*6-nsize + x;
	        	planetlist.add(new Planet(x,ny,nsize));
	        }
	        size = center.radius;
	        x = center.y + bufferdist*3;
	//        y += size*6;.
	        while(x < mapsize){
	        	float nsize = random.nextInt(200)+100;
	        	x = bufferdist*6-nsize + x;
	        	planetlist.add(new Planet(x,ny,nsize));
	        }
        }
        
        data = new Data();
        
        data.planets = planetlist.toArray(new Planet[planetlist.size()]);
        
        data.planets = new Planet[1];
        data.planets[0] = planetlist.get(0);
        
		players = data.players;
		planets = data.planets;
		projectiles = data.projectiles;
        
        Thread thread = new Thread(this);
        thread.start();
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
		last = System.currentTimeMillis();
		while(true){
			long now = System.currentTimeMillis();
			long updateLength = now - last;
		    last = now;
//			delta = updateLength / ((double)timeperupdate);
		    delta = updateLength/1000d;
//		    System.out.println(delta+ " delta");
//			if(delta > 0.05) delta = 0.05;
//			if(delta > 1) System.out.println("below 60 FPS");
			framestaken++;
			if(System.currentTimeMillis() - fpscount >= 1000){
				System.out.println(framestaken);
				framestaken = 0;
				fpscount = System.currentTimeMillis();
			}
			
			double fulldelta = delta + leftoverdelta;
			for(double i=fulldelta;i>=1/fps;i-=1/fps){
				update(1/fps, false);
			}
			futureleftoverdelta = fulldelta % (1/fps);

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
			
			//cap at 60 fps, should it be like this on the clients too, or instead cap at 50 or 40 fps (currently relying on lib-gdx to do the capping client side)
			//above comment is obsolete, but it does waste precious resources to not cap framerate
			diff = System.currentTimeMillis() - lastcap;
			long targetDelay = 1000/60;
			if (diff < targetDelay) {
				try {
					Thread.sleep(targetDelay - diff);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lastcap = System.currentTimeMillis();
			
			leftoverdelta = futureleftoverdelta;
		}
	}
	
	public void update(double delta, boolean simulation){
		//update the variables (since they are actually properly stored in data)
		players = data.players;
		planets = data.planets;
		projectiles = data.projectiles;
		
		for(Projectile projectile: new ArrayList<>(projectiles)){
			if(projectile.dead){
				if(projectile.frame - projectile.deadframe > 200){
					projectiles.remove(projectile);
				}else{
					projectile.frame++;
				}
			}else{
				projectile.update(data, delta);//
				if(System.currentTimeMillis() - projectile.start > 4500 || isTouchingPlanet(projectile, getClosestPlanet(projectile, planets))){
					projectile.dead = true;
					projectile.deadframe = projectile.frame-1;
				}
			}
		}
		
		for(Player player: new ArrayList<Player>(players)){
			player.update(data, delta);
			player.sendDataToClient(this, data);
		}
		
		if(!simulation){
			for(Movement movement: new ArrayList<>(movements)){
				if(handleMovement(movement.player, movement.disabled, movement.direction, movement.frame)) movements.remove(movement);
			}
			
			for(Shot shot: new ArrayList<>(shots)){
				if(handleShot(shot.player, shot.projectileangle, shot.frame)) shots.remove(shot);
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
			if(projectile.dead) continue;
			for(Player player: players){
				if(player.collided(projectile)){
					//collided
					affectColidedPlayers(player,projectile);
//					break; //one player should only collide with one thing per frame (other wise some weird issues arrive)
				}
			}
		}
	}
	
	public void affectColidedPlayers(Entity player1, Entity player2) { //once the players are collided, this function will deal with them
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
	    
	    player1.x += player1xspeed * 1/fps;
	    player1.y += player1yspeed * 1/fps;
	    player2.x += player2xspeed * 1/fps;
	    player2.y += player2yspeed * 1/fps;
	    
//	    if(player1 instanceof Projectile){
//	    	if(((Player) player2).collided(player1)){
//	    		projectiles.remove(player1);
//	    	}
//	    }else if(player2 instanceof Projectile){
//	    	if(((Player) player1).collided(player2)){
//	    		projectiles.remove(player2);
//	    	}
//	    }
	}
	
	public boolean handleMovement(Player player, boolean disable, boolean direction, long frame) { //returns true if dealt with
		long currentFrame = player.frames;
//		if(disable && direction == 1) frame += player.rightstart;
		
		if(frame > currentFrame){
			return false; //returns false to be called later when this movement actually happens
		}
		
//		OldState originalState = getOldStateAtFrame(new ArrayList<>(player.oldStates), frame);
//		if(originalState == null){
//			originalState = new OldState(player.x, player.y, player.xspeed, player.yspeed, currentFrame, player.left, player.right, false, 0);
//			System.out.println("--__--");
//		}
		
		//count the difference
//		int amountremoved = player.oldStates.size() - (player.oldStates.indexOf(originalState) + 1);
		long amountremoved = currentFrame - frame;
		if(frame == currentFrame) amountremoved = 0;
//		int index = player.oldStates.indexOf(originalState);
//		if(index==-1) index = 0;
//		ArrayList<OldState> oldOldStates = new ArrayList<>();
//		ArrayList<OldState> oldStates = new ArrayList<>(player.oldStates);
//		for(int i=0;i<amountremoved;i++){//remove all of the future ones
//			oldOldStates.add(oldStates.get(index));
//			oldStates.remove(index);
//		}
//		player.oldStates = oldStates;
		
		//make projectile and player oldOldState variables
		ArrayList<ArrayList<OldState>> playerOldOldStates = new ArrayList<>();
		ArrayList<ArrayList<OldState>> projectileOldOldStates = new ArrayList<>();
		
		//check for any projectiles created after this frame
		for(Projectile projectile: new ArrayList<>(projectiles)){
			if(projectile.frame < amountremoved){//because amount removed would be the amount of frames that have happened since, if this was created on that frame, then the frame - amount removed would be 0
				projectiles.remove(projectile);
			}
		}
		
		System.out.println((currentFrame - frame) + " asaasfasasdasfliuioelpo");
		
		//set all projectiles to proper values
		for(Projectile projectile: projectiles){
			OldState state = getOldStateAtFrame(projectile.oldstates, projectile.frame - (currentFrame - frame));
			projectile.x = state.x;
			projectile.y = state.y;
			projectile.xspeed = state.xspeed;
			projectile.yspeed = state.yspeed;
			projectile.frame = state.frame;
			if(projectile.dead && state.frame <= projectile.deadframe){
				projectile.dead = false;
			}
			
			projectile.oldstates = removeFutureOldStatesFromOldState(projectile.oldstates, state);
//					projectileOldOldStates.add(getOldStatesAfterOldState(projectile.oldstates, state));
		}
		
		//set all players to proper values
		for(Player player2: players){
			OldState state = getOldStateAtFrame(player2.oldStates, player2.frames - (currentFrame - frame));
			player2.x = state.x;
			player2.y = state.y;
			player2.xspeed = state.xspeed;
			player2.yspeed = state.yspeed;
			player2.frames = state.frame;
			
			playerOldOldStates.add(getOldStatesAfterOldState(player2.oldStates, state));
			player2.oldStates = removeFutureOldStatesFromOldState(player2.oldStates, state);
		}
		
		//trigger movement
		if(direction){
			player.right = !disable;
		}else{
			player.left = !disable;
		}
		
		player.move(data, 1/fps);
		
		ArrayList<Player> nonSpawnedPlayers = new ArrayList<>();
		
		for(Player player2: new ArrayList<>(players)){
			if(player2.frames < amountremoved){//because amount removed would be the amount of frames that have happened since, if this was created on that frame, then the frame - amount removed would be 0
				nonSpawnedPlayers.add(player2);
				players.remove(player2);
			}
		}
		
		//call player.update however many missed frames there were
		player.frames = frame;
//		amountremoved = 0;
		System.out.println("Started moving at frame" + player.frames + " " + amountremoved);
		for(int i=0;i<amountremoved;i++){
			
			for(Player player2: new ArrayList<>(nonSpawnedPlayers)){
				if(player.frames < amountremoved-i){
					players.add(player2);
					nonSpawnedPlayers.remove(player2);
				}
			}
			
			//iterate through players to make sure all events from oldstate are recalculated
			for(Player player2: players){
				ArrayList<OldState> oldOldStates = playerOldOldStates.get(players.indexOf(player2));
				if(player2 != player){
					player2.left = oldOldStates.get(i).left;
					player2.right = oldOldStates.get(i).right;
				}
				if(oldOldStates.get(i).shot){
					player2.shoot(oldOldStates.get(i).projectileAngle, projectiles, Projectile.class);
//					player2.shot = true;
//					player2.projectileangle = oldOldStates.get(i).projectileangle;
//					player2.xspeed -= (float) (Math.cos(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
//					player2.yspeed -= (float) (Math.sin(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
//					
//					Projectile addedProjectile1 = new Projectile(player.x + ((player.getSize() + projectilesize/2) * Math.cos(oldOldStates.get(i).projectileangle)), player.y + ((player.getSize() + projectilesize/2) * Math.sin(oldOldStates.get(i).projectileangle)), projectilesize, oldOldStates.get(i).projectileangle, projectileSpeed);
//					projectiles.add(addedProjectile1);
				}
			}
			
			update(1/fps, true);
		}
		
//		player.paused = false;
//		for(int i=0;i<player.pausedStack;i++){
//			player.update(this, 1/fps);
//		}
//		player.pausedStack = 0;
		
		return true;
	}
	
	public boolean handleShot(Player player, float projectileangle, long frame) { //returns true if dealt with
		long currentFrame = player.frames;
		
		//now do the thing
		if(frame > currentFrame){
			// that's ok, the client is probably behind on purpose (still need to fix the bug where the client gets incredibly behind)
//			player.start = (long) (currentTime - time);
			frame = currentFrame;
			System.out.println("aslkjdsalkjasjjjdjdsajasldjdas");
			return false;
		}

//		player.shot = true;
//		player.projectileangle = projectileangle;
		
//		if(frame == currentFrame){
//			System.out.println("asertyuiute456765rf");
//			return true;
//		}else{
//			System.out.println("aefsrtyu7654322sertyuiute456765rf");
//		}
		
		OldState originalState = getOldStateAtFrame(new ArrayList<>(player.oldStates), frame);
		if(originalState == null){
			originalState = new OldState(player.x, player.y, player.xspeed, player.yspeed, currentFrame, player.left, player.right, false, 0);
		}
		
//		//make now like that old state
//		player.x = originalState.x;
//		player.y = originalState.y;
//		player.xspeed = originalState.xspeed;
//		player.yspeed = originalState.yspeed;

		
		//count the difference
		int amountremoved = player.oldStates.size() - (player.oldStates.indexOf(originalState) + 1);
		if(frame == currentFrame) amountremoved = 0;
//		System.out.println("AMOUNT REMOVED " + amountremoved);
//		int index = player.oldStates.indexOf(originalState);
//		if(index==-1) index = 0;
//		ArrayList<OldState> oldOldStates = new ArrayList<>();
//		for(int i=0;i<amountremoved;i++){//remove all of the future ones
//			oldOldStates.add(player.oldStates.get(index));
//			player.oldStates.remove(index);
//		}
		//insert new data
		
		//make projectile and player oldOldState variables
		ArrayList<ArrayList<OldState>> playerOldOldStates = new ArrayList<>();
		ArrayList<ArrayList<OldState>> projectileOldOldStates = new ArrayList<>();
		
		//check for any projectiles created after this frame
		for(Projectile projectile: new ArrayList<>(projectiles)){
			if(projectile.frame < amountremoved){//because amount removed would be the amount of frames that have happened since, if this was created on that frame, then the frame - amount removed would be 0
				projectiles.remove(projectile);
			}
		}
		
		System.out.println((currentFrame - frame) + " asaasfasasdasfliuioelpo");
		
		//set all projectiles to proper values
		for(Projectile projectile: projectiles){
			OldState state = getOldStateAtFrame(projectile.oldstates, projectile.frame - (currentFrame - frame));
			projectile.x = state.x;
			projectile.y = state.y;
			projectile.xspeed = state.xspeed;
			projectile.yspeed = state.yspeed;
			projectile.frame = state.frame;
			if(projectile.dead && state.frame <= projectile.deadframe){
				projectile.dead = false;
			}
			
			projectile.oldstates = removeFutureOldStatesFromOldState(projectile.oldstates, state);
//			projectileOldOldStates.add(getOldStatesAfterOldState(projectile.oldstates, state));
		}
		
		//set all players to proper values
		for(Player player2: players){
			OldState state = getOldStateAtFrame(player2.oldStates, player2.frames - (currentFrame - frame));
			player2.x = state.x;
			player2.y = state.y;
			player2.xspeed = state.xspeed;
			player2.yspeed = state.yspeed;
			player2.frames = state.frame;
			
			playerOldOldStates.add(getOldStatesAfterOldState(player2.oldStates, state));
			player2.oldStates = removeFutureOldStatesFromOldState(player2.oldStates, state);
		}
		
		player.shoot(projectileangle, projectiles, Projectile.class);
		
		ArrayList<Player> nonSpawnedPlayers = new ArrayList<>();
		
		for(Player player2: new ArrayList<>(players)){
			if(player2.frames < amountremoved){//because amount removed would be the amount of frames that have happened since, if this was created on that frame, then the frame - amount removed would be 0
				nonSpawnedPlayers.add(player2);
				players.remove(player2);
			}
		}
		
		//change xspeeds
//		player.xspeed -= (float) (Math.cos(projectileangle) * projectileSpeedChange);
//		player.yspeed -= (float) (Math.sin(projectileangle) * projectileSpeedChange);
		
		//set the player shot variable and projectileangle
//		player.shot = true;
//		player.projectileangle = projectileangle;//TODO CALL METHOD
		
		//create projectiles
//		Projectile addedProjectile = new Projectile(player.x + ((player.getSize() + projectilesize/2) * Math.cos(projectileangle)), player.y + ((player.getSize() + projectilesize/2) * Math.sin(projectileangle)), projectilesize, projectileangle, projectileSpeed);
//		projectiles.add(addedProjectile);
		
		//call update however many missed frames there were
		for(int i=0;i<amountremoved;i++){//remove all of the future ones
			System.out.println("isthisevenrunning????");
			
			for(Player player2: new ArrayList<>(nonSpawnedPlayers)){
				if(player.frames < amountremoved-i){
					players.add(player2);
					nonSpawnedPlayers.remove(player2);
				}
			}
			
			//iterate through players to make sure all events from oldstate are recalculated
			for(Player player2: players){
				ArrayList<OldState> oldOldStates = playerOldOldStates.get(players.indexOf(player2));
				player2.left = oldOldStates.get(i).left;
				player2.right = oldOldStates.get(i).right;
				if(oldOldStates.get(i).shot){
					player2.shoot(oldOldStates.get(i).projectileAngle, projectiles, Projectile.class);
//					player2.shot = true;
//					player2.projectileangle = oldOldStates.get(i).projectileangle;
//					player2.xspeed -= (float) (Math.cos(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
//					player2.yspeed -= (float) (Math.sin(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
//					
//					Projectile addedProjectile1 = new Projectile(player.x + ((player.getSize() + projectilesize/2) * Math.cos(oldOldStates.get(i).projectileangle)), player.y + ((player.getSize() + projectilesize/2) * Math.sin(oldOldStates.get(i).projectileangle)), projectilesize, oldOldStates.get(i).projectileangle, projectileSpeed);
//					projectiles.add(addedProjectile1);
				}
			}
			
			update(1/fps, true);
		}
		
//		//call player.update however many missed frames there were
//		double leftoverdelta = 0;
//		for(int i=0;i<amountremoved;i++){//remove all of the future ones
//			player.left = oldOldStates.get(i).left;
//			player.right = oldOldStates.get(i).right;
//			if(oldOldStates.get(i).shot){
//				player.xspeed -= (float) (Math.cos(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
//				player.yspeed -= (float) (Math.sin(oldOldStates.get(i).projectileangle) * projectileSpeedChange);
//			}
//			player.update(this, 1/fps);
//		}
		
//		//call projectile.update for each missing frame too
//		for(int i=0;i<amountremoved;i++){
//			projectile.update(this, 1/fps);
//		}
//
//		//add to old states
//		if(index<player.oldStates.size()-1){
//			player.oldStates.get(index+1).projectileangle = projectileangle;
//			player.oldStates.get(index+1).shot = true;
//		}
		
		return true;
	}
	
	@Override
	public void onMessageRecieved(String message, int id) {
		if(message.split(" ").length<2) return;
		String omessage = message;//original message
		if(message.startsWith("s")){
			
			//click (shoot)
			Player player = getPlayer(id, players);
			if(player==null) return;
			float projectileangle = Float.parseFloat(message.split(" ")[1]);
			long frame = Long.parseLong(message.split(" ")[2]);// when the action happened
			
			shots.add(new Shot(player, projectileangle, frame));
			
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
			Player player = getPlayer(id, players);
			if(player==null) return;
			int direction = Integer.parseInt(message.split(" ")[0]);
			long frame = Long.parseLong(message.split(" ")[1]);// when the action happened
			
			movements.add(new Movement(player, disable, direction > 0, frame));
			
			for(Player player2:players){
				if(player2 != player){
//					System.out.println("hwhsakjsadkj" + player2 + " " + player);
					messenger.sendMessageToClient(player2.id, player.id + " " + omessage);
//					messenger.sendMessageToClient(player2.id, player.id + " " + (Math.cos(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * delta) + " " + (Math.sin(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * delta));
				}
			}
			messenger.sendMessageToClient(id, "rm");//received move
			
			
		}
	}
	
	public static int getIndexOf(Object toSearch, Object[] tab ){
	  for( int i=0; i< tab.length ; i ++ )
	    if( tab[ i ] == toSearch)
	     return i;

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
		System.out.println("Player connected");
		
		Player newplayer = new Player(id, 0, 800, playerStartSize);
		for(Player player:players){
			messenger.sendMessageToClient(player.id, "CONNECTED " + id + " " + newplayer.x + " " + newplayer.y + " " + newplayer.xspeed + " " + newplayer.yspeed + " " + newplayer.mass);
		}
		for(Player player:players){
			messenger.sendMessageToClient(id, "CONNECTED " + player.id + " " + player.x + " " + player.y + " " + player.xspeed + " " + player.yspeed + " " + player.mass);
		}
		for(int i=0;i<data.planets.length;i++){
			System.out.println("sent data for planet");
			messenger.sendMessageToClient(id, "PLANET " + data.planets[i].x + " " + data.planets[i].y + " " + data.planets[i].radius + " " + data.planets[i].food.length);
			for(int s=0;s<data.planets[i].food.length;s++){
				messenger.sendMessageToClient(id, "FOOD " + i + " " + s + " " + data.planets[i].food[s].enabled + " " + data.planets[i].food[s].angle + " " + data.planets[i].food[s].getAmount());
			}
		}
		messenger.sendMessageToClient(id, "START");
		players.add(newplayer);
	}

	@Override
	public void onDisconnected(int id) {
		players.remove(getPlayer(id, players));
		for(Player player: players){
			messenger.sendMessageToClient(player.id, "DISCONNECTED " + id);
		}
	}
	
}
