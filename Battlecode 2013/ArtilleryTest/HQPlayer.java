package ArtilleryTest;

import battlecode.common.*;

public class HQPlayer extends BasePlayer {
	private static int spawnPower = 50;
	private static MapLocation[] target_encampments;
	//private static int encampmentCounter;
	private static boolean farAwayHQ;
	//private static int counter = 0;
	//private static MapLocation[] captured_encampments;
	private int dataStart = 30005;



	//private static int pointer = 0;

	public HQPlayer(RobotController rc) {
		super(rc);


		if(rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation()) < 600){
			farAwayHQ = false;
		} else{
			farAwayHQ = true;
		}

		if(farAwayHQ){
			target_encampments = rc.senseAllEncampmentSquares();
			QuicksortLocations(target_encampments, rc.getLocation());
		}

		else{
			try {
				target_encampments = rc.senseEncampmentSquares(rc.getLocation(), 200, Team.NEUTRAL);
				QuicksortLocations(target_encampments, rc.getLocation());
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}




		try {
			rc.broadcast(base_channel, dataStart);
			for (int i = 0; i < target_encampments.length;i++){
				if(target_encampments[i] != null){
					rc.broadcast(dataStart+i, mapLocationToInt(target_encampments[i]));
					//rc.setIndicatorString(1, "Attempting to broadcast to base channel: "+Integer.toString(dataStart)+" with: "+i+" data channels.");
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}



	public void run() throws GameActionException {

		rc.setIndicatorString(2, "I AM Artillarytest");
		
		if(farAwayHQ && rc.senseAllEncampmentSquares().length > 20 && (!rc.hasUpgrade(Upgrade.FUSION) || !rc.hasUpgrade(Upgrade.DEFUSION))){
			if(!rc.hasUpgrade(Upgrade.FUSION))
				rc.researchUpgrade(Upgrade.FUSION);
			else if(!rc.hasUpgrade(Upgrade.DEFUSION))
				rc.researchUpgrade(Upgrade.DEFUSION);	
		}




		//code to execute for the whole match
		if (rc.isActive()) {
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			int[] spawningOffsets = {0,1,-1,2,-2,3,-3,4};
			for (int offset : spawningOffsets){
				Direction spawnDir = Direction.values()[(dir.ordinal()+offset+8)%8];
				if (rc.canMove(spawnDir) && rc.getTeamPower() > spawnPower){
					rc.spawn(spawnDir);
					return;
				}
			}
		}

	}



	private static int mapLocationToInt(MapLocation location){
		return location.x*1000 + location.y;
	}





}