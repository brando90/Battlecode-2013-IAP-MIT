package DefensiveSwarm;

import battlecode.common.*;

public class SoldierPlayer extends BasePlayer {
	private boolean atRV = false;
	MapLocation[] NeutralEncampments;
	MapLocation closestEncampment;
	NavigationController nav;
	Team ourTeam, opponentTeam;
	
	private enum State {
	    EncampmentCapture,Swarm,Micro,Soldier,InBattle
	}
	
	private State state;
	
	
	public SoldierPlayer(RobotController rc){
		super(rc);
		this.nav = new NavigationController(rc);
		this.currentLocation = rc.getLocation();
		this.ourTeam = rc.getTeam();
        this.opponentTeam = this.ourTeam.opponent();
//		try{
//			if (Clock.getRoundNum() == 1){
//				this.state = State.EncampmentCapture;
//				this.NeutralEncampments = rc.senseEncampmentSquares(this.currentLocation, this.mapRadiusSq/4, Team.NEUTRAL);
//				this.closestEncampment = getNearestEncampmentLocation(NeutralEncampments);
//			}else
//				{ this.state = State.Soldier;}
//		}catch (Exception e){
//			e.getStackTrace();
//		}
		//this.state = State.InBattle;
		this.state = State.Soldier;
	}
	
	public void run() throws GameActionException{
		this.currentLocation = rc.getLocation();
		if (rc.isActive())
		{
			if (this.state == State.EncampmentCapture){
				if (this.currentLocation.equals(closestEncampment)){
					rc.captureEncampment(RobotType.SUPPLIER);
				}else{
					nav.progressMove(closestEncampment, false);
				}
			}else{
				Robot[] enemyBotsInProximity;
				int enemyBotsInProximityNum;
				enemyBotsInProximity = rc.senseNearbyGameObjects(Robot.class, this.currentLocation, 2, this.opponentTeam);
				enemyBotsInProximityNum = enemyBotsInProximity.length;
				rc.setIndicatorString(0, enemyBotsInProximityNum+"");
				if (enemyBotsInProximityNum > 0){
					this.microCloseBattle();
				}
				
				MapLocation rendezvous = mapLocationFromInt(rc.readBroadcast(rendezvousChannel));
				Robot[] enemyRobotsInSharedVision = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
				if (enemyRobotsInSharedVision.length == 0){
					this.layMineOrGoToRV(rendezvous);
				} else{ //enemyRobotsInSharedVision.length > 1
					atRV = false;
					MapLocation closestRobotToRVLocation = getClosestRobotLocation(enemyRobotsInSharedVision,rendezvous);
					//if he is really far, use progressMove
					nav.progressMove(closestRobotToRVLocation, true);
				}
				
			}
		}
	}
	
	
	private void layMineOrGoToRV(MapLocation rendezvous) throws GameActionException {
		if (this.currentLocation.distanceSquaredTo(rendezvous) <=2 && !atRV){
			atRV = true;
		}
		if (atRV && rc.senseMine(this.currentLocation) == null){
			if (rc.isActive()){
				rc.layMine();
			}
		} else{
			swarmAround(rendezvous);
		}
	}

	private void microCloseBattle() throws GameActionException {
		//if there is any enemy bots in adjacency, try maximizing damage and minimizing damage taken
		this.state = State.InBattle;
		Direction potentialDirectionMovement;
		MapLocation potentialNewLocation;
		MapLocation bestBattleLocation;
		bestBattleLocation = this.currentLocation;
		Direction[] directions = Direction.values();
		for (int i = 8; --i>= 0;){
			potentialDirectionMovement = directions[i];
			potentialNewLocation = this.currentLocation.add(potentialDirectionMovement);
			Team mine = rc.senseMine(potentialNewLocation);
			if (rc.canMove(potentialDirectionMovement) && !(mine == this.opponentTeam || mine == Team.NEUTRAL)){
				potentialNewLocation = this.currentLocation.add(potentialDirectionMovement);
				bestBattleLocation = this.statEvalRadSizeOne(potentialNewLocation, bestBattleLocation);
			}
		}
		Direction directionBetterBalltlePos = this.currentLocation.directionTo(bestBattleLocation);
		if (directionBetterBalltlePos != Direction.NONE){
			rc.move(directionBetterBalltlePos);
		}
		
	}

	private MapLocation statEvalRadSizeOne(MapLocation potentialNewLocation, MapLocation currentBestLocation) throws GameActionException {
		Robot[] potNewLocRobots = rc.senseNearbyGameObjects(Robot.class, potentialNewLocation, 2, this.opponentTeam);
		Robot[] bestLocRobots = rc.senseNearbyGameObjects(Robot.class, currentBestLocation, 2, this.opponentTeam);
		int num_potNewLoc = potNewLocRobots.length;
		int num_bestLoc = bestLocRobots.length;
		if (num_bestLoc > num_potNewLoc && num_potNewLoc > 0 || num_bestLoc == 0 ){
			return potentialNewLocation;
		}else if(num_bestLoc ==  num_potNewLoc){
			Robot currBot;
			int tot_alliedPOT = 0;
			for (int i = num_potNewLoc; --i>=0;){
				currBot = potNewLocRobots[i];
				tot_alliedPOT += rc.senseNearbyGameObjects(Robot.class, rc.senseRobotInfo(currBot).location, 2, this.opponentTeam).length;
			}
			int tot_alliedBEST = 0;
			for (int i = num_potNewLoc; --i>=0;){
				currBot = potNewLocRobots[i];
				tot_alliedBEST += rc.senseNearbyGameObjects(Robot.class, rc.senseRobotInfo(currBot).location, 2, this.opponentTeam).length;
			}
			if(tot_alliedBEST > tot_alliedPOT){
				return currentBestLocation;
			} else{
				return potentialNewLocation;
			}
		}
		return currentBestLocation;
	}

	
	@SuppressWarnings("unused")
	private MapLocation getNearestEncampmentLocation(MapLocation[] arrayOfEncampments){
		MapLocation nearestEncampment = arrayOfEncampments[arrayOfEncampments.length - 1];
		int distanceToNearestEncampment = this.currentLocation.distanceSquaredTo(nearestEncampment);
		for (int i = arrayOfEncampments.length; --i>=1;){
			if (distanceToNearestEncampment 
			<= this.currentLocation.distanceSquaredTo(arrayOfEncampments[i]) )
			{
				distanceToNearestEncampment = this.currentLocation.distanceSquaredTo(arrayOfEncampments[i]);
				nearestEncampment = arrayOfEncampments[i];
			}
		}
		return nearestEncampment;
	}
	
	
	private void swarmAround(MapLocation destination) throws GameActionException{
		this.nav.progressMove(destination, false);
	}
	
	
}
