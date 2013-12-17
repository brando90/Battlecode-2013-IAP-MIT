package StrategyPlayer2;

import battlecode.common.*;

public class SmallSoldierPlayer extends BasePlayer{
	private State state = State.EncampmentCapture;
	private NavigationController nav;
	private MapLocation waypoint;
	private int initialMineCount;
	private MapLocation[] targetEncampments;
	private MapLocation[] potentialEncampments;
	private MapLocation rendezvous;

	private enum State{
		First,Second,EncampmentCapture,MiniSwarm
	}

	public SmallSoldierPlayer(RobotController rc){
		super(rc);
		this.nav = new NavigationController(rc);
		if (Clock.getRoundNum() < 10){
			rc.setIndicatorString(2, "First Soldier");
			this.state = State.First;
		} else if (Clock.getRoundNum() < 20){
			rc.setIndicatorString(2, "Second Soldier");
			this.state = State.Second;
		} else{
			rc.setIndicatorString(2, "General Soldier");
			this.rendezvous = new MapLocation((3*this.HQLocation.x+this.enemyHQLocation.x)/4, (3*this.HQLocation.y+this.enemyHQLocation.y)/4);
			this.state = State.MiniSwarm;
		}
		this.initialMineCount = rc.senseMineLocations(this.mapCenter, this.mapRadiusSq, Team.NEUTRAL).length;
		try{
			this.targetEncampments = this.getTargetEncampments();
		} catch (GameActionException e){

		}
	}

	public void run() throws GameActionException {
		this.currentLocation = rc.getLocation();
		switch (this.state){
		case First:
			if (waypoint == null){
				if (3*this.initialMineCount > this.mapArea){
					//High mine density
					this.waypoint = this.targetEncampments[0];
				} else{
					//Low mine density
					if (this.viablePositionsInArtilleryArea()>=1){
						for (MapLocation encampment : this.targetEncampments){
							if (this.inArtilleryArea(encampment)){
								this.waypoint = encampment;
							}
						}
					} else{
						this.waypoint = this.targetEncampments[0];
					}
				}
			} else if (this.currentLocation.equals(waypoint)){ 
				if (3*this.initialMineCount > this.mapArea){
					//High mine density
					//Build Supplier first
					try{
						rc.captureEncampment(RobotType.SUPPLIER);
					} catch (GameActionException e){

					}
				} else{
					//Low mine density
					//Build Artillery first
					if (this.inArtilleryArea(this.currentLocation)){
						try{
							rc.captureEncampment(RobotType.ARTILLERY);
						} catch (GameActionException e){}
					} else{
						if (this.couldBeArtilleryPosition(this.currentLocation)){
							if (this.viablePositionsInArtilleryArea()<2){
								try{
									rc.captureEncampment(RobotType.ARTILLERY);
								} catch (GameActionException e){}
							} else{
								try{
									rc.captureEncampment(RobotType.SUPPLIER);
								} catch (GameActionException e){}
							}
						} else{
							try{
								rc.captureEncampment(RobotType.SUPPLIER);
							} catch (GameActionException e){}
						}
					}
				}

			} else{
				this.nav.evalMove(this.currentLocation, waypoint);
			}
			break;
		case Second:
			if (waypoint == null){
				this.waypoint = this.nextClosestAvailableEncampment();
			} else if (this.currentLocation.equals(waypoint)){
				try{
					rc.captureEncampment(RobotType.SUPPLIER);
				} catch (GameActionException e){

				}
			} else{
				this.nav.evalMove(this.currentLocation, this.waypoint);
			}
			break;
		case MiniSwarm:
			if (this.waypoint == null){
					rc.setIndicatorString(1, ""+this.turnsPerSpawn());
					if (Clock.getRoundNum()%(20*this.turnsPerSpawn()) == 0){
						this.waypoint = this.enemyHQLocation;
						this.nav.progressMove(this.waypoint);
					} else{
						this.nav.progressMove(this.rendezvous);
					}
			} else{
				this.nav.progressMove(this.waypoint);
			}
			break;
		case EncampmentCapture:
			break;
		default:
			break;
		}
	}
	
	private MapLocation nextClosestAvailableEncampment(){
		for (int i = 0;i < this.targetEncampments.length;i++){
			try{
				if (rc.senseObjectAtLocation(this.targetEncampments[i]) == null){
					return this.targetEncampments[i];
				}

			} catch (GameActionException e){}
		}
		return null;
	}
	
