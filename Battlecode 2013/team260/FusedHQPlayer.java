package team260;

import battlecode.common.*;

public class FusedHQPlayer extends BasePlayer {

	private Direction directionToEnemyHQ;
	private int initialMineCount;
	private int mineThresholdCoeff = 3;
	private MapLocation spawnSquare;
	private boolean needMorePower = false;
	private boolean needVision = false;
	private MapLocation[] targetEncampments;

	private int targetEncampmentsStart = generator.nextInt(GameConstants.BROADCAST_MAX_CHANNELS);

	public FusedHQPlayer(RobotController rc) {
		super(rc);
		this.currentLocation = rc.getLocation();
		this.directionToEnemyHQ = this.currentLocation.directionTo(enemyHQLocation);
		this.initialMineCount = rc.senseMineLocations(currentLocation, 10000, Team.NEUTRAL).length;		


		try{			
			if (this.canBeTrapped()){
				rc.setIndicatorString(2, "Can be trapped");
				this.markSpawnSquare();
			} else{
				rc.setIndicatorString(2, "No trap sensed");
			}

			//pick encampments on our side of the map

			rc.broadcast(TargetEncampmentsStartChannel, targetEncampmentsStart);
			this.targetEncampments = rc.senseEncampmentSquares(currentLocation, this.currentLocation.distanceSquaredTo(enemyHQLocation)/9, Team.NEUTRAL);
			
			//int a = Clock.getBytecodeNum();
			QuicksortLocations(this.targetEncampments, this.currentLocation);
			if (targetEncampments.length > 10){
				MapLocation[] temp = new MapLocation[10];
				for (int i = 0; i <temp.length;i++){
					temp[i] = this.targetEncampments[i];
				}
				this.targetEncampments = temp;
			}
			//int b = Clock.getBytecodeNum();
			//rc.setIndicatorString(1, "Bytecodes: "+(b-a));

			this.broadcastTargetEncampments();
		} catch (GameActionException e) {
			e.printStackTrace();
		}

	}

	public void run() throws GameActionException {
		//rc.broadcast(DefuseSpawnSquareChannel, intFromMapLocation(this.spawnSquare)); //Broadcast every turn to be extra safe... may not be needed

		if (this.isThreatened()){
			//Broadcast for help!
			rc.broadcast(HQThreatenedChannel, intFromBoolean(true)); //Can save bytecodes without fctn call
		} else{
			rc.broadcast(HQThreatenedChannel, intFromBoolean(false)); //Can save bytecodes without fctn call
		}
		if (rc.senseEnemyNukeHalfDone()){
			if (rc.checkResearchProgress(Upgrade.NUKE) < 200){
				rc.broadcast(EnemyNukeHalfDoneChannel, intFromBoolean(true));
			}
		}
		if (rc.isActive()){//Spawn or Research
			//Initial research
			if (!this.initialResearch()){
				Direction spawnDir = this.getSpawnDir();
				if (spawnDir == null){
					//rc.setIndicatorString(2, "No valid spawn direction");
					this.research();
				} else{
					//rc.setIndicatorString(2, "Found valid spawn direction");
					int powerGenRate = this.getPowerGenerationRate();
					if (rc.getTeamPower() - 40 + powerGenRate > 10 && powerGenRate > 0){ // find accurate metric
						rc.spawn(spawnDir);
					} else{
						this.research();
					}
				}
			}
		}	
	}

	private void broadcastTargetEncampments() throws GameActionException {
		int currentChannel = targetEncampmentsStart;
		for (MapLocation encampment : this.targetEncampments){
			if (this.spawnSquare == null){
				rc.broadcast(currentChannel++, intFromMapLocation(encampment));
			} else if (!encampment.equals(this.spawnSquare)){
				rc.broadcast(currentChannel++, intFromMapLocation(encampment));
			}
		}
	}

	private boolean initialResearch() throws GameActionException{
		return false;
		/*if (!rc.hasUpgrade(Upgrade.PICKAXE)){
			if (this.mineThresholdCoeff*this.initialMineCount < this.mapArea){
				rc.researchUpgrade(Upgrade.PICKAXE);
				return true;
			}
		}
		return false;*/
	}

