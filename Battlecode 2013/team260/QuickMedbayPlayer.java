package team260;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class QuickMedbayPlayer extends BasePlayer {
	public QuickMedbayPlayer(RobotController rc) {
		super(rc);
		this.currentLocation = rc.getLocation();
	}
	public void run() throws GameActionException {
		rc.broadcast(MedbayLocationChannel, roundNumIntFromLocation(this.currentLocation));
	}
}

