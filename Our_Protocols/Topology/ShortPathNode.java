package Topology;

import Hashing.Hash;
import Routing.*;

/***
 * Node class implementing Shortest-Path routing. 
 * That is, this node attempts to forward packets over a random link that lies on a shortest path.
 * More precisely, as local failover protocols are considered, the node forwards over shortest paths
 * in the CLOS-Topology where only links directly incident to the node are failed.
 * However, as further failures may exists downstream, this packet may not actually follow a shortest 
 * path.
 */

public class ShortPathNode extends Node{

	/**
	 * Basic constructor for the node object. Nodes are assumed to be created by the block and pod objects.
	 * 
	 * @param t	Type of the node BLOCK, TOP or BOTTOM
	 * @param pPod	Parent pod. Needs to be specified if TOP or BOTTOM node. May be set to null for BLOCK nodes
	 * @param pBlock Parent block. Needs to specified for BLOCK nodes. May be null for TOP or BOTTOM nodes
	 * @param idLocal	ID of the node 
	 * @param k	Degree of the routers (should be the same as used in the parent Block/Pod and CLOS Topology)
	 */
	public ShortPathNode(Type t, Pod pPod, Block pBlock, int idLocal, int k, Hash hashFunction) {
		super(t, pPod, pBlock, idLocal, k, hashFunction);
	}
	
	//Creation of dummy node
	public ShortPathNode() {
		super();
	}

	/**
	 * Initializes the set of forwarding candidates.
	 * This functions assumes that the edge failures have already been placed.
	 * 
	 * It sets up the tFSet and bFSet fields such that the forward() function in
	 * Node.class implements (local) Shortest-Path routing.
	 * 
	 */
	@Override
	public void updateRoutingState() {
		if(tLink != null) {
			int count = 0;
			for(int i = 0; i < tLink.length; i++) {
				if(!tFail[i]) {count++;}
			}
			tFSet = new Node[count]; count = 0;
			for(int i = 0; i < tLink.length; i++) {
				if(!tFail[i]) {tFSet[count] = tLink[i]; count++;}
			}
			
			if(tFSet.length == 0) {
				System.err.println("Too many edge failures -- Network got disconnected!");
				System.exit(-1);
			}
		}
		if(bLink != null) {
			int count = 0;
			for(int i = 0; i < bLink.length; i++) {
				if(!bFail[i]) {count++;}
			}
			bFSet = new Node[count]; count = 0;
			for(int i = 0; i < bLink.length; i++) {
				if(!bFail[i]) {bFSet[count] = bLink[i]; count++;}
			}
			
			if(bFSet.length == 0) {
				System.err.println("Too many edge failures -- Network got disconnected!");
				System.exit(-1);
			}
		}
	}

	/**
	 * Selects the hash function to be used.
	 * The forwarding strategy is source-destination based. Therefore,
	 * the pseudo-random routing decisions need to depend on both, source and destination address.
	 */
	@Override
	public Node forward(Packet p) {
		return forward(p, hashFunction);
	}
	
}
