package fleePlayer;

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
        while(true){
            try{
                if(rc.getEnergon() != 40){
                    flee();
                }
            } catch (Exception e){

            }
        }
    }

    @SuppressWarnings("unused")
    private static void fleeAdjacent() throws GameActionException{
        lookAdj: for (Direction d : Direction.values()){
            MapLocation myLoc = rc.getLocation();
            GameObject adjObj = rc.senseObjectAtLocation(myLoc.add(d));
            if(adjObj != null){
                if (adjObj instanceof Robot){
                    Robot adjRobot = (Robot) adjObj;
                    if (rc.senseRobotInfo(adjRobot).team == rc.getTeam().opponent()){
                        Direction awayFromEnemy = myLoc.directionTo(rc.senseRobotInfo(adjRobot).location).opposite();

                        if (rc.canMove(awayFromEnemy) && !goingCloserToEnemyBase(awayFromEnemy)){
                            rc.move(awayFromEnemy);
                            break lookAdj;
                        }else if (rc.canMove(myLoc.directionTo(rc.senseHQLocation()))){
                            rc.move(myLoc.directionTo(rc.senseHQLocation()));
                        }
                    }
                }
            }
        }
    }

    private static boolean goingCloserToEnemyBase(Direction dir) {
        MapLocation enemyBaseLocation = rc.senseEnemyHQLocation();
        MapLocation myLoc = rc.getLocation();
        return myLoc.add(dir).distanceSquaredTo(enemyBaseLocation) <= myLoc.distanceSquaredTo(enemyBaseLocation);

    }

    private static void flee() throws GameActionException{
        MapLocation nearestEnemyLoc = getNearestEnemyRobotLocation();
        if (nearestEnemyLoc != null){
            Direction awayFromEnemy = rc.getLocation().directionTo(nearestEnemyLoc).opposite();
            if (rc.canMove(awayFromEnemy) && !goingCloserToEnemyBase(awayFromEnemy)){
                rc.move(awayFromEnemy);
            }else{
                Direction towardBase = rc.getLocation().directionTo(rc.senseHQLocation());
                if (rc.canMove(towardBase)){
                    rc.move(towardBase);
                }

            }
        }
    }

    private static MapLocation getNearestEnemyRobotLocation() throws GameActionException{
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
        if (enemyRobots.length == 0){
            return null;
        }else{
            int nearestDist = 0;
            MapLocation nearestEnemyLoc = null;
            for(Robot enemyRobot:enemyRobots){
                MapLocation enemyLoc = rc.senseRobotInfo(enemyRobot).location;
                int distToEnemyRobot = rc.getLocation().distanceSquaredTo(enemyLoc);
                if (distToEnemyRobot<nearestDist || nearestDist == 0){
                    nearestDist = distToEnemyRobot;
                    nearestEnemyLoc = enemyLoc;
                }
            }
            return nearestEnemyLoc;
        }
    }
}