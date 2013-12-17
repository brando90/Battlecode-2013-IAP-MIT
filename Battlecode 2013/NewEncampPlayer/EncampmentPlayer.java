package NewEncampPlayer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class EncampmentPlayer extends BasePlayer {
	public EncampmentPlayer(RobotController rc) {
		super(rc);
	}
	public void run()  {
		//rc.setIndicatorString(0, rc.getLocation().x + "," + rc.getLocation().y);
		//rc.broadcast(arg0, arg1)
		
		int broadcast_channel = findChannel(rc.getLocation());
	    try {
			rc.broadcast(broadcast_channel, updateTime());
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		rc.setIndicatorString(0, "round=" + Clock.getRoundNum() + "   println=" + updateTime());

	}
	
	
	public int updateTime(){
		int time = Clock.getRoundNum() + 1 - 1000;

		if(time < 0){
			return 1000000*time - 1000*rc.getLocation().x - rc.getLocation().y;
		} else{
			return 1000000*time + 1000*rc.getLocation().x + rc.getLocation().y;
		}
	}
	
    public MapLocation intToMapLocation(int input){
        
		if(input < 0){
        	input = input * -1;
        }
        
		int x = input/1000 % 1000;
		int y = input%1000;
		return new MapLocation(x, y);
	}
	
	
	public int findChannel(MapLocation loc){
		 int answer = 0;
		 try {
			while(intToMapLocation(rc.readBroadcast(answer)).equals(loc) != true){
				answer = answer + 1;
			}
		 } catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	     return answer;
		 
			 
			 
	}
	
}

