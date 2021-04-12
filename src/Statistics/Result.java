package Statistics;

import Topology.CLOSNetwork;

/**
 * Container for the basic information about the outcome of an experiment in the
 * CLOS-Topology
 */

public class Result {
	//General Information -- Parameters
	public int k;
	public CLOSNetwork.Type type;
	public CLOSNetwork.FailType failType;
	public double failP;
	public boolean sentFromServers;
	public int numInt;
	public int totalPacks;
	public int numFailedEdges;
	public String experimentType;
	
	//Experiment Results
	public int[] load; //Load values for each node. Integer.MAX_VALUE if node was part of a forwarding loop
	public int packsInCycle; //Number of packets that ended up in a permanent forwarding loop
}

