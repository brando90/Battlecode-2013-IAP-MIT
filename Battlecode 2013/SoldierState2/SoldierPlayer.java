package SoldierState2;

import battlecode.common.*;



public class SoldierPlayer extends BasePlayer {

	private NavigationController nav;
	private MapLocation waypoint;

	private enum State {
		EncampmentCapture,Swarm,Micro,DefuseTeam;
	}

	private State state;


	public SoldierPlayer(RobotController rc) {
		super(rc);
		nav = new NavigationController(rc);
		this.currentLocation = rc.getLocation();
		try {
			this.waypoint = mapLocationFromInt(rc.readBroadcast(DefuseSpawnSquareChannel));
			if (this.waypoint == null){
				this.state = State.EncampmentCapture;
			} else{
				this.state = State.DefuseTeam;
			}
		} catch (GameActionException e) {
			this.state = State.Swarm;
			e.printStackTrace();
		}
	}
	public void run() throws GameActionException{
		this.currentLocation = rc.getLocation();

		//Decide if we should switch state
		switch(this.state){
		case EncampmentCapture:
			rc.setIndicatorString(2, "EncampmentCapture");
			if (waypoint == null){
				int targetEncampmentsStart = rc.readBroadcast(TargetEncampmentsStartChannel);
				//rc.setIndicatorString(0, " to read from channel: "+targetEncampmentsStart);
				MapLocation targetEncampment = mapLocationFromInt(rc.readBroadcast(targetEncampmentsStart));
				if (targetEncampment == null){
					this.state = State.Swarm;
				} else{
					rc.setIndicatorString(1, "Target Encampment: "+targetEncampment.toString());
					waypoint = targetEncampment;
					rc.broadcast(TargetEncampmentsStartChannel, targetEncampmentsStart+1);
					nav.progressMove(waypoint);
				}
			} else if (sameLocations(this.currentLocation,waypoint)){
				//Capture Encampment
			} else{
				nav.progressMove(waypoint);
			}
			break;
		case Swarm:
			rc.setIndicatorString(2, "Swarm");
			break;
		case Micro:
			rc.setIndicatorString(2, "Micro");
			break;
		case DefuseTeam:
			rc.setIndicatorString(2,"DefuseTeam");
			if (this.currentLocation.isAdjacentTo(waypoint)){
				if (this.mineHazard(waypoint)){
					rc.defuseMine(waypoint);
				}
				rc.broadcast(DefuseSpawnSquareChannel, 0);
				waypoint = null;
				this.state = State.EncampmentCapture;
			} else{
				nav.progressMove(waypoint);
			}
			break;
		}
	}
}
