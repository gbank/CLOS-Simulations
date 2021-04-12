package Topology;

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
			if(net.type == CLOSNetwork.Type.SP_SD) {
				top[i] = new ShortPathSDNode(Node.Type.TOP,this,null,i,k);
			}
			else if(net.type == CLOSNetwork.Type.INT_D){
				top[i] = new IntervalDNode(Node.Type.TOP,this,null,i,k, net.numIntervals);
			}
			else if(net.type == CLOSNetwork.Type.INT_SD) {
				top[i] = new IntervalSDNode(Node.Type.TOP,this,null,i,k, net.numIntervals);
			}
			else if(net.type == CLOSNetwork.Type.SP_D) {
				top[i] = new ShortPathDNode(Node.Type.TOP,this,null,i,k);
			}
		}
		
		bot = new Node[k/2];
		for(int i = 0; i < k/2; i++) {
			if(net.type == CLOSNetwork.Type.SP_SD) {
				bot[i] = new ShortPathSDNode(Node.Type.BOT,this,null,i,k);
			}
			else if(net.type == CLOSNetwork.Type.INT_D){
				bot[i] = new IntervalDNode(Node.Type.BOT,this,null,i,k, net.numIntervals);
			}
			else if(net.type == CLOSNetwork.Type.INT_SD) {
				bot[i] = new IntervalSDNode(Node.Type.BOT,this,null,i,k, net.numIntervals);
			}
			else if(net.type == CLOSNetwork.Type.SP_D) {
				bot[i] = new ShortPathDNode(Node.Type.BOT,this,null,i,k);
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
	public void initialize() {
		for(Node n: top) {
			n.updateRoutingState();
		}
		for(Node n: bot) {
			n.updateRoutingState();
		}
	}
}
