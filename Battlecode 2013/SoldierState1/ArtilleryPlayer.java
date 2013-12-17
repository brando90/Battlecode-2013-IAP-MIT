package SoldierState1;

import battlecode.common.*;

public class ArtilleryPlayer extends BasePlayer {
	public ArtilleryPlayer(RobotController rc) {
		super(rc);
		this.currentLocation = rc.getLocation();
	}
	public void run() throws GameActionException {
		if (rc.isActive()){
			MapLocation bestTarget = getBestFiringLocation();
			if (bestTarget != null && rc.canAttackSquare(bestTarget)){
				rc.attackSquare(bestTarget);
			}
		}
	}

	private MapLocation getBestFiringLocation() throws GameActionException{
		Robot[] enemyRobotsInRange = rc.senseNearbyGameObjects(Robot.class, getAttackRadius(), rc.getTeam().opponent());
		MapLocation[] enemyRobotLocations = mapLocationsFromRobots(enemyRobotsInRange);
		if (enemyRobotsInRange.length != 0){
			QuicksortLocations(enemyRobotLocations, this.currentLocation);
			for (MapLocation firingLocation : enemyRobotLocations){
				if (numberOfAlliedUnitsHit(firingLocation) == 0){
					return firingLocation;
				}
			}
		}
		return null;

	}

	private MapLocation[] mapLocationsFromRobots(Robot[] robots) throws GameActionException{
		MapLocation[] locations = new MapLocation[robots.length];
		for (int i = 0; i < robots.length; i++){
			Robot robot = robots[i];
			locations[i] = rc.senseLocationOf(robot);
		}
		return locations;
	}

	public static int getAttackRadius(){
		return 63;
	}

	private int numberOfAlliedUnitsHit(MapLocation firingLocation){
		return rc.senseNearbyGameObjects(Robot.class, firingLocation, GameConstants.ARTILLERY_SPLASH_RADIUS_SQUARED,rc.getTeam()).length;
	}

	/*	private int scorePotentialFiringLocation(MapLocation firingLocation){
		Robot[] alliedRobotsHit = rc.senseNearbyGameObjects(Robot.class, firingLocation, GameConstants.ARTILLERY_SPLASH_RADIUS_SQUARED, rc.getTeam());
		Robot[] enemyRobotsHit = rc.senseNearbyGameObjects(Robot.class, firingLocation, GameConstants.ARTILLERY_SPLASH_RADIUS_SQUARED, rc.getTeam().opponent());
		return 0;
	}*/
}

