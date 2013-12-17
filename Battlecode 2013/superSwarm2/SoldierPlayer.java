package superSwarm2;

import battlecode.common.*;

public class SoldierPlayer extends BasePlayer {
	private boolean atRV = false;
	private NavigationController navController;
	public SoldierPlayer(RobotController rc) {
		super(rc);
		navController = new NavigationController(rc);
	}
	public void run() throws GameActionException{
		myCurrentLocation = rc.getLocation();
		MapLocation rendezvous = mapLocationFromInt(rc.readBroadcast(rendezvousChannel));

		Robot[] enemyRobotsInSharedVision = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());

		if (enemyRobotsInSharedVision.length == 0){
			if (myCurrentLocation.distanceSquaredTo(rendezvous) <=2 && !atRV){
				atRV = true;
			}
			if (atRV && rc.senseMine(myCurrentLocation) == null){
				if (rc.isActive()){
					rc.layMine();
				}
			} else{
				swarmAround(rendezvous);
			}
		} else{
			atRV = false;
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



	private void swarmAround(MapLocation destination) throws GameActionException{
		navController.progressMove(destination);
	}

}
