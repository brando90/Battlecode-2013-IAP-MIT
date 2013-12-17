package TestClock;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Upgrade;

public class RobotPlayer {

    public static RobotController rc;
    
    public static void run(RobotController myRC){
        rc = myRC;
        while(true){
            try{
                if(rc.getType() == RobotType.HQ){
                    rc.researchUpgrade(Upgrade.NUKE);
                }
            }catch(GameActionException e){
                
            }
        }
    }
}
