package NukesTehDevs;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {

    public static RobotController rc;
    
    public static void run(RobotController myRC){
        rc = myRC;
        while(true){
            if(rc.getType() == RobotType.GENERATOR){
                rc.setIndicatorString(0, "Current Round: "+Clock.getRoundNum());
            }
        }
    }
}
