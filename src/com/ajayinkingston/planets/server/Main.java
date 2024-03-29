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
        
//        data.planets = new Planet[1];
//        data.planets[0] = planetlist.get(0);
        
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
				for(Player player: new ArrayList<Player>(data.players)){
					g.fillOval((int) (player.x/10 + getWidth()/2), (int) (player.y/10 + 200), (int) (player.getSize()/5), (int) (player.getSize()/5));
				}
				for(Projectile projectile: data.projectiles){
					g.fillOval((int) (projectile.x/10 + getWidth()/2), (int) (projectile.y/10 + 200), (int) (projectile.getSize()/5), (int) (projectile.getSize()/5));
				}
				
				for(int i=0;i<data.planets.length;i++){
					g.fillOval((int) (data.planets[i].x/10-data.planets[i].radius/10 + getWidth()/2), (int) (data.planets[i].y/10-data.planets[i].radius/10 + 200), (int) (data.planets[i].radius/5), (int) (data.planets[i].radius/5));
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
		
		for(Projectile projectile: new ArrayList<>(data.projectiles)){
			if(projectile.dead){
				if(projectile.frame - projectile.deadframe > 200){
					data.projectiles.remove(projectile);
				}else{
					projectile.frame++;
				}
			}else{
				projectile.update(data, delta);//
				if(System.currentTimeMillis() - projectile.start > 4500 || Data.isTouchingPlanet(projectile, Data.getClosestPlanet(projectile, data.planets))){
					projectile.dead = true;
					projectile.deadframe = projectile.frame-1;
				}
			}
		}
		
		for(Player player: new ArrayList<Player>(data.players)){
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
		for(int i=0;i<data.players.size();i++){//for player to player
			for(int s=i+1;s<data.players.size();s++){
				if(data.players.get(i).collided(data.players.get(s))){
					//collided with player
					affectColidedPlayers(data.players.get(i), data.players.get(s));
				}
			}
		}
		//projectile collision
		for(Projectile projectile: new ArrayList<>(data.projectiles)){
			if(projectile.dead) continue;
			for(Player player: data.players){
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
		
		//count the difference
		long amountremoved = currentFrame - frame;
		if(frame == currentFrame) amountremoved = 0;

		//make projectile and player oldOldState variables
		ArrayList<ArrayList<OldState>> playerOldOldStates = new ArrayList<>();
		
		//check for any projectiles created after this frame
		for(Projectile projectile: new ArrayList<>(data.projectiles)){
			if(projectile.frame < amountremoved){//because amount removed would be the amount of frames that have happened since, if this was created on that frame, then the frame - amount removed would be 0
				data.projectiles.remove(projectile);
			}
		}
		
		System.out.println((currentFrame - frame) + " asaasfasasdasfliuioelpo");
		
		//set all projectiles to proper values
		for(Projectile projectile: data.projectiles){
			OldState state = Data.getOldStateAtFrame(projectile.oldstates, projectile.frame - (currentFrame - frame));
			projectile.x = state.x;
			projectile.y = state.y;
			projectile.xspeed = state.xspeed;
			projectile.yspeed = state.yspeed;
			projectile.frame = state.frame;
			if(projectile.dead && state.frame <= projectile.deadframe){
				projectile.dead = false;
			}
			
			projectile.oldstates = Data.removeFutureOldStatesFromOldState(projectile.oldstates, state);
		}
		
		//set all players to proper values
		for(Player player2: data.players){
			OldState state = Data.getOldStateAtFrame(player2.oldStates, player2.frames - (currentFrame - frame));
			player2.x = state.x;
			player2.y = state.y;
			player2.xspeed = state.xspeed;
			player2.yspeed = state.yspeed;
			player2.frames = state.frame;
			
			playerOldOldStates.add(Data.getOldStatesAfterOldState(player2.oldStates, state));
			player2.oldStates = Data.removeFutureOldStatesFromOldState(player2.oldStates, state);
		}
		
		//trigger movement
		if(direction){
			player.right = !disable;
		}else{
			player.left = !disable;
		}
		
		ArrayList<Player> nonSpawnedPlayers = new ArrayList<>();
		
		for(Player player2: new ArrayList<>(data.players)){
			if(player2.frames < amountremoved){//because amount removed would be the amount of frames that have happened since, if this was created on that frame, then the frame - amount removed would be 0
				nonSpawnedPlayers.add(player2);
				data.players.remove(player2);
			}
		}
		
		//call player.update however many missed frames there were
		player.frames = frame;
//		System.out.println("Started moving at frame" + player.frames + " " + amountremoved);
		for(int i=0;i<amountremoved;i++){
			
			for(Player player2: new ArrayList<>(nonSpawnedPlayers)){
				if(player.frames < amountremoved-i){
					data.players.add(player2);
					nonSpawnedPlayers.remove(player2);
				}
			}
			
			//iterate through players to make sure all events from oldstate are recalculated
			for(Player player2: data.players){
				ArrayList<OldState> oldOldStates = playerOldOldStates.get(data.players.indexOf(player2));
				if(player2 != player){
					player2.left = oldOldStates.get(i).left;
					player2.right = oldOldStates.get(i).right;
				}
				if(oldOldStates.get(i).shot){
					player2.shoot(oldOldStates.get(i).projectileAngle, data.projectiles, Projectile.class);
				}
			}
			
			update(1/fps, true);
		}
		
		return true;
	}
	
	public boolean handleShot(Player player, float projectileangle, long frame) { //returns true if dealt with
		long currentFrame = player.frames;
		
		//now do the thing
		if(frame > currentFrame){
			// that's ok, the client is probably behind on purpose (still need to fix the bug where the client gets incredibly behind)
			System.out.println("aslkjdsalkjasjjjdjdsajasldjdas");
			return false;
		}

		//count the difference
		long amountremoved = currentFrame - frame;
		if(frame == currentFrame) amountremoved = 0;
		
		//make projectile and player oldOldState variables
		ArrayList<ArrayList<OldState>> playerOldOldStates = new ArrayList<>();
		
		//check for any projectiles created after this frame
		for(Projectile projectile: new ArrayList<>(data.projectiles)){
			if(projectile.frame < amountremoved){//because amount removed would be the amount of frames that have happened since, if this was created on that frame, then the frame - amount removed would be 0
				data.projectiles.remove(projectile);
			}
		}
		
		System.out.println((currentFrame - frame) + " asaasfasasdasfliuioelpo");
		
		//set all projectiles to proper values
		for(Projectile projectile: data.projectiles){
			OldState state = Data.getOldStateAtFrame(projectile.oldstates, projectile.frame - (currentFrame - frame));
			projectile.x = state.x;
			projectile.y = state.y;
			projectile.xspeed = state.xspeed;
			projectile.yspeed = state.yspeed;
			projectile.frame = state.frame;
			if(projectile.dead && state.frame <= projectile.deadframe){
				projectile.dead = false;
			}
			
			projectile.oldstates = Data.removeFutureOldStatesFromOldState(projectile.oldstates, state);
		}
		
		//set all players to proper values
		for(Player player2: data.players){
			OldState state = Data.getOldStateAtFrame(player2.oldStates, player2.frames - (currentFrame - frame));
			player2.x = state.x;
			player2.y = state.y;
			player2.xspeed = state.xspeed;
			player2.yspeed = state.yspeed;
			player2.frames = state.frame;
			
			playerOldOldStates.add(Data.getOldStatesAfterOldState(player2.oldStates, state));
			player2.oldStates = Data.removeFutureOldStatesFromOldState(player2.oldStates, state);
		}
		
		player.shoot(projectileangle, data.projectiles, Projectile.class);
		
		ArrayList<Player> nonSpawnedPlayers = new ArrayList<>();
		
		for(Player player2: new ArrayList<>(data.players)){
			if(player2.frames < amountremoved){//because amount removed would be the amount of frames that have happened since, if this was created on that frame, then the frame - amount removed would be 0
				nonSpawnedPlayers.add(player2);
				data.players.remove(player2);
			}
		}
		
		player.frames = frame;
		
		//call update however many missed frames there were
		for(int i=0;i<amountremoved;i++){//remove all of the future ones
			System.out.println("isthisevenrunning????");
			
			for(Player player2: new ArrayList<>(nonSpawnedPlayers)){
				if(player.frames < amountremoved-i){
					data.players.add(player2);
					nonSpawnedPlayers.remove(player2);
				}
			}
			
			//iterate through players to make sure all events from oldstate are recalculated
			for(Player player2: data.players){
				ArrayList<OldState> oldOldStates = playerOldOldStates.get(data.players.indexOf(player2));
				player2.left = oldOldStates.get(i).left;
				player2.right = oldOldStates.get(i).right;
				if(oldOldStates.get(i).shot){
					player2.shoot(oldOldStates.get(i).projectileAngle, data.projectiles, Projectile.class);
				}
			}
			
			update(1/fps, true);
		}
		
		return true;
	}
	
	@Override
	public void onMessageRecieved(String message, int id) {
		if(message.split(" ").length<2) return;
		String omessage = message;//original message
		if(message.startsWith("s")){
			
			//click (shoot)
			Player player = Data.getPlayer(id, data.players);
			if(player==null) return;
			float projectileangle = Float.parseFloat(message.split(" ")[1]);
			long frame = Long.parseLong(message.split(" ")[2]);// when the action happened
			
			shots.add(new Shot(player, projectileangle, frame));
			
			for(Player player2:data.players){
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
			Player player = Data.getPlayer(id, data.players);
			if(player==null) return;
			int direction = Integer.parseInt(message.split(" ")[0]);
			long frame = Long.parseLong(message.split(" ")[1]);// when the action happened
			
			System.out.println("RECIEVED MOVEMENT FROM PLAYER " + id + " " + message);
			
			movements.add(new Movement(player, disable, direction > 0, frame));
			
			for(Player player2:data.players){
				if(player2 != player){
//					System.out.println("hwhsakjsadkj" + player2 + " " + player);
					messenger.sendMessageToClient(player2.id, player.id + " " + omessage);
//					messenger.sendMessageToClient(player2.id, player.id + " " + (Math.cos(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * delta) + " " + (Math.sin(getClosestAngle(player)+Math.toRadians(direction*90)) * speed * delta));
				}
			}
			messenger.sendMessageToClient(id, "rm");//received move
			
			
		}
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
		
		Player newPlayer = new Player(id, 0, 800, playerStartSize);
		for(Player player: data.players){
			messenger.sendMessageToClient(player.id, "CONNECTED " + id + " " + newPlayer.x + " " + newPlayer.y + " " + newPlayer.xspeed + " " + newPlayer.yspeed + " " + newPlayer.mass + " " + newPlayer.frames + " " + player.frames);
		}
		for(Player player: data.players){
			messenger.sendMessageToClient(id, "CONNECTED " + player.id + " " + player.x + " " + player.y + " " + player.xspeed + " " + player.yspeed + " " + player.mass + " " + player.frames + " " + newPlayer.frames);
		}
		for(int i=0;i<data.planets.length;i++){
			System.out.println("sent data for planet");
			messenger.sendMessageToClient(id, "PLANET " + data.planets[i].x + " " + data.planets[i].y + " " + data.planets[i].radius + " " + data.planets[i].food.length);
			for(int s=0;s<data.planets[i].food.length;s++){
				messenger.sendMessageToClient(id, "FOOD " + i + " " + s + " " + data.planets[i].food[s].enabled + " " + data.planets[i].food[s].angle + " " + data.planets[i].food[s].getAmount());
			}
		}
		messenger.sendMessageToClient(id, "START");
		data.players.add(newPlayer);
	}

	@Override
	public void onDisconnected(int id) {
		data.players.remove(Data.getPlayer(id, data.players));
		for(Player player: data.players){
			messenger.sendMessageToClient(player.id, "DISCONNECTED " + id);
		}
	}
	
}
