package team260;

import battlecode.common.*;

public class FusedMedbayPlayer extends BasePlayer {
	public FusedMedbayPlayer(RobotController rc) {
		super(rc);
		this.currentLocation = rc.getLocation();
	}
	public void run() throws GameActionException {
		rc.broadcast(MedbayLocationChannel, roundNumIntFromLocation(this.currentLocation));
	}
}

