package Topology;

import Routing.Hash;
import Routing.Packet;


/**
 * Node class implementing the Shortest-Path routing protocol.
 * The only difference to ShortestPathSDNode.java is the employed Hash-Function.
 * 
 * The pseudo-random selection for forwarding partners in this class only accounts
 * for the destination field in the packet header. Therefore, packets with the same
 * destination address will follow the exact same forwarding decisions.	
 */
public class ShortPathDNode extends ShortPathSDNode{
	
	public ShortPathDNode(Type t, Pod pPod, Block pBlock, int idLocal, int k) {
		super(t, pPod, pBlock, idLocal, k);
	}
	
	//Creation of dummy node
	public ShortPathDNode() {
		super();
	}
	
	
	@Override
	public Node forward(Packet p) {
		return forward(p, Hash.destHash);
	}
	
}
