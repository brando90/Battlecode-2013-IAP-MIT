package team260;

import battlecode.common.*;

/** Haitao's first attempt at a good macro bot.
 */
public class RobotPlayer {
	public static void run(RobotController rc) {
		BasePlayer br;
		int encampmentCount = rc.senseAllEncampmentSquares().length;
		if (encampmentCount > 40){
			switch(rc.getType()) {
			case HQ:
				br = new FusedHQPlayer(rc);
				break;
			case SOLDIER:
				br = new FusedSoldierPlayer(rc);
				break;
			case ARTILLERY:
				br = new ArtilleryPlayer(rc);
				break;
			case MEDBAY:
				br = new FusedMedbayPlayer(rc);
				break;
			case SUPPLIER:
				br = new FusedSupplierPlayer(rc);
				break;
			case GENERATOR:
				br = new FusedGeneratorPlayer(rc);
				break;
			default:
				br = new EncampmentPlayer(rc);
				break;
			}
		}else{
			switch(rc.getType()) {
			case HQ:
				br = new QuickHQPlayer(rc);
				break;
			case SOLDIER:
				br = new QuickSoldierPlayer(rc);
				break;
			case ARTILLERY:
				br = new ArtilleryPlayer(rc);
				break;
			case MEDBAY:
				br = new QuickMedbayPlayer(rc);
				break;
			case SUPPLIER:
				br = new QuickSupplierPlayer(rc);
				break;
			case GENERATOR:
				br = new QuickGeneratorPlayer(rc);
				break;
			default:
				br = new EncampmentPlayer(rc);
				break;
			}
		}

		br.loop();
	}

}
