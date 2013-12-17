package FusedPlayer;

import java.util.Random;

import battlecode.common.*;

public abstract class BasePlayer {
	protected RobotController rc;
	protected MapLocation currentLocation;
	protected MapLocation HQLocation;
	protected MapLocation enemyHQLocation;
	protected Team myTeam;
	protected int mapWidth;
	protected int mapHeight;
	protected int mapRadiusSq;
	protected int mapArea;
	protected MapLocation mapCenter;
	
	protected static Random generator = new Random(130260);

	protected static int HQThreatenedChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS); //Needs to be initialized
	protected static int DefuseSpawnSquareChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS); //Needs to be randomized
	protected static int TargetEncampmentsStartChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS); //Needs to be randomized; Pointer to channel array start
	protected static int MedbayLocationChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS); //Needs to be randomized
	protected static int SupplierInProgressCountChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS); //Needs to be randomized
	protected static int GeneratorInProgressCountChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS); //Needs to be randomized
	protected static int EnemyNukeHalfDoneChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS); //Needs to be randomized

	public BasePlayer(RobotController rc) {
		this.rc = rc;
		this.HQLocation = rc.senseHQLocation();
		this.enemyHQLocation = rc.senseEnemyHQLocation();
		this.myTeam = rc.getTeam();
		this.mapWidth = rc.getMapWidth();
		this.mapHeight = rc.getMapHeight();
		this.mapRadiusSq = this.mapWidth*this.mapWidth + this.mapHeight*this.mapHeight;
		this.mapArea = this.mapWidth*this.mapHeight;
		this.mapCenter = new MapLocation((HQLocation.x+enemyHQLocation.x)/2,(HQLocation.y+enemyHQLocation.y)/2);
	}

	public abstract void run() throws GameActionException;

	public void loop() {
		while(true) {
			try {
				// Execute turn
				run();

			} catch (Exception e) {
				e.printStackTrace();
			}

			// End turn
			rc.yield();
		}
	}

	protected boolean mineHazard(MapLocation location) {
		Team mine = rc.senseMine(location);
		return (mine == Team.NEUTRAL || mine == myTeam.opponent());
	}

	protected static int intFromDirection(Direction direction){
		return direction.ordinal() + 1;
	}

	protected static Direction directionFromInt(int integer){
		if (integer == 0){
			return null;
		} else{
			return Direction.values()[(integer - 1)%8];
		}
	}

	protected static int roundNumIntFromLocation(MapLocation location){
		return 1000000*Clock.getRoundNum() + 1000*location.x + location.y;
	}

	protected static int roundNumFromInt(int integer){
		return integer/1000000;
	}

	protected static int intFromMapLocation(MapLocation location){
		return 1000000 + location.x*1000 + location.y;
	}

	protected static MapLocation mapLocationFromInt(int integer){
		if (integer <= 0){
			return null;
		} else{
			int x = (integer/1000)%1000;
			int y = integer%1000;
			return new MapLocation(x,y);
		}
	}

	protected static boolean booleanFromInt(int integer){
		return integer > 0;
	}

	protected static int intFromBoolean(boolean bool){
		if (bool){
			return 1;
		} else{
			return -1;
		}
	}

	protected boolean roundNumValid(int roundNum){
		return (Clock.getRoundNum() - roundNum) <= 1;
	}

	protected int getSightRadiusSq(){
		if (rc.hasUpgrade(Upgrade.VISION)){
			return 33;
		} else{
			return 14;
		}
	}

	protected void QuicksortLocations(MapLocation[] locations, MapLocation reference){
		QuicksortRecurseLocations(locations, reference, 0, locations.length - 1);
	}

	private void QuicksortRecurseLocations(MapLocation[] locations, MapLocation reference,int from, int to){


		if(from >= to)
			return;

		//choose pivot
		int p = (from + to) / 2;

		//Partition
		int i=from;
		int j=to;

		while(i <= j){
			if(locations[i].distanceSquaredTo(reference) <= locations[p].distanceSquaredTo(reference))
				i++;
			else if(locations[j].distanceSquaredTo(reference) >= locations[p].distanceSquaredTo(reference))
				j--;
			else{
				swapLocations(locations, i, j);
				i++;
				j--;
			}
		}

		//Swap pivot after partition
		if(p < j){
			swapLocations(locations, j, p);
			p=j;
		} 
		else if(p > i){
			swapLocations(locations, i, p);
			p=i;
		}

		//Recursive sort the rest of the list
		QuicksortRecurseLocations(locations, reference, from, p-1);
		QuicksortRecurseLocations(locations, reference, p+1, to);

	}

	private static void swapLocations(MapLocation[] a, int i, int j){
		MapLocation temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}
	
	protected int getGeneratorCount() throws GameActionException{
		int genCount = 0;
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class, 4*this.mapRadiusSq, this.myTeam);
		for (Robot alliedRobot : alliedRobots){
			if (rc.senseRobotInfo(alliedRobot).type == RobotType.GENERATOR){
				genCount += 1;
			}
		}
		return genCount + rc.readBroadcast(GeneratorInProgressCountChannel);
	}

	protected int getPredictedPowerGenerationRate() throws GameActionException{
		int predictedPowerRate = 40;
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class, 4*this.mapRadiusSq, this.myTeam);
		for (Robot alliedRobot : alliedRobots){
			if (rc.senseRobotInfo(alliedRobot).type == RobotType.GENERATOR){
				predictedPowerRate += 10;
			} else{
				predictedPowerRate -= 2;
			}
		}
		return predictedPowerRate + 10*rc.readBroadcast(GeneratorInProgressCountChannel);
	}

	protected int getPowerGenerationRate() throws GameActionException{ //Bytecode expensive method
		int powerRate = 40;
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class, 4*this.mapRadiusSq, this.myTeam);
		for (Robot alliedRobot : alliedRobots){
			if (rc.senseRobotInfo(alliedRobot).type == RobotType.GENERATOR){
				powerRate += 10;
			} else{
				powerRate -= 2;
			}
		}
		return powerRate;
	}
}
