package microPlayer2;

import battlecode.common.*;

public class SoldierPlayer extends BasePlayer {
	private int previousMove = -1;
	public SoldierPlayer(RobotController rc) {
		super(rc);
	}
	public void run() throws GameActionException{
		myCurrentLocation = rc.getLocation();
		MapLocation rendezvous = mapLocationFromInt(rc.readBroadcast(rendezvousChannel));
		
		Robot[] enemyRobotsInSharedVision = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
		
		if (enemyRobotsInSharedVision.length == 0){
			swarmAround(rendezvous);
		} else{
			MapLocation closestRobotToRVLocation = getClosestRobotLocation(enemyRobotsInSharedVision,rendezvous);
			swarmAround(closestRobotToRVLocation);
		}

	/*	if (isHunting){
			MapLocation targetLKP = mapLocationFromInt(rc.readBroadcast(targetLKPChannel));
			
		} else{
			if (enemyRobotsInSight.length != 0){
				rc.broadcast(huntingChannel, intFromBoolean(true));
				MapLocation closestRobotLocation = getClosestRobotLocation(enemyRobotsInSight,rendezvous);
				progressMove(closestRobotLocation);
				rc.broadcast(targetLKPChannel, intFromMapLocation(closestRobotLocation));
			}else {
				swarmAround(rendezvous);
			}
		}*/

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
			if(rc.canMove(toTarget) || rc.senseMine(rc.getLocation())==rc.getTeam()){
				moveOrDefuse(toTarget);
			} 
		}
	}

	private void swarmAround(MapLocation destination) throws GameActionException{
		progressMove(destination);
	}

	private void moveOrDefuse(Direction dir) throws GameActionException{
		MapLocation ahead = rc.getLocation().add(dir);
		if(rc.senseMine(ahead)!= null && rc.senseMine(ahead) != rc.getTeam()){
			rc.defuseMine(ahead);
		}else{
			if(rc.canMove(dir)){
				rc.move(dir);     
			}
		}
	}
}
