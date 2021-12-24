package Topology;

import Util.DisconnectException;
import Util.Edge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import Hashing.Hash;
import Hashing.ThreePermutationDestinationHash;
import Hashing.ThreePermutationInportDestinationHash;
import Hashing.ThreePermutationInportSourceDestinationHash;
import Routing.*;
import Statistics.*;
import Util.Utility;


/**
 * This class constructs a CLOS Fat-Tree topology consisting of routers with k ports.
 * It is used to simulate the flow of packets and compare the performance of various
 * different fast local failover re-routing mechanisms.
 * 
 * The topology consists of k*(k/2 + k) many routers (henceforth also called nodes).
 * The nodes are partitioned into k/2 so-called blocks and k many pods.
 * 
 * Each block consists of k/2 many nodes and each pod contains two sets of k/2 nodes each.
 * For each pod, we call one of these sets "top" nodes and the other set "bottom" nodes.
 * 
 * (For a detailed description of this topology see:
 * 		Mohammad Al-Fares, Alexander Loukissas and Amin Vahdat
 * 		"A scalable, commodity data center network architecture"
 * 		ACM SIGCOMM Computer Communication Review, Vol. 38 2008
 * )
 */


public class CLOSNetwork {
	//Degree (number of ports) of employed routers
	int k;
	
	//Storage for block and pod partitions
	Pod[] pods;
	Block [] blocks;
	
	//A packet that travels more than LOOP_MAX steps
	//is assumed to be in a cycle
	public static final int LOOP_MAX = 1000;
	
	//Forwarding strategy which is employed by the nodes
	public enum Type{
		INT_D,
		INT_ID,
		INT_SID,
		INT_SIDH,
		SP_D,
		SP_ID,
		SP_SID,
		TP_D,
		TP_ID,
		TP_SID
	}
	
	//Strategy used for placing the edge failures
	public enum FailType{
		RANDOM,
		WCINTERVAL,
		DESTINATION
	}
	
	//Storage for forwarding and failure strategy
	Type type;
	FailType cFailType;
	double cFailP;
	int cFailedEdges;
	
	//Number of intervals when employing the interval failover strategyy
	int numIntervals;
	
	/**
	 * Creates a CLOS-Topology.
	 * Note that the parameter k directly controls the size of the network
	 * which is of order O(k^2)
	 * 
	 * @param	t	Specifies the desired failover Strategy
	 * @param	k	Degree of the employed switches
	 * @param	numIntervals	Number of Intervals when using the Interval failover strategy
	 */
	public CLOSNetwork(Type t, int k, int numIntervals) {
		if(k % 2 != 0 || k <= 0) {
			System.err.println("Degree k of CLOS must be even! Entered:" + k);
			System.exit(-1);
		}
		else {
			this.k = k;
			this.type = t;
			this.numIntervals = numIntervals;
		}
		
		
		int numPerm = 6;
		if(t == Type.TP_D) { 
			Hash.threedHash = new ThreePermutationDestinationHash(k,numPerm);
		}
		else if(t == Type.TP_ID) {
			Hash.threediHash = new ThreePermutationInportDestinationHash(k,numPerm);
		}
		else if(t == Type.TP_SID) {
			Hash.threedisHash = new ThreePermutationInportSourceDestinationHash(k,numPerm);
		}
	}
	
