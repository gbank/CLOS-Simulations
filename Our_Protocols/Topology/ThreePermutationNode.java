package Topology;

import Hashing.Hash;
import Routing.*;

/**
 * Node class that implements an adapted version of the 3-permutations routing protocol
 * in https://arxiv.org/abs/2009.01497.
 *
 * This class inherits many functions from the Node superclass. The respective documentation
 * can be found in Node.java.
 */

public class ThreePermutationNode extends IntervalNode{

	
	/**
	 * Basic constructor for the node object. Nodes are assumed to be created by the block and pod objects.
	 * 
	 * @param t	Type of the node BLOCK, TOP or BOTTOM
	 * @param pPod	Parent pod. Needs to be specified if TOP or BOTTOM node. May be set to null for BLOCK nodes
	 * @param pBlock Parent block. Needs to specified for BLOCK nodes. May be null for TOP or BOTTOM nodes
	 * @param idLocal	ID of the node 
	 * @param k	Degree of the routers (should be the same as used in the parent Block/Pod and CLOS Topology)
	 */
	public ThreePermutationNode(Type t, Pod pPod, Block pBlock, int idLocal, int k, Hash hashFunction) {
		super(t, pPod, pBlock, idLocal, k,1, hashFunction);
		this.k = k;
	}
	
	//Creation of dummy node
	public ThreePermutationNode() {
		super();
	}

	/**
	 * Sets the hash function used to determine the random choices.
	 * As this protocol is destination-based (only uses destination address in header
	 * to derive the forwarding decisions), a corresponding hash function is used.
	 */
	@Override
	public Node forward(Packet p) {
		return forward(p, hashFunction);
	}
	
}
