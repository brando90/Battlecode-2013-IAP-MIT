package superSwarm1;

import java.util.Random;


import battlecode.common.*;

/** The example funcs player is a player meant to demonstrate basic usage of the most common commands.
 * Robots will move around randomly, occasionally mining and writing useless messages.
 * The HQ will spawn soldiers continuously. 
 */
public class RobotPlayer {

	private static RobotController rc;
	private static MapLocation myCurrentLocation;
	//private static int huntingChannel;
	private static int targetLKPChannel;
	private static int rendezvousChannel;
	private static Random generator;

	private static int spawnPower = 50;
	private static int nextUpgradeIndex = 0;
	private static Upgrade[] orderedUpgrades = {Upgrade.PICKAXE,Upgrade.FUSION,Upgrade.NUKE};

	private static int previousMove = -1;


	public static void run(RobotController robotController) {
		rc = robotController;
		generator = new Random();
		targetLKPChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS);
		rendezvousChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS);
		//huntingChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS);

		if (rc.getType() == RobotType.HQ){
			try{
				MapLocation enemyHQLoc = rc.senseEnemyHQLocation();
				int rendezvousX = (3*rc.getLocation().x + enemyHQLoc.x)/4;
				int rendezvousY = (3*rc.getLocation().y + enemyHQLoc.y)/4;
				MapLocation rendezvous = new MapLocation(rendezvousX,rendezvousY);
				System.out.println("RENDEZVOUS: "+rendezvous.toString());
				rc.broadcast(rendezvousChannel, intFromMapLocation(rendezvous));
				rc.broadcast(targetLKPChannel, intFromMapLocation(rendezvous));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		while (true) {
			try {
				if (rc.getType() == RobotType.HQ) {
					HQCode();
				} else if (rc.getType() == RobotType.SOLDIER) {
					SoldierCode();                          
				}

				// End turn
			} catch (Exception e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}

	private static void HQCode() throws GameActionException{
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
			if (!rc.hasUpgrade(orderedUpgrades[nextUpgradeIndex%orderedUpgrades.length])){
				rc.researchUpgrade(orderedUpgrades[nextUpgradeIndex%orderedUpgrades.length]);
				nextUpgradeIndex++;
			}

			if (Clock.getRoundNum() == 500){
				//rc.setIndicatorString(1, "SET RV TO: "+rc.senseEnemyHQLocation().toString());
				rc.broadcast(rendezvousChannel, intFromMapLocation(rc.senseEnemyHQLocation()));
			}

		}
	}

	@SuppressWarnings("unused")
	private static int getSightRadiusSq(){
		//Generalize with game constants later...
		if (rc.hasUpgrade(Upgrade.VISION)){
			return 33;
		}else {
			return 14;
		}
	}

	private static void SoldierCode() throws GameActionException{
		myCurrentLocation = rc.getLocation();
		//MapLocation rendezvous = mapLocationFromInt(rendezvousChannel);
		//int enemySoldiersInSight = 0;
		//int swarmSoldiersInSight = 0;

		/*		if (isLeader){
			Robot[] robotsInSight = rc.senseNearbyGameObjects(Robot.class, getSightRadiusSq());
			for (Robot robot : robotsInSight){
				if (robot.getTeam() == rc.getTeam()){
					if (rc.senseRobotInfo(robot).type == RobotType.SOLDIER){
						swarmSoldiersInSight += 1;
					}
				}else {
					if (rc.senseRobotInfo(robot).type == RobotType.SOLDIER){
						enemySoldiersInSight += 1;
					}
				}
			}

			if (enemySoldiersInSight == 0){
				isLeader = false;
				int rendezvousInt = rc.readBroadcast(swarmRendezvousChannel);
				rc.broadcast(swarmDestinationChannel, rendezvousInt);
				MapLocation rendezvous = intToMapLocation(rendezvousInt);
				progressMove(rendezvous);
			}else {
				if (swarmSoldiersInSight > enemySoldiersInSight){
					Robot[] enemyRobotsInSight = rc.senseNearbyGameObjects(Robot.class, getSightRadiusSq(),rc.getTeam().opponent());
					MapLocation closestRobotLocation = getClosestRobotLocation(enemyRobotsInSight);
					rc.broadcast(swarmDestinationChannel,mapLocationToInt(closestRobotLocation));
					progressMove(closestRobotLocation);
				} else{
					isLeader = false;
					int rendezvousInt = rc.readBroadcast(swarmRendezvousChannel);
					rc.broadcast(swarmDestinationChannel, rendezvousInt);
					MapLocation rendezvous = intToMapLocation(rendezvousInt);
					progressMove(rendezvous);
				}
			}

		} else{
			MapLocation swarmDestination = intToMapLocation(rc.readBroadcast(swarmDestinationChannel));
			rc.setIndicatorString(0, "Swarm Destination: "+rc.readBroadcast(swarmDestinationChannel));
			MapLocation rendezvous = intToMapLocation(rc.readBroadcast(swarmRendezvousChannel));
			if (sameLocations(swarmDestination, rendezvous)){
				Robot[] enemyRobotsInSight = rc.senseNearbyGameObjects(Robot.class, getSightRadiusSq(), rc.getTeam().opponent());
				if (enemyRobotsInSight.length != 0){
					isLeader = true;
					MapLocation closestRobotLocation = getClosestRobotLocation(enemyRobotsInSight);
					progressMove(closestRobotLocation);
					rc.broadcast(swarmDestinationChannel,mapLocationToInt(closestRobotLocation));
				}else {
					swarmAround(rendezvous);
				}
			} else{

				swarmAround(swarmDestination);
			}
		}*/
	}


	@SuppressWarnings("unused")
	private static boolean sameLocations(MapLocation a, MapLocation b){
		if (a.x == b.x && a.y == b.y){
			return true;
		} else{
			return false;
		}
	}

	@SuppressWarnings("unused")
	private static MapLocation getClosestRobotLocation(Robot[] robots) throws GameActionException{
		if (robots.length == 0){
			return null;
		} else{
			RobotInfo currentRobotInfo = rc.senseRobotInfo(robots[0]);
			MapLocation closestRobot = currentRobotInfo.location;
			int closestDist = rc.getLocation().distanceSquaredTo(closestRobot);
			int distFromRobot;
			for (Robot robot : robots){
				currentRobotInfo = rc.senseRobotInfo(robot);
				distFromRobot = myCurrentLocation.distanceSquaredTo(currentRobotInfo.location);
				if (distFromRobot < closestDist){
					closestRobot = currentRobotInfo.location;
					closestDist = distFromRobot;
				}
			}
			return closestRobot;
		}
	}

	@SuppressWarnings("unused")
	private static void swarmAround(MapLocation swarmLocation) throws GameActionException{
		//ATTEMPT 1
		/*	MapLocation destination;
		boolean success;
		for (Direction adjDir : Direction.values()){
			destination = swarmLocation.add(adjDir);
			success = tryToReach(destination);
			if (success){
				return true;
			}
		}
		for (Direction adjDir : Direction.values()){
			destination = swarmLocation.add(adjDir);
			success = swarmAround(destination);
			if (success){
				return true;
			}
		}
		return false;*/

		//ATTEMPT 2

		progressMove(swarmLocation);

	}

	private static int intFromMapLocation(MapLocation location){
		return location.x*1000 + location.y;
	}

	@SuppressWarnings("unused")
	private static MapLocation mapLocationFromInt(int integer){
		if (integer == -1){
			return null;
		} else{
			int x = integer/1000;
			int y = integer%1000;
			return new MapLocation(x,y);
		}
	}

	@SuppressWarnings("unused")
	private static boolean booleanFromInt(int integer){
		return integer > 0;
	}

	@SuppressWarnings("unused")
	private static int intFromBoolean(boolean bool){
		if (bool){
			return 1;
		} else{
			return -1;
		}
	}

	private static void progressMove(MapLocation whereToGo) throws GameActionException{
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

	private static void moveOrDefuse(Direction dir) throws GameActionException{
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