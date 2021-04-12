package Topology;

import Routing.*;

/**
 * Node class implementing the Interval routing protocol
 * The only difference to IntervalDNode.java is the employed Hash-Function.
 * 
 * The pseudo-random selection for forwarding partners in this class accounts for,
 * both, the source and destination address. Therefore, it is unlikely for packets with
 * distinct source or destination address to be forwarded to the same node.
 */
public class IntervalSDNode extends IntervalDNode{

	public IntervalSDNode(Node.Type t, Pod pPod, Block pBlock, int idLocal, int k, int numIntervals) {
		super(t, pPod, pBlock, idLocal, k, numIntervals);
	}
	
	//Creation of dummy node
	public IntervalSDNode() {
		super();
	}
	
	
	@Override
	public Node forward(Packet p) {
		return forward(p, Hash.destSourceHash);
	}
	
}
