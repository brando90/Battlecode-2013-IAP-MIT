package team260;

import battlecode.common.*;

public class FusedSupplierPlayer extends BasePlayer {
	public FusedSupplierPlayer(RobotController rc) {
		super(rc);
		try{
			rc.broadcast(SupplierInProgressCountChannel, rc.readBroadcast(SupplierInProgressCountChannel) - 1); 
		} catch (GameActionException e){
			e.printStackTrace(); 
		}
	}
	public void run() throws GameActionException {
	}
}

