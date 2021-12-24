package Statistics;

import java.util.HashMap;

import Topology.CLOSNetwork;
import Topology.Node;
import Util.Edge;

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
	
	public HashMap<Node,Double> nodeLoad; //Map containing nodes and the load they received
	public HashMap<Edge, Double> edgeLoad; //Map containing the edges and the load they received
	public int packsInCycle; //Number of packets that ended up in a permanent forwarding loop
	public double avgHops;	//Avg. number of hops to reach destination (packets on cycle excluded)
	public int maxHops; //Maximum number of hops (by packets not in cycle) to reach destination
}

