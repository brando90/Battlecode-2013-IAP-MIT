package SoldierState1;

import battlecode.common.*;

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
		case ARTILLERY:
			br = new ArtilleryPlayer(rc);
			break;
//		case MEDBAY:
//			br = new MedbayPlayer(rc);
//			break;
//		case SUPPLIER:
//			br = new SupplierPlayer(rc);
//			break;
//		case GENERATOR:
//			br = new GeneratorPlayer(rc);
//			break;
		default:
			br = new EncampmentPlayer(rc);
			break;
		}
		
		br.loop();
	}
}
