package NewEncampPlayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

/** Haitao's first attempt at a good macro bot.
 */
public class RobotPlayer {
	public static void run(RobotController rc) {
		BasePlayer br;
		switch(rc.getType()) {
		case HQ:
			br = new HQPlayer(rc);
			break;
		case SOLDIER:
			//MapLocation enemyHQLoc = rc.senseEnemyHQLocation();
			br = new SoldierPlayer(rc);
			break;
		default:
			br = new EncampmentPlayer(rc);
			break;
		}		
		br.loop();
	}
	
	public static RobotInfo nearestEnemy(RobotController rc, int distThreshold) throws GameActionException {
		// functinos written here will be available to each player file
		return null;
	}
}