	private void markSpawnSquare() throws GameActionException{
		if (this.canBeTrappedByOnlyEncampments()){//Don't commission encampment on spawn square
			rc.setIndicatorString(1, "Trapped by just encampments");
			this.spawnSquare = currentLocation.add(this.getDirectSpawnDir());
		} else{//Commission a mine defusion on spawn square
			rc.setIndicatorString(1, "Trapped by mines/encampments");
			Direction directDir = this.getDirectSpawnDir();
			int[] spawningOffsets = {0,1,-1,2,-2,3,-3,4};
			SpawnSquareSearch: for (int offset : spawningOffsets){
				Direction potentialSpawnDir = Direction.values()[(directDir.ordinal()+offset+8)%8];
				MapLocation potentialSpawnSquare = this.currentLocation.add(potentialSpawnDir);
				if (this.mineHazard(potentialSpawnSquare)){
					this.spawnSquare = potentialSpawnSquare;
					rc.broadcast(DefuseSpawnSquareChannel, intFromMapLocation(this.spawnSquare));
					break SpawnSquareSearch;
				}
			}
		}
		rc.setIndicatorString(2, "Spawn Square: "+ this.spawnSquare.toString());
	}

	private boolean canBeTrappedByOnlyEncampments(){
		for (int i = 0; i < 8; i++){
			Direction dir = Direction.values()[i];
			MapLocation adjacent = this.currentLocation.add(dir);
			if (!rc.senseEncampmentSquare(adjacent) && rc.senseTerrainTile(adjacent) != TerrainTile.OFF_MAP){
				return false;
			}
		}
		return true;
	}

	private boolean canBeTrapped(){
		for (int i = 0; i < 8; i++){
			Direction dir = Direction.values()[i];
			MapLocation adjacent = this.currentLocation.add(dir);
			if (rc.senseTerrainTile(adjacent) != TerrainTile.OFF_MAP){
				if (!rc.senseEncampmentSquare(adjacent) && !this.mineHazard(adjacent)){
					return false;
				}
			}
		}
		return true;
	}

	private boolean isThreatened(){
		Robot[] enemyRobotsNearHQ = rc.senseNearbyGameObjects(Robot.class, currentLocation, this.getSightRadiusSq(), myTeam.opponent());
		return enemyRobotsNearHQ.length > 0;
	}

	private void research() throws GameActionException{
		//DEFUSION handled in initial research
		/*if (!rc.hasUpgrade(Upgrade.DEFUSION)){
			if (this.mineThresholdCoeff*this.initialMineCount > this.mapArea){
				rc.researchUpgrade(Upgrade.DEFUSION);	
				return;
			}
		} */
		/*if (!rc.hasUpgrade(Upgrade.FUSION)){
			if (!this.needMorePower){
				this.needMorePower = (this.getPowerGenerationRate() <= 0);
			}
			if (this.needMorePower){
				rc.researchUpgrade(Upgrade.FUSION);
				return;
			}
		}
		if (!rc.hasUpgrade(Upgrade.VISION)){
			if (this.needVision){
				rc.researchUpgrade(Upgrade.VISION);
				return;
			}
		}*/
		rc.researchUpgrade(Upgrade.NUKE);
	}

	private Direction getSpawnDir(){
		Direction directDir = this.getDirectSpawnDir();
		int[] spawningOffsets = {0,1,-1,2,-2,3,-3,4};
		for (int offset : spawningOffsets){
			Direction potentialSpawnDir = Direction.values()[(directDir.ordinal()+offset+8)%8];
			MapLocation potentialSpawnLocation = this.currentLocation.add(potentialSpawnDir);
			if (rc.canMove(potentialSpawnDir) && !this.mineHazard(potentialSpawnLocation)){
				return potentialSpawnDir;
			}
		}
		return null;
	}

	private Direction getDirectSpawnDir() {
		return this.directionToEnemyHQ;
	}

}
