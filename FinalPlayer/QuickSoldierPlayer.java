package FinalPlayer;

import battlecode.common.*;



public class QuickSoldierPlayer extends BasePlayer {

	private NavigationController nav;
	private MapLocation waypoint;
	private MapLocation rendezvous;
	private boolean atRV = false;
	private boolean assignedToMedbay = false;

	private enum State {
		EncampmentCapture,Swarm,Micro,Defuse;
	}

	private State state;
	private State previousState;


	public QuickSoldierPlayer(RobotController rc) {
		super(rc);
		nav = new NavigationController(rc);
		this.currentLocation = rc.getLocation();
		this.rendezvous = this.HQLocation.add(this.HQLocation.directionTo(this.enemyHQLocation), 2);
		try {
			this.waypoint = mapLocationFromInt(rc.readBroadcast(DefuseSpawnSquareChannel));
			if (this.waypoint == null){
				this.state = State.EncampmentCapture;
			} else{
				this.state = State.Defuse;
			}
		} catch (GameActionException e) {
			this.state = State.Swarm;
			e.printStackTrace();
		}
	}
	public void run() throws GameActionException{
		this.currentLocation = rc.getLocation();

		if (this.state != State.Micro){
			if (rc.senseNearbyGameObjects(Robot.class, this.currentLocation, 2, this.myTeam.opponent()).length > 0){
				this.previousState = this.state;
				this.state = State.Micro;
			}
		}
		//Decide if we should switch state
		switch(this.state){
		case EncampmentCapture:
			rc.setIndicatorString(2, "EncampmentCapture");
			if (this.waypoint == null){
				int targetEncampmentsStart = rc.readBroadcast(TargetEncampmentsStartChannel);
				//rc.setIndicatorString(0, " to read from channel: "+targetEncampmentsStart);
				MapLocation targetEncampment = mapLocationFromInt(rc.readBroadcast(targetEncampmentsStart));
				if (targetEncampment == null){
					this.state = State.Swarm;
				} else{
					rc.setIndicatorString(1, "Target Encampment: "+targetEncampment.toString());
					this.waypoint = targetEncampment;
					rc.broadcast(TargetEncampmentsStartChannel, targetEncampmentsStart+1);
					nav.evalMove(this.currentLocation, this.waypoint);
				}
			} else if (this.currentLocation.equals(this.waypoint)){
				try{
					//int alliedEncampmentCount = rc.senseAlliedEncampmentSquares().length;
					//double captureCost = GameConstants.CAPTURE_POWER_COST*(1+(alliedEncampmentCount));
					if (mapLocationFromInt(rc.readBroadcast(MedbayLocationChannel)) == null || this.assignedToMedbay){
						int distFromHQ = this.nav.movesAway(this.currentLocation,this.HQLocation);
						if (distFromHQ < 5*this.nav.movesAway(this.rendezvous,HQLocation)){
							try{
								rc.captureEncampment(RobotType.MEDBAY);
								rc.broadcast(MedbayLocationChannel, roundNumIntFromLocation(this.currentLocation));
							} catch (GameActionException e){
								this.state = State.Swarm;
							}
						} else{
							try{
								RobotType encampment = this.getEncampmentType(currentLocation);
								if (encampment == null){
									this.state = State.Swarm;
									this.nav.progressMove(this.rendezvous);
								} else{
									rc.captureEncampment(encampment);
									this.broadcastToCountChannel(encampment);
								}
							} catch (GameActionException e){
								//wait
							}
						}
					} else{
						try{
							RobotType encampment = this.getEncampmentType(currentLocation);
							if (encampment == null){
								this.state = State.Swarm;
								this.nav.progressMove(this.rendezvous);
							} else{
								rc.captureEncampment(encampment);
								this.broadcastToCountChannel(encampment);
							}
						} catch (GameActionException e){//Couldn't build (not enough power)
							//wait
						}
					}

				} catch (GameActionException e){//Need more power to capture encampments
					this.state = State.Swarm;
				}
			} else{
				nav.evalMove(this.currentLocation,this.waypoint);
			}
			break;

		case Swarm:
			rc.setIndicatorString(2, "Swarm");
			if (booleanFromInt(rc.readBroadcast(HQThreatenedChannel))){
				Robot[] threateningRobots = rc.senseNearbyGameObjects(Robot.class, getSightRadiusSq(), this.myTeam.opponent());
				MapLocation closestRobotToHQ = getClosestRobotLocation(threateningRobots,this.HQLocation);
				if (closestRobotToHQ == null){
					this.nav.progressMove(this.rendezvous);
				} else{
					nav.progressMove(closestRobotToHQ);
				}
			} else if (booleanFromInt(rc.readBroadcast(EnemyNukeHalfDoneChannel))){
				nav.progressMove(enemyHQLocation);
			} else{
				Robot[] enemyRobotsInSharedVision = rc.senseNearbyGameObjects(Robot.class, 4*this.mapRadiusSq, this.myTeam.opponent());
				MapLocation medbay = mapLocationFromInt(rc.readBroadcast(MedbayLocationChannel));
				if (rc.getEnergon() < 30 && medbay != null){
					try{
						if (this.roundNumValid(roundNumFromInt(rc.readBroadcast(MedbayLocationChannel)))){
							nav.progressMove(medbay);
						} else{
							if (enemyRobotsInSharedVision.length == 0){
								MapLocation[] medbayLocations = rc.senseEncampmentSquares(this.HQLocation, 63, Team.NEUTRAL);
								waypoint = this.getClosestEncampmentToHQLocation(medbayLocations);
								if (waypoint != null){
									this.assignedToMedbay = true;
									this.state = State.EncampmentCapture;
								} else{
									this.nav.progressMove(this.rendezvous);
								}
							} else{
								this.atRV = false;
								MapLocation closestRobotToDL = getClosestRobotLocation(enemyRobotsInSharedVision,this.HQLocation);
								nav.progressMove(closestRobotToDL);
							}
						}
					} catch (GameActionException e){
						if (enemyRobotsInSharedVision.length == 0){
							nav.progressMove(this.rendezvous);
						} else{
							this.atRV = false;
							MapLocation closestRobotToDL = getClosestRobotLocation(enemyRobotsInSharedVision,this.HQLocation);
							nav.progressMove(closestRobotToDL);
						}
					}
				} else {	
					if (enemyRobotsInSharedVision.length == 0){
						if (rc.getEnergon() < 40 && medbay != null){
							try{
								if (this.roundNumValid(roundNumFromInt(rc.readBroadcast(MedbayLocationChannel)))){
									nav.progressMove(medbay);
								} else{
									this.layMineOrGoToRV(this.rendezvous);
								}
							} catch (GameActionException e){
								this.layMineOrGoToRV(this.rendezvous);
							}
						} else{
							this.layMineOrGoToRV(this.rendezvous);
						}
					} else{
						this.atRV = false;
						MapLocation closestRobotToDL = getClosestRobotLocation(enemyRobotsInSharedVision,this.HQLocation);
						nav.progressMove(closestRobotToDL);
					}			
				}
			}
			break;



		case Micro:
			this.atRV = false;
			rc.setIndicatorString(2, "Micro");
			if (rc.senseNearbyGameObjects(Robot.class, this.currentLocation, 2, this.myTeam.opponent()).length == 0){
				this.state = this.previousState;
				this.previousState = null;
				if (this.state == State.Swarm){
					this.nav.progressMove(this.rendezvous);
				}
			} else{

				Direction potentialDirectionMovement;
				MapLocation potentialNewLocation;
				MapLocation bestBattleLocation;
				bestBattleLocation = this.currentLocation;
				Direction[] directions = Direction.values();
				for (int i = 8; --i>= 0;){
					potentialDirectionMovement = directions[i];
					potentialNewLocation = this.currentLocation.add(potentialDirectionMovement);
					Team mine = rc.senseMine(potentialNewLocation);
					if (rc.canMove(potentialDirectionMovement) && !(mine == this.myTeam.opponent() || mine == Team.NEUTRAL)){
						potentialNewLocation = this.currentLocation.add(potentialDirectionMovement);
						bestBattleLocation = this.statEvalRadSizeOne(potentialNewLocation, bestBattleLocation);
					}
				}
				Direction directionBetterBalltlePos = this.currentLocation.directionTo(bestBattleLocation);
				if (directionBetterBalltlePos != Direction.NONE && directionBetterBalltlePos != Direction.OMNI){
					if (rc.isActive()){
						rc.move(directionBetterBalltlePos);
					}
				}
			}
			break;

		case Defuse:
			rc.setIndicatorString(2,"Defuse");
			if (this.currentLocation.isAdjacentTo(this.waypoint)){
				if (this.mineHazard(this.waypoint)){
					rc.defuseMine(this.waypoint);
				}
				rc.broadcast(DefuseSpawnSquareChannel, 0);
				this.waypoint = null;
				this.state = State.EncampmentCapture;
			} else{
				nav.progressMove(this.waypoint);
			}
			break;
		}
	}

