package firstPlayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public class HQPlayer extends BasePlayer {

	//declare local variables
    private int spawnPower = 50;
    private MapLocation[] target_encampments;
    private boolean farAwayHQ = true;
    private int dataStart = 30500;
	private int distanceThreshold = 600;
    
	public HQPlayer(RobotController rc) {
		super(rc);
		try{		    
			// encampment stuff
		    if(rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation()) < distanceThreshold){
	            farAwayHQ = false;
	        }

	        if(farAwayHQ){
	            target_encampments = rc.senseAllEncampmentSquares();
	            QuicksortLocations(target_encampments, rc.getLocation());
	        }else{
	            try {
	                target_encampments = rc.senseEncampmentSquares(rc.getLocation(), 200, Team.NEUTRAL);
	                QuicksortLocations(target_encampments, rc.getLocation());
	            } catch (GameActionException e) {
	                e.printStackTrace();
	            }
	        }

	        try {
	            rc.broadcast(base_channel, dataStart);
	            for (int i = 0; i < target_encampments.length;i++){
	                if(target_encampments[i] != null){
	                    rc.broadcast(dataStart+i, intFromMapLocation(target_encampments[i]));
	                    //rc.setIndicatorString(1, "Attempting to broadcast to base channel: "+Integer.toString(dataStart)+" with: "+i+" data channels.");
	                }
	            }
	        } catch (GameActionException e) {
	            e.printStackTrace();
	        }
	        
	        // swarm stuff
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
		    if(farAwayHQ && rc.senseAllEncampmentSquares().length > 20 && (!rc.hasUpgrade(Upgrade.FUSION) || !rc.hasUpgrade(Upgrade.DEFUSION))){
	            if(!rc.hasUpgrade(Upgrade.FUSION))
	                rc.researchUpgrade(Upgrade.FUSION);
	            else if(!rc.hasUpgrade(Upgrade.DEFUSION))
	                rc.researchUpgrade(Upgrade.DEFUSION);
	            return;
	        }
		    
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
			/*if (rc.hasUpgrade(orderedUpgrades[currentUpgradeIndex%orderedUpgrades.length])){
				currentUpgradeIndex++;
			} else{
				rc.researchUpgrade(orderedUpgrades[currentUpgradeIndex%orderedUpgrades.length]);
			}*/

			if (Clock.getRoundNum() == 500){
				//rc.setIndicatorString(1, "SET RV TO: "+rc.senseEnemyHQLocation().toString());
				rc.broadcast(rendezvousChannel, intFromMapLocation(rc.senseEnemyHQLocation()));
			}
		}
	}
}
