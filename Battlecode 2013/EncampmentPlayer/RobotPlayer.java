package EncampmentPlayer;

import battlecode.common.*;

public class RobotPlayer {
        private static RobotController rc;
        private static int previous_move = -1; 
        private static MapLocation rallyPt;
        private static int spawnPower = 10;
        private static int nextUpgradeIndex = 0;
        private static Upgrade[] orderedUpgrades = {Upgrade.PICKAXE,Upgrade.FUSION,Upgrade.NUKE};
        private static MapLocation[] target_encampments;
        private static int base_channel;
        private static double encampment_radius_coeff = 5;
        private static boolean farAwayHQ;
        private static int concurrentEncampmentCaptureMax = 5;
        private static int encampmentCounter = 0;

        public static void run(RobotController myRC){
                //initialization
                rc = myRC;
                if (rc.getTeam() == Team.A){
                        base_channel = 034;
                } else{
                        base_channel = 789;
                }
                
                //if(rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation()) < 600){
                	farAwayHQ = false;
                //} else{
                //	farAwayHQ = true;
                //}
                
                if (rc.getType() == RobotType.HQ){
                        try{
                            System.out.println("HQ DISTANCE: " + farAwayHQ);
                        	if(farAwayHQ){
                        		target_encampments = rc.senseAllEncampmentSquares();
                        	    Quicksort(target_encampments, 0, target_encampments.length-1);
                        	}
                        
                        	else{
                        		target_encampments = findTargetEncampemnts();
                        		Quicksort(target_encampments, 0, target_encampments.length-1);
                        	}
                            
                        	for (int i = 0; i < target_encampments.length;i++){
                                        rc.broadcast(base_channel+i, mapLocationToInt(target_encampments[i]));
                                        rc.setIndicatorString(1, "Attempting to broadcast to channel: "+Integer.toString(i));
                                }
                         
                        	}catch (Exception e){
                                e.printStackTrace();
                        }
               
                } else if (rc.getType() == RobotType.SOLDIER){
                        try{
                                MapLocation encampment = getTargetEncampmentAtChannel(base_channel);                    
                                if (encampment != null ){
                                        rallyPt = encampment;
                                        rc.setIndicatorString(1, "Heading to Enc: "+encampment.toString());
                                }else {
                                        rallyPt = rc.senseEnemyHQLocation();
                                }
                        
                        } catch (Exception e){
                                e.printStackTrace();
                        }
                }

                //steady-state
                while(true){
                        try {
                                if (rc.getType() == RobotType.HQ){
                                        HQCode();
                                } else if (rc.getType() == RobotType.SOLDIER){
                                        SoldierCode();
                                }
                        } catch (Exception e){
                                e.printStackTrace();
                        }
                        rc.yield();
                }
        }


        private static void HQCode() throws GameActionException{

        	
        	/**
        	 * If enemyHQ is far away and there are many encampments on the board, we want to research Fusion 
        	 * and Defusion first. 
        	 */
        	if(farAwayHQ && rc.senseAllEncampmentSquares().length > 20 && (!rc.hasUpgrade(Upgrade.FUSION) || !rc.hasUpgrade(Upgrade.DEFUSION))){
        		if(!rc.hasUpgrade(Upgrade.FUSION))
        			rc.researchUpgrade(Upgrade.FUSION);
        	    if(!rc.hasUpgrade(Upgrade.DEFUSION));
        			rc.researchUpgrade(Upgrade.DEFUSION);	
        	}
        	
        
        	if (rc.isActive()) {
                        // Spawn a soldier
                        Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                        if (rc.canMove(dir) && rc.getTeamPower() > spawnPower){
                                rc.spawn(dir);
                        } else{
                                if (rc.hasUpgrade(orderedUpgrades[nextUpgradeIndex]) && nextUpgradeIndex < orderedUpgrades.length){
                                        rc.researchUpgrade(orderedUpgrades[nextUpgradeIndex++]);
                                }
                        }
                }
        	
        }

        private static MapLocation getTargetEncampmentAtChannel(int i) throws GameActionException{
                rc.setIndicatorString(2, "Attempting to read channel: "+Integer.toString(i));
                int broadcast = rc.readBroadcast(i);
                switch(broadcast){
                case 0:
                        return null;
                case -1:
                        return getTargetEncampmentAtChannel(i+1);
                default:
                        rc.broadcast(i, -1);
                        return intToMapLocation(broadcast);
                }


        }

        
    
        /** =============== Sorting MapLocations based on closeness to our HQ ================= */
        
        private static void Quicksort(MapLocation[] encampList, int from, int to){
        	if(from >= to)
        		return;
        	
        	//choose pivot
        	int p = (from + to) / 2;
        	
        	//Partition
        	int i=from;
        	int j=to;
        	
        	while(i <= j){
        		if(encampList[i].distanceSquaredTo(rc.senseHQLocation()) <= encampList[p].distanceSquaredTo(rc.senseHQLocation()))
        			i++;
        		else if(encampList[j].distanceSquaredTo(rc.senseHQLocation()) >= encampList[p].distanceSquaredTo(rc.senseHQLocation()))
        			j--;
        		else{
        			swap(encampList, i, j);
        			i++;
        			j--;
        		}
        	}
        	
        	//Swap pivot after partition
        	if(p < j){
    			swap(encampList, j, p);
    			p=j;
    		} 
    		else if(p > i){
    			swap(encampList, i, p);
    			p=i;
    		}
    		
        	//Recursive sort the rest of the list
    		Quicksort(encampList, from, p-1);
    		Quicksort(encampList, p+1, to);
        	
        }
        
