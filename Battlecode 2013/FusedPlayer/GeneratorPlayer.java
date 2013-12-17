package FusedPlayer;

import battlecode.common.*;

public class GeneratorPlayer extends BasePlayer {
	public GeneratorPlayer(RobotController rc) {
		super(rc);
		try{
			rc.broadcast(GeneratorInProgressCountChannel, rc.readBroadcast(GeneratorInProgressCountChannel) - 1); 
			rc.setIndicatorString(0, "Broadcast to generator progress channel");
		} catch (GameActionException e){
			e.printStackTrace();
		}
	}
	public void run() throws GameActionException {
	}
}

