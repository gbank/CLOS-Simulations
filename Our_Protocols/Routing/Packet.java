package Routing;

import Topology.*;

/**
 * Object used to create a Packet (without payload) that is to be sent through the CLOS-Topology
 */
public class Packet {

	//Header field of the packet (the forwarding strategies only use some of them)
	public Node source; 
	public Node destination; 
	public Pod destinationPod;	//Could also be inferred from the destination. Added for convenience.
	public Node last_hop;		
	public int hopCount; 		//Counts the number of hops the packets have travelled.
	
	public double weight = 1;
	
	/**
	 * Basic constructor for Packet objects
	 * 
	 * @param source	From which the packet was sent (can use a dummy Node object for this)
	 * @param destination To which the packet is to be sent.
	 */
	public Packet(Node source, Node destination) {
		this.source = source;
		this.destination = destination;
		
		if(destination.type != Node.Type.BOT) {
			System.err.println("Packets may only be sent to destinations in bottom layer!");
			System.exit(-1);
		}
		this.last_hop = source;
		this.destinationPod = destination.pPod;
	}
	
}
