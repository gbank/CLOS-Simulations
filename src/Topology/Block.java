package Topology;

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
			if(net.type == CLOSNetwork.Type.SP_SD) {
				nodes[i] = new ShortPathSDNode(Node.Type.BLOCK,null,this,i,k);
			}
			else if(net.type == CLOSNetwork.Type.INT_D) {
				nodes[i] = new IntervalDNode(Node.Type.BLOCK,null,this,i,k,net.numIntervals);
			}
			else if(net.type == CLOSNetwork.Type.INT_SD) {
				nodes[i] = new IntervalSDNode(Node.Type.BLOCK,null,this,i,k,net.numIntervals);
			}
			else if(net.type == CLOSNetwork.Type.SP_D) {
				nodes[i] = new ShortPathDNode(Node.Type.BLOCK,null,this,i,k);
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
	
	public void initialize() {
		for(Node n: nodes) {
			n.updateRoutingState();
		}
	}
}
