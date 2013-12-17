package NewEncampPlayer;

import battlecode.common.*;

public class HQPlayer extends BasePlayer {
	private static int spawnPower = 10;
	private static MapLocation[] target_encampments;



	public HQPlayer(RobotController rc) {
		super(rc);

		//TODO: Get list of encampments to capture -- replace with other method
		target_encampments = rc.senseAllEncampmentSquares();

		//Broadcast the closest one to basechannel
		buildheap(target_encampments);
		MapLocation loc = popMin();
		
		if(loc != null){
			try {
				rc.broadcast(base_channel + totalEncamps, mapLocationToInt(loc));
				totalEncamps += 1;
			} catch (GameActionException e2) {
				e2.printStackTrace();
			}
		}
		
	}



	public void run() throws GameActionException {
		
	
		

		//code to execute for the whole match
		if (rc.isActive()) {
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.canMove(dir) && rc.getTeamPower() > spawnPower){
				
				MapLocation loc = popMin();
				if(loc != null){
					rc.broadcast(base_channel + totalEncamps, mapLocationToInt(loc));
					rc.setIndicatorString(0, loc.x + "," + loc.y);
					
					totalEncamps += 1;
				}
				
				rc.spawn(dir);
			}
			
			
		}
		
	

	}



	/** ================================ HEAP CODE ====================================== **/
	public void buildheap(MapLocation[] encampList){
		int size = encampList.length;

		for(int i=size/2; i>=0; i--){
			MinHeapify(encampList, i);
		}

	}

	public void MinHeapify(MapLocation[] encamps, int i){
		int left = 2*i;
		int right = 2*i+1;

		int smallest;


		if(left < encamps.length && rc.senseHQLocation().distanceSquaredTo(encamps[left]) < rc.senseHQLocation().distanceSquaredTo(encamps[i])){
			smallest = left;
		} else{
			smallest = i;
		}

		if(right < encamps.length && rc.senseHQLocation().distanceSquaredTo(encamps[right]) < rc.senseHQLocation().distanceSquaredTo(encamps[smallest])){
			smallest = right;
		}

		if(smallest != i){
			MapLocation temp = encamps[i];
			encamps[i] = encamps[smallest];
			encamps[smallest] = temp;
			MinHeapify(encamps, smallest);
		}
	}

	public MapLocation popMin(){
		if(target_encampments.length == 0){
			return null;
		}else{
			MapLocation min = target_encampments[0];
			MapLocation[] updated_list = new MapLocation[target_encampments.length-1];
			for(int i=1; i<target_encampments.length; i++){
				updated_list[i-1] = target_encampments[i];
			}

			buildheap(updated_list);
			target_encampments = updated_list;
			return min;
		}
	}

	/** ================================================================================= **/

	private int mapLocationToInt(MapLocation location){
		//1000 ,000, 000
		int time = Clock.getRoundNum();

		if(time < 0){
			return 1000000*time - 1000*location.x - location.y;
		} else{
			return 1000000*time + 1000*location.x + location.y;
		}
	}




}