package ArtilleryTest;

//import java.util.Random;
//import java.util.*;
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
	private int cylceBreakerCounter = 0;
	private MapLocation[] previousFour = new MapLocation[4];
	private Team myTeam;
	//private Team opponentTeam;

	
	
	NavigationController(RobotController rc){
		this.rc = rc;
		this.myTeam = rc.getTeam();
		//this.opponentTeam = this.myTeam.opponent();
		//directioToObjective = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		//this.objectiveLocation = objectiveLocation;
		//this.directionToObjective = rc.getLocation().directionTo(objectiveLocation);
	}

	public void progressMove(MapLocation whereToGo) throws GameActionException{
		MapLocation currentLocation = rc.getLocation();
		int dist = currentLocation.distanceSquaredTo(whereToGo);
		
		if (dist>0 && rc.isActive()){
			//init
			Direction toTarget = currentLocation.directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction potentialDirectionMovement; //direction of where we are going to move
			MapLocation newLocation;
			
			//rc.setIndicatorString(0,this.cylceBreakerCounter+"");
			//rc.setIndicatorString(1,toTarget.toString());
			for (int offset: directionOffsets){
				potentialDirectionMovement = Direction.values()[(toTarget.ordinal()+offset+8)%8];
				newLocation = rc.getLocation().add(potentialDirectionMovement);
				Team mineAtPotentialNewLocation = rc.senseMine(newLocation);
				int potentialDirectionMovementNum = potentialDirectionMovement.ordinal();
				//System.out.println("Offset: "+offset);
				//System.out.println("Potential Direction Mov: "+potentialDirectionMovement.toString());
				if (this.cylceBreakerCounter >0){ 
					//Dont do the normal algorithm if your Doing a Cycle break. Instead, prioritize diffusing a mine.
					if(rc.canMove(potentialDirectionMovement) 
					&& mineAtPotentialNewLocation != myTeam && mineAtPotentialNewLocation != null)
					{
						rc.defuseMine(newLocation);
						this.previousFour[lastIndex] = currentLocation;
						this.lastIndex = (lastIndex + 1)%4;
						this.cylceBreakerCounter --;
						return;
					}
					else if(rc.canMove(potentialDirectionMovement) && potentialDirectionMovementNum != (previousMove+4)%8
					&& (mineAtPotentialNewLocation == myTeam || mineAtPotentialNewLocation == null))
					{
						rc.move(potentialDirectionMovement);
						this.cylceBreakerCounter --;
						return;
					}
					//this.cylceBreakerCounter --;
					//rc.setIndicatorString(0, "Loc Array: "+Arrays.toString(this.previousFour));
				}
				else if (this.beenThereAlready(newLocation)){
					//System.out.println("I was executed inside the beenThereAlready");
					if(rc.canMove(potentialDirectionMovement) 
					&& mineAtPotentialNewLocation != myTeam && mineAtPotentialNewLocation != null)
					{
						rc.defuseMine(newLocation);
						this.previousFour[lastIndex] = currentLocation;
						this.lastIndex = (lastIndex + 1)%4;
					}
					this.cylceBreakerCounter = 1;
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
			for (int offset: directionOffsets){
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

//	private void setNewObjective(MapLocation newObjectiveLocation){
//		this.objectiveLocation = newObjectiveLocation;
//	}
	
	//@SuppressWarnings("unused")
	private void moveOrDefuse(Direction dir) throws GameActionException{
		MapLocation ahead = rc.getLocation().add(dir);
		if(rc.senseMine(ahead)!= null && rc.senseMine(ahead) != rc.getTeam()){
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