	/**
	 * Initializes all Pods and Blocks as well as the links (or edges) between them.
	 * This is done by inserting bi-directional edges in the following way:
	 * 	1) 	In each pod we iterate through all top nodes and the i-th top nodes is connected to
	 * 		every node in the i-th block
	 * 	2) 	Each pod is a complete bipartite graph, where each node in the top set is connected to every
	 * 		node in the bottom layer and vice-versa.
	 */
	public void initEdges() {
		System.out.println("-----------------------------------------------------------");
		System.out.println("**** Initializing CLOS Network with k=" + k +" and of type " + type.toString());
		System.out.println("** Consisting of:");
		System.out.println("**\t" + k + " Pods each with " + k + " nodes");
		System.out.println("**\t" + (k/2) + " Blocks each with " + k/2 + " nodes");
		System.out.println("** TOTAL ROUTERS: " + (k*k + (k/2) *(k/2)) + " (BOTTOM-LAYER "+ k*(k/2) + ")" + " ENDPOINTS: " + (k*(k/2)*(k/2)) +  " -- COMBINED: "+ (k*k + (k/2) *(k/2) + k*(k/2)*(k/2)));
		
		//Create Pods
		pods = new Pod[k];
		for(int i = 0; i < k; i++) {
			pods[i] = new Pod(k,i,this);
		}
		//Create Blocks
		blocks = new Block[k/2];
		for(int i = 0; i < k/2; i++) {
			blocks[i] = new Block(k,i,this);
		}
		
		System.out.println("** Adding Edges...");
		//Draw edges between all nodes
		for(int p = 0; p < pods.length; p++) {
			pods[p].connectInternalEdges();
			pods[p].connectTo(blocks);
		}
		for(int b = 0; b < blocks.length; b++) {
			blocks[b].connectTo(pods);
		}
		System.out.println("**** Topology creation completed!");
		System.out.println("-----------------------------------------------------------\n");
	}
	
	/**
	 * Sets the desired edge failing strategy.  The detailed description of each of these strategies can be
	 * found as a comment above the respective functions that are called in the switch statement.
	 * 
	 * 
	 * @param fType	Selection of the desired failure model via CLOSNetwork.FailType
	 * @param fParam	Parameter for the selected failure model
	 * @param destination	Some failure strategies are optimized w.r.t a certain destination node.
	 * 						This parameter is not used when fType = RANDOM
	 */
	public void failEdges(FailType fType, double fParam, Node destination) {
		switch(fType) {
		case RANDOM:
			failEdgesRandomly(fParam); break;
		case WCINTERVAL:
			worstCaseIntervalFailures(fParam, destination); break;
		case DESTINATION:
			failDestEdges(fParam, destination); break;
		default:
			System.err.println("Invalid Failure Model specified: " + fType + " exiting ...");
			System.exit(-1);
		}
	}
	
	
	
	/**
	 * Initialize the routing tables by calling
	 * .initialize() at each pod and block
	 * 
	 * Needs to be called after setting the failures and before starting any
	 * routing experiments.
	 * 
	 * Throws exception if a too large amount of edge failures prevent the strategy from
	 * being used.
	 */
	public void initRoutingState() throws DisconnectException{
		System.out.println("-----------------------------------------------------------");
		System.out.println("** Initializing Routing Entries of Nodes...");
		if(this.type == Type.INT_D || this.type == Type.INT_ID || this.type == Type.INT_SID) {
			System.out.println("** Number of Intervals " + numIntervals);
			System.out.println("** Size of smallest Interval " +  (k/2) / numIntervals);
		}
		for(int p = 0; p < pods.length; p++) {
			pods[p].initialize();
		}
		for(int b = 0; b < blocks.length; b++) {
			blocks[b].initialize();
		}
		System.out.println("** Entries Initialized");
		System.out.println("-----------------------------------------------------------\n");
	}
	
	
	/**
	 * Repairs any failed links in the network.
	 * This function only needs to be called when performing multiple experiments
	 * with the same CLOSNetwork object.
	 */
	public void healAllEdges() {
		for(Pod p : pods) {
			for(Node n: p.bot) {
				n.healFailures();
			}
			for(Node n: p.top) {
				n.healFailures();
			}
		}
		for(Block b: blocks) {
			for(Node n: b.nodes) {
				n.healFailures();
			}
		}
	}	
	
	
	//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ Failure Model Implementations +-+-+-+-+-+-+-+-+-+-+- 
	
	
	/**
	 * Implementation of the random failure placement model.
	 * Each link in the network is failed with probability p independently from other links
	 * 
	 * @param	p	 Probability for a fixed link to fail.
	 */
	public void failEdgesRandomly(double p) {
		System.out.println("-----------------------------------------------------------");
		System.out.println("** Failing Edges Randomly with p=" +  p + " ...");
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		int count = 0;
		int total = 0;
		
		//Store that we currently employed the RANDOM failover model
		cFailType = FailType.RANDOM;
		cFailP = p;
		for(int i = 0; i < pods.length; i++) {
			
			//Iterate only over top nodes (Note that each link has as either as end- or starting point a top node)
			for(int j = 0; j < pods[i].top.length ;j++) {
				Node cNode = pods[i].top[j];
				for(int a = 0; a < cNode.tLink.length; a++) {
					if(rng.nextDouble() <= p) {
						cNode.tFail[a] = true;
						count++;
						//Mirror failures as we consider undirected edges
						Node otherNode = cNode.tLink[a];
						otherNode.bFail[cNode.pPod.id] = true;
					}
					total++;
				}
				
				for(int a = 0; a < cNode.bLink.length; a++) {
					if(rng.nextDouble() <= p) {
						cNode.bFail[a] = true;
						count++;
						Node otherNode = cNode.bLink[a];
						otherNode.tFail[j] = true;
					}
					total++;
				}
			}
		}
		cFailedEdges = count;
		System.out.println("** Edge Failures Placed");
		System.out.println("** Failed " + count + " edges out of "  + total );
		System.out.println("-----------------------------------------------------------\n");
	}


