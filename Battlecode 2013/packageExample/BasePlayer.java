package packageExample;

import battlecode.common.*;

public abstract class BasePlayer {
	RobotController rc;
	
	public BasePlayer(RobotController rc) {
		this.rc = rc;
	}
	
	public abstract void run() throws GameActionException;
	
	public void loop() {
		while(true) {
			try {
				// Execute turn
				run();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// End turn
			rc.yield();
		}
	}
}
