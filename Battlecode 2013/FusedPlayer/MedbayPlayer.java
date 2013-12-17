package FusedPlayer;

import battlecode.common.*;

public class MedbayPlayer extends BasePlayer {
	public MedbayPlayer(RobotController rc) {
		super(rc);
		this.currentLocation = rc.getLocation();
	}
	public void run() throws GameActionException {
		rc.broadcast(MedbayLocationChannel, roundNumIntFromLocation(this.currentLocation));
	}
}