        private static void swap(MapLocation[] a, int i, int j){
        	MapLocation temp = a[i];
        	a[i] = a[j];
        	a[j] = temp;
        }
        
        
        /** ================================================================================= **/
        
    	
        
        
       /** ================================== Soldier Code ================================== **/
        private static void SoldierCode() throws GameActionException{

               if (rc.isActive()){
                        //Use awesome Nav
                        if (rc.senseEncampmentSquare(rc.getLocation())){
                        	
                        		rc.captureEncampment(RobotType.GENERATOR);
                        	    encampmentCounter +=1;
                        	  
                        		
                        	
                        
                   
                        } else{
                                if (rc.getLocation().distanceSquaredTo(rallyPt) < 5){
                                        moveOrDefuse(rc.getLocation().directionTo(rallyPt));
                                }else{
                                        progressMove(rallyPt);
                                }
                        }
                } 
               
               rc.setIndicatorString(1, "encampments numbers: " + encampmentCounter);
   	
        	}
        /** ================================================================================= **/
        
        
        
        
        /** ============================== Finding target Encampments ======================== **/
        private static MapLocation[] findTargetEncampemnts(){
        	MapLocation[] allEncampments = rc.senseAllEncampmentSquares();
        	MapLocation[] encampments = new MapLocation[allEncampments.length];
        	int counter = 0;
        	
        	for(MapLocation loc : allEncampments){
        		MapLocation ourhq = rc.senseHQLocation();
        		MapLocation enemyhq = rc.senseEnemyHQLocation();
        		
        		if(loc.distanceSquaredTo(ourhq) < 150){
        			
        			encampments[counter] = loc;
        			counter++;
        		} 
        		
        		//else if ( (loc.x < ourhq.x && loc.x > enemyhq.x) || (loc.x > ourhq.x && loc.x < enemyhq.x)){
        			//if((loc.y - ourhq.y < 10) || (loc.y - ourhq.y > -10)){
        				//encampments[counter] = loc;
        				//counter++;
        			//}
        		//}
        		
        		
        	}
        	
        	return encampments;
        	
        }

/** ========================================================================================================== **/
        
        
        private static void progressMove(MapLocation whereToGo) throws GameActionException{
                int dist = rc.getLocation().distanceSquaredTo(whereToGo);
                if (dist>0 && rc.isActive()){
                        Direction to_target = rc.getLocation().directionTo(whereToGo);
                        int[] directionOffsets = {0,1,-1,2,-2};
                        Direction move;
                        MapLocation ahead;
                        for (int offset: directionOffsets){
                                move = Direction.values()[(to_target.ordinal()+offset+8)%8];
                                ahead = rc.getLocation().add(move);
                                Team mine = rc.senseMine(ahead);
                                int move_num = move.ordinal();
                                if (rc.canMove(move) && (mine == rc.getTeam() || mine == null) && move_num != (previous_move+4)%8){
                                        //rc.setIndicatorString(1, "move: "+move.toString());
                                        //rc.setIndicatorString(2, "to_target: "+to_target.toString());
                                        //rc.setIndicatorString(2, "previous_move"+Integer.toString(previous_move));
                                        //System.out.println(Integer.toString(previous_move));
                                        //System.out.println(attempt_to_move.toString());
                                        previous_move = move_num;
                                        rc.move(move);
                                        return;
                                }
                        }
                        //for (int i =0; i<3; i++)
                        previous_move = -1;
                        if(rc.canMove(to_target) || rc.senseMine(rc.getLocation())==rc.getTeam()){
                                moveOrDefuse(to_target);
                        } else{
                                MapLocation infront= rc.getLocation().add(to_target);
                                if (rc.senseEncampmentSquare(infront)){
                                        rc.setIndicatorString(2, "Trying to move through enc");
                                        rc.move(to_target);
                                }
                        }
                }
        }

       
        private static void moveOrDefuse(Direction dir) throws GameActionException{
                MapLocation ahead = rc.getLocation().add(dir);
                if(rc.senseMine(ahead)!= null && rc.senseMine(ahead) != rc.getTeam()){
                        rc.defuseMine(ahead);
                }else{
                        if(rc.canMove(dir)){
                                rc.move(dir);     
                        }
                }
        }
        
        private static MapLocation getNearestEncampmentMapLocation(){

            return null;
       }


        private static void printMapLocations(MapLocation[] locations){
                for (MapLocation location : locations){
                        System.out.print("Loc: "+location.toString()+", ");
                }
        }

        private static int mapLocationToInt(MapLocation location){
                return location.x*1000 + location.y;
        }

        private static MapLocation intToMapLocation(int integer){
                if (integer == -1){
                        return null;
                } else{
                        int x = integer/1000;
                        int y = integer%1000;
                        return new MapLocation(x,y);
                }
        }
}


