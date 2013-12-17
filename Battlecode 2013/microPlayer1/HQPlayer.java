package microPlayer1;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Upgrade;

public class HQPlayer extends BasePlayer {

    private int spawnPower = 50;
    private int currentUpgradeIndex = 0;
    private Upgrade[] orderedUpgrades = {Upgrade.FUSION,Upgrade.NUKE};
	
	public HQPlayer(RobotController rc) {
		super(rc);
		try{
            int rendezvousX = (3*myLoc.x + enemyHQ.x)/4;
            int rendezvousY = (3*myLoc.y + enemyHQ.y)/4;
            MapLocation rendezvous = new MapLocation(rendezvousX,rendezvousY);
//            System.out.println("RENDEZVOUS: "+rendezvous.toString());
            rc.broadcast(rendezvousChannel, intFromMapLocation(rendezvous));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	public void run() throws GameActionException {
	    if (rc.isActive()){
	        Direction dir = myLoc.directionTo(enemyHQ);
            int[] spawningOffsets = {0,1,-1,2,-2,3,-3,4};
            for (int offset : spawningOffsets){
                Direction spawnDir = Direction.values()[(dir.ordinal()+offset+8)%8];
                if (rc.canMove(spawnDir) && rc.getTeamPower() > spawnPower && !boobyTrapped(myLoc, spawnDir)){
                    rc.spawn(spawnDir);
                    return;
                }
            }
//            if (rc.hasUpgrade(orderedUpgrades[currentUpgradeIndex%orderedUpgrades.length])){
//                currentUpgradeIndex++;
//            } else{
//                rc.researchUpgrade(orderedUpgrades[currentUpgradeIndex%orderedUpgrades.length]);
//            }

            if (Clock.getRoundNum() == 500){
                //rc.setIndicatorString(1, "SET RV TO: "+rc.senseEnemyHQLocation().toString());
                rc.broadcast(rendezvousChannel, intFromMapLocation(enemyHQ));
            }
        }
		
	}
	
    /**
     * Checks whether the next in a certain direction has a mine on it.
     * @param loc current location.
     * @param d the direction to check in
     * @return true if there is a non-allied mine, false otherwise
     */
    public boolean boobyTrapped(MapLocation loc, Direction d){
        MapLocation dest = loc.add(d);
        if (rc.senseMine(dest) != rc.getTeam()){
            return true;
        }
        return false;
    }
	
}
