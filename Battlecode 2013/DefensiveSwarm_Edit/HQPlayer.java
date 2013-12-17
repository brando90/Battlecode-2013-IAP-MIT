package DefensiveSwarm_Edit;

import battlecode.common.*;

public class HQPlayer extends BasePlayer {

	private Direction directionToEnemyHQ;
//	private int spawnThresholdPower = 50; //Make better metric
	private MapLocation spawnSquare;
	//private int defuseSpawnSquareInt;
	private boolean needMorePower = false;
	private MapLocation[] targetEncampments;
	private int targetEncampmentsStart = 1; //Needs to be initialized

	public HQPlayer(RobotController rc) {
		super(rc);
		this.currentLocation = rc.getLocation();
		this.directionToEnemyHQ = this.currentLocation.directionTo(enemyHQLocation);
		
		try{
			MapLocation enemyHQLoc = rc.senseEnemyHQLocation();
			int rendezvousX = (int) (15*rc.getLocation().x + enemyHQLoc.x)/16;
			int rendezvousY = (int) (15*rc.getLocation().y + enemyHQLoc.y)/16;
			MapLocation rendezvous = new MapLocation(rendezvousX,rendezvousY);
//			System.out.println("RENDEZVOUS: "+rendezvous.toString());
			rc.broadcast(rendezvousChannel, intFromMapLocation(rendezvous));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try{			
			if (this.canBeTrapped()){
				rc.setIndicatorString(2, "Can be trapped");
				this.markSpawnSquare();
			} else{
				rc.setIndicatorString(2, "No trap sensed");
			}

			//pick encampments on our side of the map

			rc.broadcast(TargetEncampmentsStartChannel, targetEncampmentsStart);
			this.targetEncampments = rc.senseEncampmentSquares(currentLocation, this.mapRadiusSq/4, Team.NEUTRAL);
			//int a = Clock.getBytecodeNum();
			QuicksortLocations(this.targetEncampments, this.currentLocation);
			//int b = Clock.getBytecodeNum();
			//rc.setIndicatorString(1, "Bytecodes: "+(b-a));

			this.broadcastTargetEncampments();
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	private void broadcastTargetEncampments() throws GameActionException {
		int currentChannel = targetEncampmentsStart;
		for (MapLocation encampment : this.targetEncampments){
			if (this.spawnSquare == null){
				rc.broadcast(currentChannel++, intFromMapLocation(encampment));
			} else if (!sameLocations(encampment,this.spawnSquare)){
				rc.broadcast(currentChannel++, intFromMapLocation(encampment));
			}
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
            rc.broadcast(EnemyNukeHalfDoneChannel, intFromBoolean(true));
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
                    if (rc.getTeamPower() - 40 + this.getPowerGenerationRate() > 10){ // find accurate metric
                        rc.spawn(spawnDir);
                    } else{
                        this.research();
                    }
                }
            }
        }   
    }
	
	private boolean initialResearch() throws GameActionException{
	    if (!rc.hasUpgrade(Upgrade.PICKAXE)){
	        rc.researchUpgrade(Upgrade.PICKAXE);
	        return true;
	    }
	    return false;
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
        if (!rc.hasUpgrade(Upgrade.FUSION)){
            if (!this.needMorePower){
                this.needMorePower = (this.getPowerGenerationRate() <= 0);
            }
            if (this.needMorePower){
                rc.researchUpgrade(Upgrade.FUSION);
                return;
            }
        }
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
