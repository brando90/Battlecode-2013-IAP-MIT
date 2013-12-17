package microPlayer1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class SoldierPlayer extends BasePlayer {
    private int previousMove = -1;
    int safeDistance;
    MapLocation rallyPoint;
    
	public SoldierPlayer(RobotController rc) {
		super(rc);
	      rallyPoint = enemyHQ;
	      safeDistance = 2;
	}
	
	public void run() throws GameActionException{
	    if (rc.isActive()){
	        MapLocation rendezvous = mapLocationFromInt(rc.readBroadcast(rendezvousChannel));
	        Robot[] enemyRobotsInSharedVision = rc.senseNearbyGameObjects(Robot.class, 10000, opponent);
	        if (enemyRobotsInSharedVision.length == 0){
	            swarmAround(rendezvous);
	        } else{
	            MapLocation closestRobotToRVLocation = getClosestRobotLocation(enemyRobotsInSharedVision,rendezvous);
	            swarmAround(closestRobotToRVLocation);
	        }
	    }
	}
	
	private void moveOrDefuse(Direction dir) throws GameActionException {
	    MapLocation ahead = myLoc.add(dir);
	    Team mineAhead = rc.senseMine(ahead);
	    if(mineAhead != null && mineAhead != us){
	        rc.defuseMine(ahead);
	    }else{
	        if (rc.canMove(dir)) rc.move(dir);           
	    }
    }
	
	private int numberOfMoves(MapLocation origin, MapLocation destination){
	    int dx = Math.abs(origin.x - destination.x);
	    int dy = Math.abs(origin.y - destination.y);
	    return Math.min(dx, dy) + Math.abs(dx-dy);
	}
    
    private void progressMove(MapLocation whereToGo) throws GameActionException{
        rc.setIndicatorString(1, "Swarm target: "+whereToGo.toString());
        int dist = rc.getLocation().distanceSquaredTo(whereToGo);
        if (dist>0 && rc.isActive()){
            Direction toTarget = rc.getLocation().directionTo(whereToGo);
            int[] directionOffsets = {0,1,-1,2,-2};
            Direction move;
            MapLocation ahead;
            for (int offset: directionOffsets){
                move = Direction.values()[(toTarget.ordinal()+offset+8)%8];
                ahead = rc.getLocation().add(move);
                Team mine = rc.senseMine(ahead);
                int move_num = move.ordinal();
                if (rc.canMove(move) && (mine == rc.getTeam() || mine == null) && move_num != (previousMove+4)%8){
                    previousMove = move_num;
                    rc.move(move);
                    return;
                }
            }
            previousMove = -1;
            moveOrDefuse(toTarget);
        }
    }

    private void swarmAround(MapLocation destination) throws GameActionException{
        if (numberOfMoves(myLoc, destination) <= safeDistance){
            progressMove(destination);
        }
    }
}
