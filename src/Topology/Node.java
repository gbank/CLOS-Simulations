package Topology;
import java.util.concurrent.ThreadLocalRandom;

import Routing.*;

/**
 * Class used for the routers inside the CLOS-Topology.
 * 
 * This class is abstract to allow for implementation of different failover strategies via inheritance.
 * Each Node belongs to one of three types, depening on its position in the CLOS-Topology.
 * 
 *	1)	Block nodes, which are contained in the blocks
 *	2)	Top nodes, which lie in the top level of some pod
 *	3)	Bottom nodes, wich lie in the bottom level of some pod
 *
 * The nodes follow a hierarchical order Block -> Top -> Bottom, where edges are only drawn between
 * block and top, as well as top and bottom nodes.
 * 
 * This way, the set of neighbors is described by the sets tLink and bLink, where tLink denotes links which connect
 * to nodes further left in the hierachy (e.g. from top node to block). The links are assume to be set by the Pod and Block
 * objects.
 */

public abstract class Node {

	//Storage for neighboring sets.
	//A TOP node has tLinks towards BLOCK nodes and bLinks towards BOTTOM nodes
	//A BOTTOM node has tLinks towards TOP nodes and no bLinks
	//A BLOCK node has no tLinks but bLinks towards TOP nodes.
	Node[] tLink;
	Node[] bLink;
	
	//Flags which denote whether the links is failed. E.g. if tFail[i] is set to true then
	//tLink[i] is failed and unusable by the node
	boolean[] tFail;
	boolean[] bFail;
	
	//Counter for the number of packets received throughout the current experiment
	int load;
	
	//Sets of neighbors which can be used for forwarding packets. tFSet forwards via tLinks and
	//bFSet via bLinks. All these nodes should be intact, that is NOT failed.
	Node[] tFSet;
	Node[] bFSet;
	
	//Basic information about the node.
	public Type type;
	public Pod pPod;
	public Block pBlock;
	public int idLocal; //ID of the node inside the block/pod (ranges from 0 to k/2 -1)
	
	//Generated hashID of the node (random number)
	int hashID;
	
	//Enumeration for the possible types of a node
	public static enum Type{
		TOP,
		BOT,
		BLOCK
	}
	
	/**
	 * Basic constructor for the node object. Nodes are assumed to be created by the block and pod objects.
	 * 
	 * @param t	Type of the node BLOCK, TOP or BOTTOM
	 * @param pPod	Parent pod. Needs to be specified if TOP or BOTTOM node. May be set to null for BLOCK nodes
	 * @param pBlock Parent block. Needs to specified for BLOCK nodes. May be null for TOP or BOTTOM nodes
	 * @param idLocal	ID of the node 
	 * @param k	Degree of the routers (should be the same as used in the parent Block/Pod and CLOS Topology)
	 */
	public Node(Type t, Pod pPod, Block pBlock, int idLocal, int k) {
		this();
		
		this.idLocal = idLocal;
		this.pPod = pPod;
		this.pBlock = pBlock;
		this.type = t;
		if(t == Type.BLOCK) {
			tLink = null;
			bLink = new Node[k];
			bFail = new boolean[k];
		}
		else if(t == Type.BOT) {
			bLink = null;
			tLink = new Node[k/2];
			tFail = new boolean[k/2];
		}
		else {
			bLink = new Node[k/2];
			bFail = new boolean[k/2];
			tLink = new Node[k/2];
			tFail = new boolean[k/2];
		}
	}
	
	/**
	 * Creates a dummy node. Should not be used part of the CLOS Topology. Can be used to specify a unique source
	 * address in the packet header
	 */
	public Node() {
		this.hashID = ThreadLocalRandom.current().nextInt();
	}
	
	/**
	 * Function that prepares the sets of possible forwarding candidates.
	 * This function needs to be overwritten and define proper tSet and fSets consisting of
	 * nodes which may be reached via non-failed links only.
	 */
	public abstract void updateRoutingState();
	
	/**
	 * Hook function, which should call forward(Packet p, Hash h)  after specifying a 
	 * hash function.
	 * 
	 * @param p	Packet to be forwarded
	 * @return	Neighbor which the node forwards the packet towards
	 */
	public abstract Node forward(Packet p);
	
	/**
	 * Forwards the specified packet towards a neighbor as specified by the routing strategy.
	 * Possible forwarding partners are assumed to be stored inside tSet and bSet.
	 * 
	 * Depending on the location of the destination of p, a node in tSet/bSet is selected at random
	 * for forwarding. These sets are initialized by calling updateRoutingState() after edge failures have been placed.
	 * This selection is done via hashing the packet header to ensure that packets with the same hash-value 
	 * (e.g. same destination and source) always follow the same  path.
	 * 
	 * @param p	Packet to be forwarded
	 * @param h	Hash function applied to the packet header.
	 * @return	Neighbor of the node which receives the packet.
	 */
	public Node forward(Packet p, Hash h) {
		p.last_hop = this;
		if(this.load < Integer.MAX_VALUE) {this.load ++;}
		Node nextHop = null;
		int pHash = h.hash(p, this);
		if(p.destination == this) {
			//Do nothing packet arrived
			return null;
		}
		else if(this.type == Type.BOT) {
			nextHop = tFSet[pHash % tFSet.length];
		}
		else if(this.type == Type.TOP) {
			if(p.destinationPod == this.pPod) { //Node lies in same pod as destination
				if(this.bFail[p.destination.idLocal]) { //Check if link to destination is failed
					nextHop = bFSet[pHash % bFSet.length]; //Forward to random node in bottom layer of same pod 
				}
				else {
					nextHop = p.destination;
				}
			}
			else {
				nextHop = tFSet[pHash % tFSet.length];
			}
		}
		else { // Block node
			if(this.bFail[p.destinationPod.id]) { // Pod containing the destination is not reachable
				nextHop = bFSet[pHash % bFSet.length];
			}
			else {
				nextHop = bLink[p.destinationPod.id];
			}
		}
		
		p.hopCount++;
		return nextHop;
	}
	
	/**
	 * Repair failures of edges that involve the node.
	 * This is done by simply setting bFail and tFail to true.
	 * 
	 * Note, for this to have any effect, the updateRoutingState() function needs
	 * to be called as well.
	 */
	public void healFailures() {
		if(this.bFail != null) {
			for(int i = 0; i < bFail.length; i++) {
				bFail[i] = false;
			}
		}
		if(this.tFail != null) {
			for(int i = 0; i < tFail.length; i++) {
				tFail[i] = false;
			}
		}
	}
	
	/**
	 * Prints basic information about the Node object
	 */
	@Override
	public String toString() {
		if(this.type == Type.BLOCK) {
			return "Node[" + type + "," + pBlock.id + "," + idLocal +  "]";
		}
		else {
			return "Node[" + type + "," + pPod.id + "," + idLocal +  "]";
		}
	}
	
	@Override
	public int hashCode() {
		return hashID;
	}
}