	/**
	 * Interval failures scenario. Failures are placed in the following way:
	 * 	1)	From each interval of a block (by interval we consists a consecutive set of (k/2) / numIntervals many nodes)
	 * 		we fail a p-fraction of edges that reach into the pod of the destination. Theses edges are chosen uniformly at random
	 * 		in each such interval. Note that each block is connected to only exactly one node in the destination pod.
	 * 	2)	Inside the destination pod, we fail for each interval of the top nodes a p-fraction of edges connected to the destination
	 * In case the size of these p-fractions is not an integer, the number of failures is rounded down. In case the number of intervals
	 * is large and p is small, this can lead to failing no edges at all.
	 * 
	 * @param 	p	Fraction of edges to fail in each interval
	 * @param 	destination	Router which will later be used a the distinguished destination node. This node must lie in the bottom
	 * 						layer of some pod.
	 */
	public void worstCaseIntervalFailures(double p, Node destination) {
		System.out.println("-----------------------------------------------------------");
		System.out.println("** Failing a " +  p + " fraction of edges that ");
		System.out.println("** (1a) start in an interval in a block and (1b) reach into the destination pod");
		System.out.println("** (2a) start in the top layer of the destination pod and (2b) connect to the destination");
		Pod destPod = destination.pPod;
		int destPodID = destPod.id;
		int destID = destination.idLocal;
		int count = 0;
		cFailType = FailType.WCINTERVAL;
		cFailP = p;
		//In each interval of top nodes in destination pod
		//Fail p*intervalsize (rounded down) many edges
		for (int interval = 0; interval < numIntervals; interval++) {
			int[] nodesInInterval = IntervalUtility.getIndicesInInterval(k / 2, numIntervals, interval);
			Utility.shuffle(nodesInInterval);
			for (int i = 0; i < (int) (p * nodesInInterval.length); i++) {
				destPod.top[nodesInInterval[i]].bFail[destID] = true;
				destination.tFail[destPod.top[i].idLocal] = true;
				count++;
			}
		}
		
		//In each interval of each block, fail half of the edges reaching
		//into the destination pod.
		for(Block b: blocks){
			for(int interval = 0; interval < numIntervals; interval++) {
				int[] nodesInInterval = IntervalUtility.getIndicesInInterval(k/2, numIntervals, interval);
				Utility.shuffle(nodesInInterval);
				for (int i = 0; i < (int) (p * nodesInInterval.length); i++) {
					b.nodes[nodesInInterval[i]].bFail[destPodID] = true;
					destPod.top[b.id].tFail[i] = true;
					count++;
				}
			}
		}
		cFailedEdges = count;
		System.out.println("** Failing Edges completed!");
		System.out.println("** Failed " + count + " out of " + ((k/2) * (k/2) + k*(k/2)*(k/2)) + " total edges");
		System.out.println("-----------------------------------------------------------\n");
	}
	
