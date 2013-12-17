package StrategyPlayer2;

import battlecode.common.*;

public class SupplierPlayer extends BasePlayer {
	public SupplierPlayer(RobotController rc) {
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