	private int turnsPerSpawn() throws GameActionException{
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class, 4*this.mapRadiusSq, this.myTeam);
		int supplierCount = 0;
		for (Robot alliedRobot : alliedRobots){
			if (rc.senseRobotInfo(alliedRobot).type == RobotType.SUPPLIER){
				supplierCount+=1;
			}
		}
		switch (supplierCount){
		case (0):
			return 10;
		case (1):
			return 9;
		case (2):
		case (3):
			return 8;
		case (4): 
		case (5):
			return 7;
		case (6):
		case (7):
			return 6;
		default:
			return 10;
		}
	}

	private MapLocation[] getTargetEncampments() throws GameActionException{
		this.potentialEncampments = this.getPotentialEncampments();
		this.buildheap(this.potentialEncampments);
		MapLocation[] targetEncampments = new MapLocation[Math.min(7, this.potentialEncampments.length)];
		for (int i = 0; i<targetEncampments.length;i++){
			targetEncampments[i] = this.popMin();
		}
		return targetEncampments;		
	}

	private MapLocation[] getPotentialEncampments() throws GameActionException{
		MapLocation[] potentialNeutralEncampments = rc.senseEncampmentSquares(this.HQLocation, 63, Team.NEUTRAL);
		MapLocation[] potentialAlliedEncampments = rc.senseAlliedEncampmentSquares();
		int combinedLength = potentialNeutralEncampments.length + potentialAlliedEncampments.length;
		MapLocation[] potentialEncampments = new MapLocation[combinedLength];
		for (int i = 0; i < combinedLength; i++){
			if (i < potentialNeutralEncampments.length){
				potentialEncampments[i] = potentialNeutralEncampments[i];
			} else{
				potentialEncampments[i] = potentialAlliedEncampments[i-potentialNeutralEncampments.length];
			}
		}
		return potentialEncampments;
	}

	private boolean couldBeArtilleryPosition(MapLocation position){
		if (position.distanceSquaredTo(this.HQLocation) < 4){
			return true;
		} else{
			return this.inArtilleryArea(position);
		}
	}

	private boolean inArtilleryArea(MapLocation position){
		if (5*position.distanceSquaredTo(HQLocation) > 2*this.HQLocation.distanceSquaredTo(this.enemyHQLocation)){
			return false;
		} else{
			switch(this.HQLocation.directionTo(enemyHQLocation)){
			case NORTH:
				if (position.x <= this.HQLocation.x){
					return position.y - this.HQLocation.y < -position.x + this.HQLocation.x;
				} else{
					return position.y - this.HQLocation.y < position.x - this.HQLocation.x;
				}
			case SOUTH:
				if (position.x <= this.HQLocation.x){
					return position.y - this.HQLocation.y > position.x - this.HQLocation.x;
				} else{
					return position.y - this.HQLocation.y > -position.x + this.HQLocation.x;
				}
			case WEST:
				if (position.y >= this.HQLocation.y){
					return position.y - this.HQLocation.y > -position.x + this.HQLocation.x;
				} else{
					return position.y - this.HQLocation.y < position.x - this.HQLocation.x;
				}
			case EAST:
				if (position.y >= this.HQLocation.y){
					return position.y - this.HQLocation.y > position.x - this.HQLocation.x;
				} else{
					return position.y - this.HQLocation.y < -position.x + this.HQLocation.x;
				}
			case NORTH_WEST:
				return (position.y < this.HQLocation.y) && (position.x < this.HQLocation.x);
			case SOUTH_EAST:
				return (position.y > this.HQLocation.y) && (position.x > this.HQLocation.x);
			case NORTH_EAST:
				return (position.y < this.HQLocation.y) && (position.x > this.HQLocation.x);
			case SOUTH_WEST:
				return (position.y > this.HQLocation.y) && (position.x < this.HQLocation.x);
			default:
				return false;
			}
		}
	}

	private int viablePositionsInArtilleryArea(){
		int viablePositionsCount = 0;
		for (MapLocation encampment : this.targetEncampments){
			if (this.inArtilleryArea(encampment)){
				viablePositionsCount += 1;
			}
		}
		return viablePositionsCount;
	}

	private int totalArtillery() throws GameActionException{
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class, 4*this.mapRadiusSq, this.myTeam);
		int artilleryCount = 0;
		for (Robot alliedRobot : alliedRobots){
			if (rc.senseRobotInfo(alliedRobot).type == RobotType.ARTILLERY){
				artilleryCount+=1;
			}
		}
		return artilleryCount + rc.readBroadcast(ArtilleryInProgressCountChannel);
	}

	private int totalSuppliers() throws GameActionException{
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class, 4*this.mapRadiusSq, this.myTeam);
		int supplierCount = 0;
		for (Robot alliedRobot : alliedRobots){
			if (rc.senseRobotInfo(alliedRobot).type == RobotType.SUPPLIER){
				supplierCount+=1;
			}
		}
		return supplierCount + rc.readBroadcast(SupplierInProgressCountChannel);
	}

	protected void buildheap(MapLocation[] encampments){
		int size = encampments.length;

		for(int i=size/2; i>=0; i--){
			MinHeapify(encampments, i);
		}

	}

	protected void MinHeapify(MapLocation[] encampments, int i){
		int left = 2*i;
		int right = 2*i+1;

		int smallest;


		if(left < encampments.length && this.HQLocation.distanceSquaredTo(encampments[left]) < this.HQLocation.distanceSquaredTo(encampments[i])){
			smallest = left;
		} else{
			smallest = i;
		}

		if(right < encampments.length && this.HQLocation.distanceSquaredTo(encampments[right]) < this.HQLocation.distanceSquaredTo(encampments[smallest])){
			smallest = right;
		}

		if(smallest != i){
			MapLocation temp = encampments[i];
			encampments[i] = encampments[smallest];
			encampments[smallest] = temp;
			MinHeapify(encampments, smallest);
		}
	}

	protected MapLocation popMin(){
		if(this.potentialEncampments.length == 0){
			return null;
		}else{
			MapLocation min = this.potentialEncampments[0];
			MapLocation[] updated_list = new MapLocation[this.potentialEncampments.length-1];
			for(int i=1; i<this.potentialEncampments.length; i++){
				updated_list[i-1] = this.potentialEncampments[i];
			}

			buildheap(updated_list);
			this.potentialEncampments = updated_list;
			return min;
		}
	}
}
