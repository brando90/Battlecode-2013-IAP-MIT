package superSwarm2;

import battlecode.common.*;

public class HQPlayer extends BasePlayer {

	//declare local variables
    private int spawnPower = 50;
    private int currentUpgradeIndex = 0;
    private Upgrade[] orderedUpgrades = {Upgrade.FUSION,Upgrade.DEFUSION,Upgrade.NUKE};
	
	public HQPlayer(RobotController rc) {
		super(rc);
		try{
			MapLocation enemyHQLoc = rc.senseEnemyHQLocation();
			int rendezvousX = (3*rc.getLocation().x + enemyHQLoc.x)/4;
			int rendezvousY = (3*rc.getLocation().y + enemyHQLoc.y)/4;
			MapLocation rendezvous = new MapLocation(rendezvousX,rendezvousY);
			System.out.println("RENDEZVOUS: "+rendezvous.toString());
			rc.broadcast(rendezvousChannel, intFromMapLocation(rendezvous));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void run() throws GameActionException {
		//code to execute for the whole match
		if (rc.isActive()) {
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			int[] spawningOffsets = {0,1,-1,2,-2,3,-3,4};
			for (int offset : spawningOffsets){
				Direction spawnDir = Direction.values()[(dir.ordinal()+offset+8)%8];
				if (rc.canMove(spawnDir) && rc.getTeamPower() > spawnPower){
					rc.spawn(spawnDir);
					return;
				}
			}
			if (rc.hasUpgrade(orderedUpgrades[currentUpgradeIndex%orderedUpgrades.length])){
				currentUpgradeIndex++;
			} else{
				rc.researchUpgrade(orderedUpgrades[currentUpgradeIndex%orderedUpgrades.length]);
			}

			if (Clock.getRoundNum() == 500){
				//rc.setIndicatorString(1, "SET RV TO: "+rc.senseEnemyHQLocation().toString());
				rc.broadcast(rendezvousChannel, intFromMapLocation(rc.senseEnemyHQLocation()));
			}

		}
		
	}
}