	/**
	 * Fails edges directly incident to the specified destination node.
	 * A p-fraction of edges (rounded down) of all links incident to the destination is failed.
	 * This p-fraction is chosen uniformly at random.
	 * 
	 * This function requires that the destination node lies on the bottom layer of some pod.
	 * To achieve a high load, this node should be the same as the one used for the all-to-one routing.
	 * 
	 * @param p	Fraction of links to destination to be failed
	 * @param destination	Node whose links are failed
	 */
	public void failDestEdges(double p, Node destination) {
		System.out.println("-----------------------------------------------------------");
		System.out.println("** Failing a " +  p + " fraction of edges incident to the destination node ");
		
		if(destination.type != Node.Type.BOT) {
			System.err.println("Destination node must lie on bottom layer of pod!");
			System.exit(-1);
		}
		
		cFailType = FailType.DESTINATION;
		cFailP = p;
		
		int[] neighborIDs = new int[destination.tLink.length];
		for(int i = 0; i < neighborIDs.length; i++) {
			neighborIDs[i] = i;
		}
		int failCount = 0;
		Utility.shuffle(neighborIDs);
		for(int i = 0; i < (int) (neighborIDs.length * p); i++) {
			failCount++;
			Node failedPartner = destination.tLink[neighborIDs[i]];
			destination.tFail[neighborIDs[i]] = true;
			failedPartner.bFail[destination.idLocal] = true;
		}
		cFailedEdges = failCount;
		System.out.println("** Failing Edges completed!");
		System.out.println("** Failed " + failCount + " out of the " + destination.tLink.length + " edges of the destination");
		System.out.println("-----------------------------------------------------------\n");
	}
	
	
	//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ Packet Routing Implementations +-+-+-+-+-+-+-+-+-+-+- 
	
	/**
	 * Route a packet through the CLOS Network. The forwarding decision is made by
	 * the Node objects and depends on the concrete type of node used.
	 * 
	 * In case a packet travels more than LOOP_MAX many hops, it is assumed to be 
	 * trapped in a forwarding loop and terminated. Each node inside such loop then has
	 * its load set to Integer.MAX_VALUE
	 * 
	 * @param p	Packet to forward through the CLOS topology
	 * @param start	Node at which the packet arrives
	 * @return	Path of nodes that the packet travel to arrive at the destination
	 * 			If the list has length 2*LOOP_MAX then the packet did not arrive at the destination
	 */
	private ArrayList<Node> routePacket(Packet p, Node start) {
		Node next = start.forward(p);
		ArrayList<Node> path = new ArrayList<Node>(10);
		path.add(start);
		while(next != null) {
			path.add(next);
			if(p.hopCount > 2* LOOP_MAX) {return path;}
			next = next.forward(p);
		}
		return path;
	}
	
	public Result allToOneRouting(Node dest) {
		if( ! (dest.type == Node.Type.BOT)) {
			System.err.println("Destination must lie on bottom layer!");
			System.exit(-1);
		}
		
		int dest_index = dest.pPod.id * (k/2) + dest.idLocal;
		
		int num_bot_nodes = k* k/2;
		
		double[][] matrix = new double[num_bot_nodes][num_bot_nodes];
		for(double[] row : matrix) {
			row[dest_index] = 1.0;
		}
	
		return trafficMatrixRouting(matrix, "A2O");
		
	}
	
	
	
