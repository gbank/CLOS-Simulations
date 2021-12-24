package Topology;

import Hashing.Hash;
import Util.DisconnectException;

/**
 * Class which describes a Block, containing (k/2) many nodes of
 * the CLOS-Topology
 */
public class Block {
	//ID of the block, ranging from 0 to k/2 -1
	int id;
	
	//Network containing the block
	CLOSNetwork net;
	
	//Nodes contained in the block
	Node[] nodes;
	
	/**
	 * Constructs a Block of k/2 nodes
	 * 
	 * @param k		Degree of the switches
	 * @param id	id of the block
	 * @param net	Parent network containing the block
	 */
	public Block(int k, int id, CLOSNetwork net) {
		this.id = id; this.net = net;
		nodes = new Node[k/2];
		for(int i = 0; i < k/2; i++) {
			if(net.type == CLOSNetwork.Type.SP_D) {
				nodes[i] = new ShortPathNode(Node.Type.BLOCK,null,this,i,k, Hash.dHash);
			}
			else if (net.type== CLOSNetwork.Type.SP_ID) {
				nodes[i] = new ShortPathNode(Node.Type.BLOCK,null,this,i,k, Hash.diHash);
			}
			else if (net.type == CLOSNetwork.Type.SP_SID) {
				nodes[i] = new ShortPathNode(Node.Type.BLOCK,null,this,i,k, Hash.disHash);
			}
			else if(net.type == CLOSNetwork.Type.INT_D) {
				nodes[i] = new IntervalNode(Node.Type.BLOCK,null,this,i,k,net.numIntervals,Hash.dHash);
			}
			else if(net.type == CLOSNetwork.Type.INT_ID) {
				nodes[i] = new IntervalNode(Node.Type.BLOCK,null,this,i,k,net.numIntervals,Hash.diHash);
			}
			else if (net.type == CLOSNetwork.Type.INT_SID) {
				nodes[i] = new IntervalNode(Node.Type.BLOCK,null,this,i,k,net.numIntervals,Hash.disHash);
			}
			else if (net.type == CLOSNetwork.Type.INT_SIDH) {
				nodes[i] = new IntervalNode(Node.Type.BLOCK,null,this,i,k,net.numIntervals,Hash.dishHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_D) {
				nodes[i] = new ThreePermutationNode(Node.Type.BLOCK,null,this,i,k, Hash.threedHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_ID) {
				nodes[i] = new ThreePermutationNode(Node.Type.BLOCK,null,this,i,k, Hash.threediHash);
			}
			else if(net.type == CLOSNetwork.Type.TP_SID) {
				nodes[i] = new ThreePermutationNode(Node.Type.BLOCK,null,this,i,k, Hash.threedisHash);
			}
		}
	}
	
	/**
	 * Connects the block to pods as specified in the definition of the CLOS topology.
	 * The i-th block (i.e. block with ID = i) is connected to the i-th node in every pod.
	 * 
	 * @param pods	Array of all k Pods that lie in the CLOS-Topology
	 */
	public void connectTo(Pod[] pods) {
		for(int n = 0; n < nodes.length; n++) {
			int count = 0;
			for(int p = 0; p < pods.length; p++) {
				nodes[n].bLink[count] = pods[p].top[this.id];
				count++;
			}
		}
	}
	
	public void initialize() throws DisconnectException{
		for(Node n: nodes) {
			n.updateRoutingState();
		}
	}
}
