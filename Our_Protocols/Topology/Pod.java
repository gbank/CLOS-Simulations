package Topology;

import Hashing.Hash;
import Util.DisconnectException;

/**
 * Class for the pods of a CLOS-Topology. Such a pod consists of two sets of
 * k/2 nodes. One of these sets is called the top nodes while the other nodes are called
 * bottom nodes. 
 *
 */

public class Pod {
	//Storage for the top and bottom nodes, respectively
	Node[] top;
	Node[] bot;
	
	//Reference to parent network
	CLOSNetwork net;
	
	//ID of the pod. Ranging from 0 to k-1
	int id;
	
	/**
	 * Constructor for a pod.
	 * 
	 * @param k	Degree of the employed nodes (should be the same as used in the net)
	 * @param id	ID of the net
	 * @param net	Reference to the parent network
	 */
	public Pod( int k, int id, CLOSNetwork net) {
		this.id = id; this.net = net;
		
		top = new Node[k/2];
		for(int i = 0; i < k/2; i++) {
			if(net.type == CLOSNetwork.Type.SP_D) {
				top[i] = new ShortPathNode(Node.Type.TOP,this,null,i,k, Hash.dHash);
			}
			else if (net.type== CLOSNetwork.Type.SP_ID) {
				top[i] = new ShortPathNode(Node.Type.TOP,this,null,i,k, Hash.diHash);
			}
			else if (net.type == CLOSNetwork.Type.SP_SID) {
				top[i] = new ShortPathNode(Node.Type.TOP,this,null,i,k, Hash.disHash);
			}
			else if(net.type == CLOSNetwork.Type.INT_D) {
				top[i] = new IntervalNode(Node.Type.TOP,this,null,i,k,net.numIntervals,Hash.dHash);
			}
			else if(net.type == CLOSNetwork.Type.INT_ID) {
				top[i] = new IntervalNode(Node.Type.TOP,this,null,i,k,net.numIntervals,Hash.diHash);
			}
			else if (net.type == CLOSNetwork.Type.INT_SID) {
				top[i] = new IntervalNode(Node.Type.TOP,this,null,i,k,net.numIntervals,Hash.disHash);
			}
			else if (net.type == CLOSNetwork.Type.INT_SIDH) {
				top[i] = new IntervalNode(Node.Type.TOP,this,null,i,k,net.numIntervals,Hash.dishHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_D) {
				top[i] = new ThreePermutationNode(Node.Type.TOP,this,null,i,k, Hash.threedHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_ID) {
				top[i] = new ThreePermutationNode(Node.Type.TOP,this,null,i,k, Hash.threediHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_SID) {
				top[i] = new ThreePermutationNode(Node.Type.TOP,this,null,i,k, Hash.threedisHash);
			}
		}
		
		bot = new Node[k/2];
		for(int i = 0; i < k/2; i++) {
			if(net.type == CLOSNetwork.Type.SP_D) {
				bot[i] = new ShortPathNode(Node.Type.BOT,this,null,i,k, Hash.dHash);
			}
			else if (net.type== CLOSNetwork.Type.SP_ID) {
				bot[i] = new ShortPathNode(Node.Type.BOT,this,null,i,k, Hash.diHash);
			}
			else if (net.type == CLOSNetwork.Type.SP_SID) {
				bot[i] = new ShortPathNode(Node.Type.BOT,this,null,i,k, Hash.disHash);
			}
			else if(net.type == CLOSNetwork.Type.INT_D) {
				bot[i] = new IntervalNode(Node.Type.BOT,this,null,i,k,net.numIntervals,Hash.dHash);
			}
			else if(net.type == CLOSNetwork.Type.INT_ID) {
				bot[i] = new IntervalNode(Node.Type.BOT,this,null,i,k,net.numIntervals,Hash.diHash);
			}
			else if (net.type == CLOSNetwork.Type.INT_SID) {
				bot[i] = new IntervalNode(Node.Type.BOT,this,null,i,k,net.numIntervals,Hash.disHash);
			}
			else if (net.type == CLOSNetwork.Type.INT_SIDH) {
				bot[i] = new IntervalNode(Node.Type.BOT,this,null,i,k,net.numIntervals,Hash.dishHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_D) {
				bot[i] = new ThreePermutationNode(Node.Type.BOT,this,null,i,k, Hash.threedHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_ID) {
				bot[i] = new ThreePermutationNode(Node.Type.BOT,this,null,i,k, Hash.threediHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_SID) {
				bot[i] = new ThreePermutationNode(Node.Type.BOT,this,null,i,k, Hash.threedisHash);
			}
		}
	}
	
	/**
	 * Sets links between the nodes in the pod and blocks as specified in the
	 * definition of the CLOS-topology
	 * 
	 * The i-th node in the pod is connected to all nodes in the i-th block.
	 * 
	 * @param blocks	Array of blocks contained in the CLOS-Topology
	 */
	public void connectTo(Block[] blocks) {
		for(int i = 0; i < top.length; i++) {
			Node[] nodesInBlock = blocks[i].nodes;
			for(int j = 0; j < nodesInBlock.length; j++) {
				top[i].tLink[j] = nodesInBlock[j];
			}
		}
	}
	
	/**
	 * Draws links between the nodes inside the pod.
	 * The bottom and top nodes form a bipartite graph.
	 */
	public void connectInternalEdges() {
		//Draw links from each top node to bottom node
		for(int i = 0; i < top.length; i++) {
			for(int j = 0; j < bot.length; j++) {
				top[i].bLink[j] = bot[j];
			}
		}
		
		//Draw links from bottom nodes to top nodes
		for(int i = 0; i < bot.length; i++) {
			for(int j = 0; j < top.length; j++) {
				bot[i].tLink[j] = top[j];
			}
		}
	}
	
	/**
	 * Calls initialize() function on each node in the pod
	 */
	public void initialize() throws DisconnectException {
		for(Node n: top) {
			n.updateRoutingState();
		}
		for(Node n: bot) {
			n.updateRoutingState();
		}
	}
}