	public Result trafficMatrixRouting(double[][] matrix, String expName) {
		System.out.println("-----------------------------------------------------------");
		System.out.println("** Traffix Matrix Routing (flows between bottom layer nodes only)." );
		System.out.println("** Entry M[i][j] contains weight of flow sent from i'th to j'th bottom layer node");
		
		int loopCount = 0;
		int totalPacks = 0;
		
		double avg_hops = 0.0; //Average hops of flow
		int max_hops = 0; //Max hop of flow
		
		HashMap<Node,Double> nodeLoad = new HashMap<Node,Double>();
		HashMap<Edge, Double> edgeLoad = new HashMap<Edge, Double>();
		
		for(int p = 0; p < pods.length; p++) {
			for(int i = 0; i < pods[p].bot.length; i++) { // From every destination i
				
				Node source = pods[p].bot[i];
				
				for(int q = 0; q < pods.length; q++) {
					for(int j = 0; j < pods[q].bot.length; j++) {
						
						Node destination = pods[q].bot[j];
						double packet_weight = matrix[p*(k/2) + i][q*(k/2) + j];
						
						if(source == destination || packet_weight <0.0000001) { continue; }
						
						Packet pack = new Packet(source,destination);
						pack.weight = packet_weight;
						
						ArrayList<Node> hops = routePacket(pack,source);
						
						//Compute statistics
						if(hops.size()-1 >= 2*LOOP_MAX - 10) {
							loopCount++;
						}
						else{ // Do not include packets that traveled in loop in statistic calculation
							  // Such packets cause all values to be infinite anyways
							if(hops.size()-1 > max_hops) {
								max_hops = hops.size()-1;
							}
							avg_hops += hops.size()-1;
							
							for(Node n: hops) { //Add load to nodes
								if(nodeLoad.get(n) != null) {
									nodeLoad.put(n, nodeLoad.get(n) + pack.weight);
								}
								else {nodeLoad.put(n, pack.weight);}
							}
							
							
							if(hops.size() > 1) {
								for(int u = 0; u < hops.size() - 1; u++) {
									Node n1 = hops.get(u); Node n2 = hops.get(u+1);
									Edge edge = new Edge(n1,n2);
									
									if(edgeLoad.get(edge) != null) {
										edgeLoad.put(edge,  edgeLoad.get(edge) + pack.weight);
									}
									else {
										edgeLoad.put(edge, pack.weight);
									}
								}
							}
						}
						totalPacks++;
					}
				}
		
			}
		}
		
		avg_hops = avg_hops / (totalPacks - loopCount);
		
		//Store results in array (also reset load counts at the same time)
		//Also clean-up load values by setting them back to 0
		Result r= createResultObj();
		r.packsInCycle = loopCount;
		r.sentFromServers = false;
		r.totalPacks = totalPacks;
		r.experimentType = expName;
		r.avgHops = avg_hops;
		r.maxHops = max_hops;
		r.nodeLoad = nodeLoad;
		r.edgeLoad = edgeLoad; 
				
		System.out.println("** Experiment completed. Sent " +  totalPacks + " many Packets");
		System.out.println("-----------------------------------------------------------\n");
		
		return r;
	}
	
	
	
	//+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ MISC +-+-+-+-+-+-+-+-+-+-+- 
	
	
	/**
	 * Create a Result object to store basic information about the experiment.
	 * Initialize some basic information such as the network parameters.
	 * 
	 * @return	Result object.
	 */
	public Result createResultObj() {
		Result r = new Result();
		r.k = k;
		r.failType = cFailType;
		r.type = type;
		r.numInt = numIntervals;
		r.failP = cFailP;
		r.numFailedEdges = cFailedEdges;
		return r;
	}
	
	/**
	 * Select a node in the bottom layer of some pod uniformly at random.
	 * 
	 * @return	Randomly selected node.
	 */
	public Node randomBottomLayerNode() {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		int startingPod = rng.nextInt(pods.length);
		int startingNode = rng.nextInt(pods[startingPod].bot.length);
		return pods[startingPod].bot[startingNode];
	}	
}

