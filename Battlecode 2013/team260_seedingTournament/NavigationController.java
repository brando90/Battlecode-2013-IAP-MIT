package team260_seedingTournament;

//import java.util.Random;
//import java.util.*;

import java.util.Arrays;

import battlecode.common.*;

/**
 * 
 * @author brando
 * Caution: Do not create NavigationController instances inside run method because instance variables won't be remembered
 * as expected and unexpected behavior might occur. Instead, NavigationController should be a instance of SoldierPlayer.
 * 
 */
public class NavigationController {
	private RobotController rc;
	private int previousMove = -1;
	//private static int defusionTime = 12;
	
	//private Direction directionToObjective;
	//private MapLocation objectiveLocation;
	
	private int lastIndex = 0; 
	private int cycleBreakerCounter = 0;
	private MapLocation[] previousFour = new MapLocation[4];
	private Team myTeam;

	
	
	NavigationController(RobotController rc){
		this.rc = rc;
		this.myTeam = rc.getTeam();
//		this.opponentTeam = this.myTeam.opponent();
		//directioToObjective = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//this.objectiveLocation = objectiveLocation;
		//this.directionToObjective = rc.getLocation().directionTo(objectiveLocation);
	}
	
	public void progressMove(MapLocation whereToGo) throws GameActionException{
		MapLocation currentLocation = rc.getLocation();
		int dist = currentLocation.distanceSquaredTo(whereToGo);
		
		if (dist>0 && rc.isActive()){
			Direction toTarget = currentLocation.directionTo(whereToGo);
//			int[] directionOffsets = {0,1,-1,2,-2};
			int[] directionOffsets = {-2,2,-1,1,0};
			Direction potentialDirectionMovement; //direction of where we are going to move
			MapLocation newLocation;
			
			int offset;
			for (int i = 5; --i>= 0;){
				offset = directionOffsets[i];
				potentialDirectionMovement = Direction.values()[(toTarget.ordinal()+offset+8)%8];
				newLocation = rc.getLocation().add(potentialDirectionMovement);
				Team mineAtPotentialNewLocation = rc.senseMine(newLocation);
				if (rc.canMove(potentialDirectionMovement) &&  !this.isPreviousLocation(newLocation)
						&& (mineAtPotentialNewLocation == myTeam || mineAtPotentialNewLocation == null)){
					rc.move(potentialDirectionMovement);
					this.previousFour[lastIndex] = currentLocation;
					lastIndex = (lastIndex + 1)%4;
					rc.setIndicatorString(0,Arrays.toString(this.previousFour));
					rc.setIndicatorString(1,"locationBeforeMove: "+currentLocation);
					rc.setIndicatorString(2,"to Target Direction: "+toTarget.toString());
					return;
				}
			}
			for (int i = 5; --i>= 0;){
				offset = directionOffsets[i];
				potentialDirectionMovement = Direction.values()[(toTarget.ordinal()+offset+8)%8];
				if(rc.canMove(potentialDirectionMovement)){
					newLocation = currentLocation.add(toTarget);
					this.defuseFirstorThenMove(potentialDirectionMovement);
					this.previousFour[lastIndex] = currentLocation;
					lastIndex = (lastIndex + 1)%4;
					rc.setIndicatorString(0,Arrays.toString(this.previousFour));
					rc.setIndicatorString(1,"locationBeforeMove: "+currentLocation);
					rc.setIndicatorString(2,"to Target Direction: "+toTarget.toString());
					return;
				} 
			}
		}
	}

