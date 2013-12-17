package voldemort_micro;

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

	protected static int HQThreatenedChannel; //Needs to be initialized
	protected static int DefuseSpawnSquareChannel = 999; //Needs to be randomized
	protected static int ClearPathDirectionChannel; //Needs to be initialized
	protected static int TargetEncampmentsStartChannel = 27040; //Needs to be randomized; Pointer to channel array start
	protected static int RendezVousChannel = 43434; //Needs to be randomized
	protected static int MedbayLocationChannel = 111; //Needs to be randomized
	protected static int SupplierInProgressCountChannel = 60330; //Needs to be randomized
	protected static int GeneratorInProgressCountChannel = 64330; //Needs to be randomized
	
	protected int mineCount;
    protected int distThreshold = 1600;
    protected Strategy strategy;
    protected int mineCountThreshold = 144;
    
    protected enum Strategy{
        RUSH,ECON;
    }

	public BasePlayer(RobotController rc) {
		this.rc = rc;
		this.HQLocation = rc.senseHQLocation();
		this.enemyHQLocation = rc.senseEnemyHQLocation();
		this.myTeam = rc.getTeam();
		this.mapWidth = rc.getMapWidth();
		this.mapHeight = rc.getMapHeight();
		this.mapRadiusSq = this.mapWidth*this.mapWidth + this.mapHeight*this.mapHeight;
		this.mapArea = this.mapWidth*this.mapHeight;
		this.mineCount = countMines(HQLocation, enemyHQLocation);
        this.setStrategy();
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

	protected boolean onClearPath() throws GameActionException{
		return this.HQLocation.directionTo(currentLocation) == directionFromInt(rc.readBroadcast(ClearPathDirectionChannel));
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
		if (integer == 0){
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
	
	protected void setStrategy(){
	    int dist = HQLocation.distanceSquaredTo(enemyHQLocation);
        if (dist < distThreshold || mineCount < mineCountThreshold){
            this.strategy = Strategy.RUSH;
        }else{
            this.strategy = Strategy.ECON;
        }
    }
	
	protected int countMines(MapLocation a, MapLocation b){
        int startX = Math.min(a.x, b.x);
        int startY = Math.min(a.y, b.y);
        int endX = Math.max(a.x, b.x);
        int endY = Math.max(a.y, b.y);
        int count = 0;
        for (int x=startX; ++x < endX;){
            for (int y=startY; ++y < endY;){
                if (Math.abs(x - y) <= 4){
                    MapLocation spot = new MapLocation(x,y);
                    if (rc.senseMine(spot) == Team.NEUTRAL){
                        ++count;
                    }
                }
            }
        }
        return count;
    }
	
}
