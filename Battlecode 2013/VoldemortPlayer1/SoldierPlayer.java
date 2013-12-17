package VoldemortPlayer1;

//import java.util.HashSet;
//import java.util.Set;

import battlecode.common.*;

public class SoldierPlayer extends BasePlayer {
	NavigationController nav; //Do not create instances of NavigationControllers inside of run
	MapLocation rdv;

	
	private boolean captureEncamp = false;
	private int reader = base_channel;


	public SoldierPlayer(RobotController rc) {
		super(rc);
		this.nav = new NavigationController(rc);

	
		try {
		
			int encamp = rc.readBroadcast(reader);
			
			while(encamp != 0 && valid(encamp)){
				reader += 1;
				encamp = rc.readBroadcast(reader);
			}
			
			if(valid(encamp))
				rdv = rc.senseEnemyHQLocation();
			else{
				captureEncamp = true;
				rdv = intToMapLocation(encamp);
				
			}
			
			
			
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}                    

	}

	public void run() throws GameActionException{
		if (rc.isActive()){
			
			rc.setIndicatorString(0, Integer.toString(rdv.x) + "," + Integer.toString(rdv.y));
			
			if(captureEncamp == true){
				int update_encamp = mapLocationToInt(rdv);
				rc.broadcast(reader, update_encamp);
			}
			
			
             if(rc.getLocation().equals(rdv)){
            	 rc.captureEncampment(RobotType.GENERATOR);
            	 //broadcastEnc = true;
             }
	
			nav.progressMove(rdv);
				
			}
		
		
	}
		
	
	//Checks if round number is valid
	private boolean valid(int val){
		String temp = Integer.toString(val);
		System.out.println(temp);
		int roundnum = Integer.parseInt(temp.substring(0, temp.length()-6));
		
		if(roundnum == 1000)
			return true;
		else if(roundnum + 1000 == Clock.getRoundNum())
			return true;
		
		return false;	
	}
	
	
	
	//Converts Integer to MapLocation
	public MapLocation intToMapLocation(int input){
		String str = Integer.toString(input);
		int x = Integer.parseInt(str.substring(str.length()-6, str.length()-3));
		int y = Integer.parseInt(str.substring(str.length()-3, str.length()));
		return new MapLocation(x, y);
	}
	
    
	
	
	private int mapLocationToInt(MapLocation location){
		//1000,000,000
		int time = Clock.getRoundNum() - 1000;
		
		if(Clock.getRoundNum() == 1000)
			time = 1000;
	
		String x_val = "000" + location.x;
		String y_val = "000" + location.y;

		String ans = Integer.toString(time) + x_val.substring(x_val.length()-3, x_val.length()) + y_val.substring(y_val.length()-3, y_val.length());
		return Integer.parseInt(ans);


	}
	
	
	/** ====================================================================================== **/

	private void swarmAround(MapLocation destination) throws GameActionException{
		nav.progressMove(destination);
	}

	private void moveOrDefuse(Direction dir) throws GameActionException{
		MapLocation ahead = rc.getLocation().add(dir);
		if(rc.senseMine(ahead)!= null && rc.senseMine(ahead) != rc.getTeam()){
			rc.defuseMine(ahead);
		}else{
			if(rc.canMove(dir)){
				rc.move(dir);     
			}
		}
	}

}
