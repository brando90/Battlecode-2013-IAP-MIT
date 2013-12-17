package AwesomeArtillery1;

import battlecode.common.*;

public abstract class BasePlayer {
	RobotController rc;
	int width;
	
	protected MapLocation myCurrentLocation;
	
	protected int base_channel = 200;
	protected int NextEncampmentChannel = 798;
	//protected int GeneratorChannel = 934;
	//protected int SupplierChannel = 1043;
	
	
	public BasePlayer(RobotController rc) {
		this.rc = rc;
		width = rc.getMapWidth();
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
	
	protected void QuicksortLocations(MapLocation[] locations, MapLocation reference){
		QuicksortRecurseLocations(locations, reference, 0, locations.length - 1);
	}
	
    private void QuicksortRecurseLocations(MapLocation[] locations, MapLocation reference,int from, int to){
  
    	
    	if(from >= to)
    		return;
    	
    	//choose pivot
    	int p = (from + to) / 2;
    	
    	//Partition
    	int i=from;
    	int j=to;
    	
    	while(i <= j){
    		if(locations[i].distanceSquaredTo(reference) <= locations[p].distanceSquaredTo(reference))
    			i++;
    		else if(locations[j].distanceSquaredTo(reference) >= locations[p].distanceSquaredTo(reference))
    			j--;
    		else{
    			swapLocations(locations, i, j);
    			i++;
    			j--;
    		}
    	}
    	
    	//Swap pivot after partition
    	if(p < j){
			swapLocations(locations, j, p);
			p=j;
		} 
		else if(p > i){
			swapLocations(locations, i, p);
			p=i;
		}
		
    	//Recursive sort the rest of the list
		QuicksortRecurseLocations(locations, reference, from, p-1);
		QuicksortRecurseLocations(locations, reference, p+1, to);
    	
    }
    
    private static void swapLocations(MapLocation[] a, int i, int j){
    	MapLocation temp = a[i];
    	a[i] = a[j];
    	a[j] = temp;
    }
}
