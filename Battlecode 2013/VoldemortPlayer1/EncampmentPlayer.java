package VoldemortPlayer1;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class EncampmentPlayer extends BasePlayer {
	public EncampmentPlayer(RobotController rc) {
		super(rc);
	}
	public void run() throws GameActionException {
		rc.setIndicatorString(0, rc.getLocation().x + "," + rc.getLocation().y);

	}
}