	private void layMineOrGoToRV(MapLocation rendezvous) throws GameActionException {
		if (!this.atRV && this.currentLocation.distanceSquaredTo(rendezvous) <=2){
			this.atRV = true;
		}
		if (this.atRV && rc.senseMine(this.currentLocation) == null){
			if (rc.isActive()){
				rc.layMine();
			}
		} else{
			this.nav.progressMove(rendezvous);
		}
	}

	private MapLocation statEvalRadSizeOne(MapLocation potentialNewLocation, MapLocation currentBestLocation) throws GameActionException {
		Robot[] potNewLocRobots = rc.senseNearbyGameObjects(Robot.class, potentialNewLocation, 2, this.myTeam.opponent());
		Robot[] bestLocRobots = rc.senseNearbyGameObjects(Robot.class, currentBestLocation, 2, this.myTeam.opponent());
		int num_potNewLoc = potNewLocRobots.length;
		int num_bestLoc = bestLocRobots.length;
		if (num_bestLoc > num_potNewLoc && num_potNewLoc > 0 || num_bestLoc == 0 ){
			return potentialNewLocation;
		}else if(num_bestLoc ==  num_potNewLoc){
			Robot currBot;
			int tot_alliedPOT = 0;
			for (int i = num_potNewLoc; --i>=0;){
				currBot = potNewLocRobots[i];
				tot_alliedPOT += rc.senseNearbyGameObjects(Robot.class, rc.senseRobotInfo(currBot).location, 2, this.myTeam.opponent()).length;
			}
			int tot_alliedBEST = 0;
			for (int i = num_potNewLoc; --i>=0;){
				currBot = potNewLocRobots[i];
				tot_alliedBEST += rc.senseNearbyGameObjects(Robot.class, rc.senseRobotInfo(currBot).location, 2, this.myTeam.opponent()).length;
			}
			if(tot_alliedBEST > tot_alliedPOT){
				return currentBestLocation;
			} else{
				return potentialNewLocation;
			}
		}
		return currentBestLocation;
	}

