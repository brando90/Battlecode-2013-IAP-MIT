package NewEncampPlayer;

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
				System.out.println("merp");
				encamp = rc.readBroadcast(reader);
				reader += 1;
			}

			if(valid(encamp) || encamp == 0){
				System.out.println("hi");
				rdv = rc.senseEnemyHQLocation();
			}
			else{
                //System.out.println("moose");
				
				captureEncamp = true;
				rdv = intToMapLocation(encamp);
				int update_encamp = mapLocationToInt(rdv);
				rc.broadcast(reader-1, update_encamp);

			}



		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}                    

	}

	public void run() throws GameActionException{
		if (rc.isActive()){

			rc.setIndicatorString(0, rdv.x + "," + rdv.y);

			if(captureEncamp == true){
				int update_encamp = mapLocationToInt(rdv);
				rc.broadcast(reader-1, update_encamp);
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

		if(val < 0)
			val = val * -1;
		
		int roundnum = val/1000000;
		
		if(roundnum + 1000 == Clock.getRoundNum()){
			System.out.println("entered loop");
			return true;
		}

		return false;	
	}


	//Converts Integer to MapLocation
	public MapLocation intToMapLocation(int input){
        
		if(input < 0){
        	input = input * -1;
        }
        
		int x = input/1000 % 1000;
		int y = input%1000;
		return new MapLocation(x, y);
	}

	private int mapLocationToInt(MapLocation location){
		//1000,000,000
		int time = (Clock.getRoundNum() + 1) - 1000;

		if(time < 0){
			return 1000000*time - 1000*location.x - location.y;
		} else{
			return 1000000*time + 1000*location.x + location.y;
		}
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
