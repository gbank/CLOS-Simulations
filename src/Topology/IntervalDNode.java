package Topology;

import Routing.*;

/**
 * Node class that implements an adapted version of the interval routing protocol described
 * in https://arxiv.org/abs/2009.01497.
 *
 * The general idea behind this class is the following. BLOCK, TOP, BOT nodes in each block/pod
 * are split into numIntervals many intervals, each of roughly equal size. Forwarding decisions
 * are then made w.r.t to these intervals. E.g. a BOT node in interval i only forwards packets to
 * TOP nodes that also lie in interval i.
 *
 * This class inherits many functions from the Node superclass. The respective documentation
 * can be found in Node.java.
 */

public class IntervalDNode extends Node{

	//Interval in which the nodes resides in
	int myInterval;
	
	int numIntervals;
	int k;
	
	/**
	 * Basic constructor for the node object. Nodes are assumed to be created by the block and pod objects.
	 * 
	 * @param t	Type of the node BLOCK, TOP or BOTTOM
	 * @param pPod	Parent pod. Needs to be specified if TOP or BOTTOM node. May be set to null for BLOCK nodes
	 * @param pBlock Parent block. Needs to specified for BLOCK nodes. May be null for TOP or BOTTOM nodes
	 * @param idLocal	ID of the node 
	 * @param k	Degree of the routers (should be the same as used in the parent Block/Pod and CLOS Topology)
	 * @param numIntervals	Number of intervals to split the 
	 */
	public IntervalDNode(Type t, Pod pPod, Block pBlock, int idLocal, int k, int numIntervals) {
		super(t, pPod, pBlock, idLocal, k);
		this.k = k;
		this.numIntervals = numIntervals;
	}
	
	//Creation of dummy node
	public IntervalDNode() {
		super();
	}

	/**
	 * Function that prepares the sets of possible forwarding candidates.
	 * As defined by the Intervals protocol, these candidates only consider of 
	 * nodes in a certain interval. Note that the sets tFSet and bFSet may not
	 * include nodes that are failed.
	 */
	@Override
	public void updateRoutingState() {
		myInterval = IntervalUtility.getIntervalAssignment(k/2, numIntervals, this.idLocal);
		
		if(this.type == Type.BLOCK) {
			int[] adjInterval = IntervalUtility.getIndicesInInterval(k, numIntervals, (myInterval + 1) % numIntervals);
			int count = 0; //Count how many nodes in vertical interval are reachable
			for(int i = 0; i < adjInterval.length; i++) {
				if(!bFail[adjInterval[i]]) {count++;}
			}
			bFSet = new Node[count]; count = 0;
			for(int i = 0; i < adjInterval.length; i++) {
				if(!bFail[adjInterval[i]]) {bFSet[count] = bLink[adjInterval[i]]; count++;};
			}
		}
		else if(this.type == Type.TOP) {
			//Do same as above for edges going downward
			int[] adjInterval = IntervalUtility.getIndicesInInterval(k/2, numIntervals, (myInterval + 1) % numIntervals);
			
			int count = 0; //Count how many nodes in vertical interval are reachable
			for(int i = 0; i < adjInterval.length; i++) {
				if(!bFail[adjInterval[i]]) {count++;}
			}
			bFSet = new Node[count]; count = 0;
			for(int i = 0; i < adjInterval.length; i++) {
				if(!bFail[adjInterval[i]]) {bFSet[count] = bLink[adjInterval[i]]; count++;};
			}
			
			//Node is also in some vertical interval
			int verticalID = IntervalUtility.getIntervalAssignment(k, numIntervals, this.pPod.id);
			adjInterval = IntervalUtility.getIndicesInInterval(k/2, numIntervals, verticalID);
			
			count = 0; //Count how many nodes in horizontal interval (above) are reachable
			for(int i = 0; i < adjInterval.length; i++) {
				if(!tFail[adjInterval[i]]) {count++;}
			}
			tFSet = new Node[count]; count = 0;
			for(int i = 0; i < adjInterval.length; i++) {
				if(!tFail[adjInterval[i]]) {tFSet[count] = tLink[adjInterval[i]]; count++;};
			}
			
		}
		else { // BOT Node
			int[] adjInterval = IntervalUtility.getIndicesInInterval(k/2, numIntervals, myInterval);
			
			int count = 0; //Count how many nodes in horizontal interval (above) are reachable
			for(int i = 0; i < adjInterval.length; i++) {
				if(!tFail[adjInterval[i]]) {count++;}
			}
			tFSet = new Node[count]; count = 0;
			for(int i = 0; i < adjInterval.length; i++) {
				if(!tFail[adjInterval[i]]) {tFSet[count] = tLink[adjInterval[i]]; count++;};
			}
		}
		
		if((tFSet != null && tFSet.length == 0) || (bFSet != null && bFSet.length == 0)) {
			System.err.println("Network got disconnected! Too many edge failures or too small intervals");
			System.exit(-1);
		}
	}

	/**
	 * Sets the hash function used to determine the random choices.
	 * As this protocol is destination-based (only uses destination address in header
	 * to derive the forwarding decisions), a corresponding hash function is used.
	 */
	@Override
	public Node forward(Packet p) {
		return forward(p, Hash.destHash);
	}
	
}
