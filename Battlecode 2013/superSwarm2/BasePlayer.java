package superSwarm2;

import java.util.Random;

import battlecode.common.*;

public abstract class BasePlayer {
	protected RobotController rc;
	protected MapLocation myCurrentLocation;
	protected int rendezvousChannel;
	protected Random generator;
	
	public BasePlayer(RobotController robotController) {
		rc = robotController;
		generator = new Random();
		rendezvousChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS);
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
	
	protected int getSightRadiusSq(){
		//Generalize with game constants later...
		if (rc.hasUpgrade(Upgrade.VISION)){
			return 33;
		}else {
			return 14;
		}
	}
	
	protected static boolean sameLocations(MapLocation a, MapLocation b){
		return (a.x == b.x && a.y == b.y);
	}
	
	protected MapLocation getClosestRobotLocation(Robot[] robots, MapLocation reference) throws GameActionException{
		if (robots.length == 0){
			return null;
		} else{
			RobotInfo currentRobotInfo = rc.senseRobotInfo(robots[0]);
			MapLocation closestRobot = currentRobotInfo.location;
			int closestDist = reference.distanceSquaredTo(closestRobot);
			int distFromRobot;
			for (Robot robot : robots){
				currentRobotInfo = rc.senseRobotInfo(robot);
				distFromRobot = reference.distanceSquaredTo(currentRobotInfo.location);
				if (distFromRobot < closestDist){
					closestRobot = currentRobotInfo.location;
					closestDist = distFromRobot;
				}
			}
			return closestRobot;
		}
	}
	
	protected static int intFromMapLocation(MapLocation location){
		return location.x*1000 + location.y;
	}

	protected static MapLocation mapLocationFromInt(int integer){
		if (integer == -1){
			return null;
		} else{
			int x = integer/1000;
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
	
	protected static void QuicksortLocations(MapLocation[] locations, MapLocation reference){
		QuicksortRecurseLocations(locations, reference, 0, locations.length - 1);
	}
	
    private static void QuicksortRecurseLocations(MapLocation[] locations, MapLocation reference,int from, int to){
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
    
	/*protected void QuicksortRobots(Robot[] robots, MapLocation reference) throws GameActionException{
		QuicksortRecurseRobots(robots, reference, 0, robots.length - 1);
	}
	
    private void QuicksortRecurseRobots(Robot[] robots, MapLocation reference,int from, int to) throws GameActionException{
    	if(from >= to)
    		return;
    	
    	//choose pivot
    	int p = (from + to) / 2;
    	
    	//Partition
    	int i=from;
    	int j=to;
    	
    	while(i <= j){
    		if(rc.senseRobotInfo(robots[i]).location.distanceSquaredTo(reference) <= rc.senseRobotInfo(robots[p]).location.distanceSquaredTo(reference))
    			i++;
    		else if(rc.senseRobotInfo(robots[j]).location.distanceSquaredTo(reference) >= rc.senseRobotInfo(robots[p]).location.distanceSquaredTo(reference))
    			j--;
    		else{
    			swapRobots(robots, i, j);
    			i++;
    			j--;
    		}
    	}
    	
    	//Swap pivot after partition
    	if(p < j){
			swapRobots(robots, j, p);
			p=j;
		} 
		else if(p > i){
			swapRobots(robots, i, p);
			p=i;
		}
		
    	//Recursive sort the rest of the list
		QuicksortRecurseRobots(robots, reference, from, p-1);
		QuicksortRecurseRobots(robots, reference, p+1, to);
    	
    }
    
    private static void swapRobots(Robot[] a, int i, int j){
    Robot temp = a[i];
    	a[i] = a[j];
    	a[j] = temp;
    }*/
}
