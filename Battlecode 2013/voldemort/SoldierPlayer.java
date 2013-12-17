package voldemort;

import battlecode.common.*;

public class SoldierPlayer extends BasePlayer {

	private NavigationController nav;
	private MapLocation waypoint;

	private enum State {
		EncampmentCapture,Swarm,Micro,Defuse;
	}

	private State state;

	public SoldierPlayer(RobotController rc) {
		super(rc);
		nav = new NavigationController(rc);
		this.currentLocation = rc.getLocation();
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
        rc.setIndicatorString(0, strategy.toString());
        
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
					nav.progressMove(this.waypoint);
				}
			} else if (this.currentLocation.equals(this.waypoint)){
				try{
					//int alliedEncampmentCount = rc.senseAlliedEncampmentSquares().length;
					//double captureCost = GameConstants.CAPTURE_POWER_COST*(1+(alliedEncampmentCount));
					if (mapLocationFromInt(rc.readBroadcast(MedbayLocationChannel)) == null){
						MapLocation rendezVous = mapLocationFromInt(rc.readBroadcast(RendezVousChannel));
						int distanceFromRV = this.currentLocation.distanceSquaredTo(rendezVous);
						int distFromHQ = this.currentLocation.distanceSquaredTo(this.HQLocation);
						if (distanceFromRV > 30 && distFromHQ < rendezVous.distanceSquaredTo(HQLocation)){
							try{
								rc.captureEncampment(RobotType.MEDBAY);
								rc.broadcast(MedbayLocationChannel, roundNumIntFromLocation(this.currentLocation));
							} catch (GameActionException e){
								this.state = State.Swarm;
							}
						} else{
							try{
								RobotType encampment = this.getEncampmentType(currentLocation);
								rc.captureEncampment(encampment);
								this.broadcastToCountChannel(encampment);
							} catch (GameActionException e){
								//wait
							}
						}
					} else{
						try{
							RobotType encampment = this.getEncampmentType(currentLocation);
							rc.captureEncampment(encampment);
							this.broadcastToCountChannel(encampment);
						} catch (GameActionException e){//Couldn't build (not enough power)
							//wait
						}
					}

				} catch (GameActionException e){//Need more power to capture encampments
					this.state = State.Swarm;
				}
			} else{
				nav.progressMove(this.waypoint);
			}
			break;

		case Swarm:
			rc.setIndicatorString(2, "Swarm");
			Robot[] enemyRobotsInSharedVision = rc.senseNearbyGameObjects(Robot.class, 4*this.mapRadiusSq, this.myTeam.opponent());
			try{
			    MapLocation medbay = mapLocationFromInt(rc.readBroadcast(MedbayLocationChannel));
			    if (rc.getEnergon() < 30 && medbay != null){
			        if (this.roundNumValid(roundNumFromInt(rc.readBroadcast(MedbayLocationChannel)))){
			            nav.progressMove(medbay);
			        }
			    } else {
			        MapLocation rendezVous = mapLocationFromInt(rc.readBroadcast(RendezVousChannel));
			        if (enemyRobotsInSharedVision.length == 0){
			            if (rc.getEnergon() < 40 && medbay != null){
			                if (this.roundNumValid(roundNumFromInt(rc.readBroadcast(MedbayLocationChannel)))){
			                    nav.progressMove(medbay);
			                }
			            } else{
			                nav.progressMove(rendezVous);
			            }
			        } else{
			            MapLocation closestRobotToDL = getClosestRobotLocation(enemyRobotsInSharedVision,this.HQLocation);
			            nav.progressMove(closestRobotToDL);
			        }			
			    }
			}catch(GameActionException e){

			}
			break;

		case Micro:
			rc.setIndicatorString(2, "Micro");
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

	private void broadcastToCountChannel(RobotType encampment) throws GameActionException{
		switch(encampment){
		case SUPPLIER:
			rc.broadcast(SupplierInProgressCountChannel, rc.readBroadcast(SupplierInProgressCountChannel) + 1);
			break;
		case GENERATOR:
			rc.broadcast(GeneratorInProgressCountChannel, rc.readBroadcast(GeneratorInProgressCountChannel) + 1);
			break;
		default:
			break;
		}
	}
	
	private RobotType getEncampmentType(MapLocation encampment) throws GameActionException{
		//Needs to take count of currently being built encampments
		if (this.canBeArtilleryPosition(encampment)){//Consider building artillery
			if (this.getPredictedPowerGenerationRate() < 65){
				return RobotType.GENERATOR;
			} else{
				return RobotType.ARTILLERY;
			}
		} else{//build something else...
			return build();
		}
	}
	
	private RobotType build() throws GameActionException {
        int sup = rc.readBroadcast(SupplierInProgressCountChannel);
        int gen = rc.readBroadcast(GeneratorInProgressCountChannel);
        switch(strategy){
        case ECON:
            if (gen < sup + 2){
                return RobotType.GENERATOR;
            }else{
                return RobotType.SUPPLIER;
            }
        case RUSH:
            if (sup < gen + 4){
                return RobotType.SUPPLIER;
            }else{
                return RobotType.GENERATOR;
            }
        default:
            return RobotType.GENERATOR;
        }
    }

	private boolean canBeArtilleryPosition(MapLocation position){
		Direction directionToHQ = this.currentLocation.directionTo(HQLocation);
		Direction directionToEnemyHQ = this.currentLocation.directionTo(enemyHQLocation);
		return directionToHQ.opposite() == directionToEnemyHQ;
	}

	private MapLocation getClosestRobotLocation(Robot[] robots, MapLocation reference) throws GameActionException{
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
}
