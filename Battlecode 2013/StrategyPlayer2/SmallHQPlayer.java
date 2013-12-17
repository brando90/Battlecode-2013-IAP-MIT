package StrategyPlayer2;

import battlecode.common.*;

public class SmallHQPlayer extends BasePlayer{

	private MapLocation[] encampments;
	
	public SmallHQPlayer(RobotController rc){
		super(rc);
		try{
			this.encampments = rc.senseEncampmentSquares(HQLocation, 4*mapRadiusSq, Team.NEUTRAL);
			
		} catch (GameActionException E){
			
		}
	}

	public void run() throws GameActionException {		
		if (rc.isActive()){//Spawn or Research
			//Initial research
			if (!this.initialResearch()){
				Direction spawnDir = this.getSpawnDir();
				if (spawnDir == null){
					//rc.setIndicatorString(2, "No valid spawn direction");
					this.research();
				} else{
					//rc.setIndicatorString(2, "Found valid spawn direction");
					if (rc.getTeamPower() - 40 + this.getPowerGenerationRate() > 10){ // find accurate metric
						rc.spawn(spawnDir);
					} else{
						this.research();
					}
				}
			}
		}	
	}
	
	private boolean initialResearch(){
		return false;
	}
	
	private void research() throws GameActionException{
		if (!rc.hasUpgrade(Upgrade.NUKE)){
			rc.researchUpgrade(Upgrade.NUKE);
		}
	}

	private Direction getSpawnDir(){
		Direction directDir = this.HQLocation.directionTo(enemyHQLocation);
		int[] spawningOffsets = {0,1,-1,2,-2,3,-3,4};
		for (int offset : spawningOffsets){
			Direction potentialSpawnDir = Direction.values()[(directDir.ordinal()+offset+8)%8];
			MapLocation potentialSpawnLocation = this.HQLocation.add(potentialSpawnDir);
			if (rc.canMove(potentialSpawnDir) && !this.mineHazard(potentialSpawnLocation)){
				return potentialSpawnDir;
			}
		}
		return null;
	}
	
}
