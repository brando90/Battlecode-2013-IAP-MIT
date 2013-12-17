package team260_seedingTournament;

//import java.util.HashSet;
//import java.util.Set;

import battlecode.common.*;

public class SoldierPlayer extends BasePlayer {
	NavigationController nav; //Do not create instances of NavigationControllers inside of run
	MapLocation rdv;
	int roundToAttackEnemyHQ = 900;
	//Artillery 

	public SoldierPlayer(RobotController rc) {
		super(rc);
		this.nav = new NavigationController(rc);


		MapLocation encampment;
		try {
			int BC = rc.readBroadcast(base_channel);
			//rc.setIndicatorString(0, "Tuning into Base Channel: "+BC);
			encampment = getTargetEncampmentAtChannel(BC);



			if (encampment != null ){
				rdv = encampment;
				//rc.setIndicatorString(1, "Heading to Enc: "+encampment.toString());
			}else if (Clock.getRoundNum() > roundToAttackEnemyHQ){
				rdv = rc.senseEnemyHQLocation();
			}else{
				rdv = getRV();
			}

		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}                    

	}

	private MapLocation getRV() {
		MapLocation enemyHQLoc = rc.senseEnemyHQLocation();
		int rendezvousX = (3*rc.senseHQLocation().x + enemyHQLoc.x)/4;
		int rendezvousY = (3*rc.senseHQLocation().y + enemyHQLoc.y)/4;
		return new MapLocation(rendezvousX,rendezvousY);
	}

	public void run() throws GameActionException{
		if (rc.isActive()){
			//Use awesome Nav
			//if (rc.senseEncampmentSquare(rc.getLocation())){
			if(rc.getLocation().equals(rdv)){
				try{
					rc.captureEncampment(getNextEncampmentType());
				} catch (GameActionException e){

				}
			} else{
				if (Clock.getRoundNum() == roundToAttackEnemyHQ){
					rdv = rc.senseEnemyHQLocation();
				}
				nav.progressMove(rdv);
			}
		} 
	}

	private RobotType getNextEncampmentType() throws GameActionException{
		int encodedEncampment = rc.readBroadcast(NextEncampmentChannel);
		switch (encodedEncampment){
		case 0:
			rc.broadcast(NextEncampmentChannel, 1);
			return RobotType.SUPPLIER;
		case 1:
			rc.broadcast(NextEncampmentChannel, 2);
			return RobotType.GENERATOR;
		default:
			rc.broadcast(NextEncampmentChannel, 0);
			return RobotType.ARTILLERY;
		}
	}

	private  MapLocation getTargetEncampmentAtChannel(int i) throws GameActionException{
		//rc.setIndicatorString(2, "Attempting to read channel: "+Integer.toString(i));

		int broadcast = rc.readBroadcast(i);
		if (broadcast == 0){
			return null;
		} else{
			rc.broadcast(base_channel, i+1);

			return intToMapLocation(broadcast);
		}

	}

	private static MapLocation intToMapLocation(int integer){
		if (integer == -1){
			return null;
		} else{
			int x = integer/1000;
			int y = integer%1000;
			return new MapLocation(x,y);
		}
	}

}
