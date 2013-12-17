package microPlayer1;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public abstract class BasePlayer {
	protected RobotController rc;
	protected Team us;
	protected Team opponent;
	protected MapLocation myLoc;
	protected MapLocation myHQ;
	protected MapLocation enemyHQ;
    protected int rendezvousChannel;
    protected Random generator;
	
	public BasePlayer(RobotController rc) {
		this.rc = rc;
		myLoc = rc.getLocation();
		myHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();
        us = rc.getTeam();
        opponent = rc.getTeam().opponent();
		generator = new Random();
        rendezvousChannel = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS);
	}
	
	public abstract void run() throws GameActionException;
	
	public void loop() {
		while(true) {
			try {
				// Execute turn
				run();
			} catch (GameActionException e) {
				System.out.println(e.getType().toString());
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
        if (a.x == b.x && a.y == b.y){
            return true;
        } else{
            return false;
        }
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
    
    protected static void Quicksort(MapLocation[] locations, MapLocation reference){
        QuicksortRecurse(locations, reference, 0, locations.length - 1);
    }
    
    private static void QuicksortRecurse(MapLocation[] locations, MapLocation reference,int from, int to){
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
                swap(locations, i, j);
                i++;
                j--;
            }
        }
        
        //Swap pivot after partition
        if(p < j){
            swap(locations, j, p);
            p=j;
        } 
        else if(p > i){
            swap(locations, i, p);
            p=i;
        }
        
        //Recursive sort the rest of the list
        QuicksortRecurse(locations, reference, from, p-1);
        QuicksortRecurse(locations, reference, p+1, to);
        
    }
    
    private static void swap(MapLocation[] a, int i, int j){
        MapLocation temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
}
