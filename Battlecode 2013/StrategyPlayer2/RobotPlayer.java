package StrategyPlayer2;

import battlecode.common.*;

/** Haitao's first attempt at a good macro bot.
 */
public class RobotPlayer {
	private enum MapSize{
		Small,Medium,Large
	}
	
	public static void run(RobotController rc) {
		BasePlayer br;
		MapSize mapSize = mapSize(rc);
		switch(rc.getType()) {
		case HQ:
			switch (mapSize){
			case Small:
				br = new SmallHQPlayer(rc);
				break;
			case Medium:
				br = new HQPlayer(rc);
				break;
			case Large:
				br = new HQPlayer(rc);
				break;
			default:
				br = new HQPlayer(rc);
				break;
			}
			break;
		case SOLDIER:
			switch (mapSize){
			case Small:
				br = new SmallSoldierPlayer(rc);
				break;
			case Medium:
				br = new SoldierPlayer(rc);
				break;
			case Large:
				br = new SoldierPlayer(rc);
				break;
			default:
				br = new SoldierPlayer(rc);
				break;
			}
			break;
		case ARTILLERY:
			br = new ArtilleryPlayer(rc);
			break;
		case MEDBAY:
			br = new MedbayPlayer(rc);
			break;
		case SUPPLIER:
			br = new SupplierPlayer(rc);
			break;
		case GENERATOR:
			br = new GeneratorPlayer(rc);
			break;
		default:
			br = new EncampmentPlayer(rc);
			break;
		}
		
		br.loop();
	}
	
	private static MapSize mapSize(RobotController rc){
		int mapArea = rc.getMapWidth()*rc.getMapHeight();
		if (mapArea < 900){
			return MapSize.Small;
		} else if (mapArea < 3025){
			return MapSize.Medium;
		} else{
			return MapSize.Large;
		}
	}
}