	private void broadcastToCountChannel(RobotType encampment) throws GameActionException{
		switch(encampment){
		case SUPPLIER:
			rc.broadcast(SupplierInProgressCountChannel, rc.readBroadcast(SupplierInProgressCountChannel) + 1);
			break;
		case GENERATOR:
			rc.broadcast(ArtilleryInProgressCountChannel, rc.readBroadcast(ArtilleryInProgressCountChannel) + 1);
			break;
		default:
			break;
		}
	}

	private RobotType getEncampmentType(MapLocation encampment) throws GameActionException{
		//Needs to take count of currently being built encampments
		if (rc.senseEncampmentSquares(this.mapCenter, HQLocation.distanceSquaredTo(enemyHQLocation)/4, Team.NEUTRAL).length <= 7){
			if (this.currentLocation.distanceSquaredTo(mapCenter) < HQLocation.distanceSquaredTo(mapCenter)){
				return RobotType.ARTILLERY;
			}
		}
		if (this.canBeArtilleryPosition(encampment)){//Consider building artillery
			if (this.getArtilleryCount() < 3){
				return RobotType.ARTILLERY;
			}
		}
		return null;
	}


	private boolean canBeArtilleryPosition(MapLocation position){
		Direction directionToHQ = this.currentLocation.directionTo(HQLocation);
		Direction directionToEnemyHQ = this.currentLocation.directionTo(enemyHQLocation);
		return directionToHQ.opposite() == directionToEnemyHQ || position.distanceSquaredTo(this.HQLocation) < 9;
	}

	private MapLocation getClosestRobotLocation(Robot[] robots, MapLocation reference) throws GameActionException{
		if (robots.length == 0){
			return null;
		} else{
			RobotInfo currentRobotInfo = rc.senseRobotInfo(robots[0]);
			MapLocation closestRobot = currentRobotInfo.location;
			int closestDist = this.nav.movesAway(reference,closestRobot);
			int distFromRobot;
			for (Robot robot : robots){
				currentRobotInfo = rc.senseRobotInfo(robot);
				distFromRobot = this.nav.movesAway(reference,currentRobotInfo.location);
				if (distFromRobot < closestDist){
					closestRobot = currentRobotInfo.location;
					closestDist = distFromRobot;
				}
			}
			return closestRobot;
		}
	}

	private MapLocation getClosestEncampmentToHQLocation(MapLocation[] locations) throws GameActionException{
		if (locations.length == 0){
			return null;
		} else{
			MapLocation closestEncampment = locations[0];
			int closestDist = this.nav.movesAway(this.HQLocation,closestEncampment);
			for (MapLocation location : locations){
				int distFromHQ = this.nav.movesAway(this.HQLocation,location);
				if (distFromHQ < closestDist){
					closestEncampment = location;
					closestDist = distFromHQ;
				}
			}
			return closestEncampment;
		}
	}

}