	public void progressMoveOld2(MapLocation whereToGo) throws GameActionException{
		MapLocation currentLocation = rc.getLocation();
		int dist = currentLocation.distanceSquaredTo(whereToGo);
		
		if (dist>0 && rc.isActive()){
			//init
			Direction toTarget = currentLocation.directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction potentialDirectionMovement; //direction of where we are going to move
			MapLocation newLocation;
			
			//rc.setIndicatorString(0,this.cycleBreakerCounter+"");
			//rc.setIndicatorString(1,"toTarget: "+toTarget.toString());
			int offset;
			for (int i = 5; --i>= 0;){
				offset = directionOffsets[i];
				potentialDirectionMovement = Direction.values()[(toTarget.ordinal()+offset+8)%8];
				newLocation = rc.getLocation().add(potentialDirectionMovement);
				Team mineAtPotentialNewLocation = rc.senseMine(newLocation);
				int potentialDirectionMovementNum = potentialDirectionMovement.ordinal();
				if (this.cycleBreakerCounter >0){ 
					//Dont do the normal algorithm if your Doing a Cycle break. Instead, prioritize diffusing a mine.
					if(rc.canMove(potentialDirectionMovement) 
					&& mineAtPotentialNewLocation != myTeam && mineAtPotentialNewLocation != null)
					{
						rc.defuseMine(newLocation);
						this.previousFour[lastIndex] = currentLocation;
						this.lastIndex = (lastIndex + 1)%4;
						this.cycleBreakerCounter --;
						return;
					}
					else if(rc.canMove(potentialDirectionMovement) && potentialDirectionMovementNum != (previousMove+4)%8
					&& (mineAtPotentialNewLocation == myTeam || mineAtPotentialNewLocation == null))
					{
						rc.move(potentialDirectionMovement);
						this.cycleBreakerCounter --;
						return;
					}
					//this.cycleBreakerCounter --;
					//rc.setIndicatorString(0, "Loc Array: "+Arrays.toString(this.previousFour));
				}
				else if (this.beenThereAlready(newLocation)){
					if(rc.canMove(potentialDirectionMovement) 
					&& mineAtPotentialNewLocation != myTeam && mineAtPotentialNewLocation != null)
					{
						rc.defuseMine(newLocation);
						this.previousFour[lastIndex] = currentLocation;
						this.lastIndex = (lastIndex + 1)%4;
					}
					this.cycleBreakerCounter = 1;
					//rc.setIndicatorString(0, "Loc Array: "+Arrays.toString(this.previousFour));
					return;
				}
				
				if (rc.canMove(potentialDirectionMovement) && potentialDirectionMovementNum != (previousMove+4)%8 
						&& (mineAtPotentialNewLocation == myTeam || mineAtPotentialNewLocation == null)){
					previousMove = potentialDirectionMovementNum;
					rc.move(potentialDirectionMovement);
					this.previousFour[lastIndex] = currentLocation;
					lastIndex = (lastIndex + 1)%4;
					//rc.setIndicatorString(0, "Loc Array: "+Arrays.toString(this.previousFour));
					return;
				}
			}
			
			previousMove = -1;
			for (int i = 5; --i>= 0;){
				offset = directionOffsets[i];
				potentialDirectionMovement = Direction.values()[(toTarget.ordinal()+offset+8)%8];
				if(rc.canMove(potentialDirectionMovement)){
					newLocation = currentLocation.add(toTarget);
					this.moveOrDefuse(potentialDirectionMovement);
					this.previousFour[lastIndex] = currentLocation;
					lastIndex = (lastIndex + 1)%4;
					return;
				} 
			}
		}
	}
	
	private boolean beenThereAlready(MapLocation new_location) {
		try{
			for (MapLocation prevLoc: this.previousFour){
				if (prevLoc.equals(new_location)){
					return true;
				}
			}
		}catch(Exception e){
			return false;	
			}
		return false;
	}

	private boolean isPreviousLocation(MapLocation new_location){
		try{
			if(new_location.equals(previousFour[0])){
				return true;
			}
		} catch (Exception e){
			return false;
		}
		return false;
	}
//	private void setNewObjective(MapLocation newObjectiveLocation){
//		this.objectiveLocation = newObjectiveLocation;
//	}
	
	private void defuseFirstorThenMove(Direction dir) throws GameActionException{
		MapLocation ahead = rc.getLocation().add(dir);
		if(rc.canMove(dir) && rc.senseMine(ahead)!= null && rc.senseMine(ahead) != rc.getTeam()){
			rc.defuseMine(ahead);
		}else{
			if(rc.canMove(dir)){
				rc.move(dir);     
			}
		}
	}
	
	private void moveOrDefuse(Direction dir) throws GameActionException{
		MapLocation ahead = rc.getLocation().add(dir);
		if(rc.canMove(dir) && rc.senseMine(ahead)!= null && rc.senseMine(ahead) != rc.getTeam()){
			rc.defuseMine(ahead);
		}else{
			if(rc.canMove(dir)){
				rc.move(dir);     
			}
		}
	}
	
	
	/**
	 * 
	 * @param whereToGo
	 * @throws GameActionException
	 * The original/old version of progressMove.
	 */
	@SuppressWarnings("unused")
	private void progressMoveOld(MapLocation whereToGo) throws GameActionException{
		rc.setIndicatorString(1, "Swarm target: "+whereToGo.toString());
		int dist = rc.getLocation().distanceSquaredTo(whereToGo);
		if (dist>0 && rc.isActive()){
			Direction toTarget = rc.getLocation().directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction move;
			MapLocation ahead;
			for (int offset: directionOffsets){
				move = Direction.values()[(toTarget.ordinal()+offset+8)%8];
				ahead = rc.getLocation().add(move);
				Team mine = rc.senseMine(ahead);
				int move_num = move.ordinal();
				if (rc.canMove(move) && (mine == rc.getTeam() || mine == null) && move_num != (previousMove+4)%8){
					previousMove = move_num;
					rc.move(move);
					return;
				}
			}
			previousMove = -1;
			if(rc.canMove(toTarget) || rc.senseMine(rc.getLocation())==rc.getTeam()){
				moveOrDefuse(toTarget);
			} 
		}
	}
	
	

}
