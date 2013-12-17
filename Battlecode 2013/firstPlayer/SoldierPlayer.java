package firstPlayer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class SoldierPlayer extends BasePlayer {
    
    NavigationController nav;
    MapLocation rdv;
    Fleet fleet;
    
	public SoldierPlayer(RobotController rc) {
		super(rc);
		nav = new NavigationController(rc);
		
		MapLocation encampment;
        try {
            int BC = rc.readBroadcast(base_channel);
            rc.setIndicatorString(0, "Tuning into Base Channel: "+BC);
            encampment = getTargetEncampmentAtChannel(BC);
            if (encampment != null ){
                fleet = Fleet.BUILD;
                rdv = encampment;
                rc.setIndicatorString(1, "Heading to Enc: "+encampment.toString());
            }else if (Clock.getRoundNum() > 1200){
                fleet = Fleet.CHARGE;
                rdv = rc.senseEnemyHQLocation();
            }else{
                fleet = Fleet.SWARM;
                rdv = getRV();
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        } 
	}
	
	private MapLocation getRV() {
        MapLocation enemyHQLoc = rc.senseEnemyHQLocation();
        int rendezvousX = (3*rc.senseHQLocation().x + enemyHQLoc.x)/4;
        int rendezvousY = (3*rc.senseHQLocation().y + enemyHQLoc.y)/4;
        return new MapLocation(rendezvousX,rendezvousY);
    }
	
	public void run() throws GameActionException{
	    if (rc.isActive()){
	        myCurrentLocation = rc.getLocation();
	        switch (fleet){
	        case BUILD:
	            if (rc.senseEncampmentSquare(rc.getLocation())){
	                rc.captureEncampment(getNextEncampmentType());
	            } else{
	                if (Clock.getRoundNum() == 1200){
	                    rdv = rc.senseEnemyHQLocation();
	                }
	                nav.progressMove(rdv);
	            }
	            break;
	        case CHARGE:
	            break;
	        default:
	            MapLocation rendezvous = mapLocationFromInt(rc.readBroadcast(rendezvousChannel));
                Robot[] enemyRobotsInSharedVision = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
                if (enemyRobotsInSharedVision.length == 0){
                    swarmAround(rendezvous);
                } else{
                    MapLocation closestRobotToRVLocation = getClosestRobotLocation(enemyRobotsInSharedVision,rendezvous);
                    swarmAround(closestRobotToRVLocation);
                }
	            break;
	        }
	    }
	}
	
	private RobotType getNextEncampmentType() throws GameActionException{
        int encodedEncampment = rc.readBroadcast(NextEncampmentChannel);
        switch (encodedEncampment){
        case 0:
            rc.broadcast(NextEncampmentChannel, 1);
            return RobotType.SUPPLIER;
        case 1:
            rc.broadcast(NextEncampmentChannel, 2);
            return RobotType.GENERATOR;
        default:
            rc.broadcast(NextEncampmentChannel, 0);
            return RobotType.ARTILLERY;
        }
    }

	private void swarmAround(MapLocation destination) throws GameActionException{
		nav.progressMove(destination);
	}
	
	private  MapLocation getTargetEncampmentAtChannel(int i) throws GameActionException{
        rc.setIndicatorString(2, "Attempting to read channel: "+Integer.toString(i));
        int broadcast = rc.readBroadcast(i);
        if (broadcast == 0){
            return null;
        } else{
            rc.broadcast(base_channel, i+1);
            return mapLocationFromInt(broadcast);
        }
    }
}
