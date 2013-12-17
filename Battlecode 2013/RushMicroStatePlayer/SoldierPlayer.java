package RushMicroStatePlayer;

import battlecode.common.*;

public class SoldierPlayer extends BasePlayer {
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
		if (rc.isActive())
			this.currentLocation = rc.getLocation();
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
				if (enemyBotsInProximityNum >= 2 || this.state == State.InBattle){
					//if there is any enemy bots in adjacency, try maximizing damage and minimizing damage taken
					this.state = State.InBattle;
					Direction potentialDirectionMovement;
					MapLocation potentialNewLocation;
					MapLocation bestBattleLocation;
					bestBattleLocation = this.currentLocation;
					Direction[] directions = Direction.values();
					for (int i = 8; --i>= 0;){
						potentialDirectionMovement = directions[i];
						if (rc.canMove(potentialDirectionMovement)){
							potentialNewLocation = this.currentLocation.add(potentialDirectionMovement);
							bestBattleLocation = this.statEvalRadSizeOne(potentialNewLocation, bestBattleLocation);
						}
					}
					Direction directionBetterBalltlePos = this.currentLocation.directionTo(bestBattleLocation);
					if (directionBetterBalltlePos != Direction.NONE){
					rc.move(directionBetterBalltlePos);
					}
				}
				nav.progressMove(enemyHQLocation, false);
			}
		}
	}
	
	private MapLocation statEvalRadSizeOne(MapLocation potentialNewLocation, MapLocation currentBestLocation) {
		int num_potNewLoc = rc.senseNearbyGameObjects(Robot.class, potentialNewLocation, 2, this.opponentTeam).length;
		int num_bestLoc = rc.senseNearbyGameObjects(Robot.class, currentBestLocation, 2, this.opponentTeam).length;
		if (num_bestLoc > num_potNewLoc && num_potNewLoc > 0 || num_bestLoc == 0 ){
			return potentialNewLocation;
		}else if(num_bestLoc ==  num_potNewLoc){
			num_potNewLoc = rc.senseNearbyGameObjects(Robot.class, potentialNewLocation, 6, this.opponentTeam).length;
			num_bestLoc = rc.senseNearbyGameObjects(Robot.class, currentBestLocation, 6, this.opponentTeam).length;
			if (num_bestLoc > num_potNewLoc && num_potNewLoc>0 || num_bestLoc == 0){
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
}
