package badArtillery;

import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc) {
		BasePlayer br;
		switch(rc.getType()) {
		case HQ:
			br = new HQPlayer(rc);
			break;
		case SOLDIER:
			br = new SoldierPlayer(rc);
			break;
		case ARTILLERY:
			br = new ArtilleryPlayer(rc);
			break;
		default:
			br = new EncampmentPlayer(rc);
			break;
		}
		
		br.loop();
	}
	
	public static RobotInfo nearestEnemy(RobotController rc, int distThreshold) throws GameActionException {
		// functions written here will be available to each player file
		return null;
	}
}
