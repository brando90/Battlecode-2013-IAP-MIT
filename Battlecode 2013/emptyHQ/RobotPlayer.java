package emptyHQ;

import battlecode.common.*;

/** The example funcs player is a player meant to demonstrate basic usage of the most common commands.
 * Robots will move around randomly, occasionally mining and writing useless messages.
 * The HQ will spawn soldiers continuously.
 */
public class RobotPlayer {

    static RobotController rc;

    public static void run(RobotController robotController) {
        rc = robotController;
        while (true) {
            try {
                if (rc.getType() == RobotType.HQ) {
                    HQCode();
                } else if (rc.getType() == RobotType.SOLDIER) {
                    SoldierCode();                          
                }

                // End turn
                rc.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void HQCode() throws GameActionException{
        while(true){
            //generate power...don't spawn
        }
    }
    private static void SoldierCode() throws GameActionException{
//        while(true){
//            //don't do anything
//            MapLocation current = rc.getLocation();
//            MapLocation rendezvous = new MapLocation (14,14);
//            if (current.x != rendezvous.x || current.y != rendezvous.y){
//                Direction dir = current.directionTo(rendezvous);
//                if (rc.isActive() && rc.canMove(dir)){
//                    rc.move(dir);
//                }
//            }
//        }
    }
}