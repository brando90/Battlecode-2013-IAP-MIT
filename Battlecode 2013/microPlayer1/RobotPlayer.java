package microPlayer1;

import battlecode.common.RobotController;

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
			br = new SoldierPlayer(rc);
			break;
		default:
			br = new EncampmentPlayer(rc);
			break;
		}	
		br.loop();
	}
	
}
