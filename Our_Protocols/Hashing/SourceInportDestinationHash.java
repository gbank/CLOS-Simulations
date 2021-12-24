package Hashing;

import java.util.concurrent.ThreadLocalRandom;

import Routing.Packet;
import Topology.Node;

/**
 * Hash function that computes a hash value out of the destination and source address and inport of the given packet
 */
public class SourceInportDestinationHash extends Hash{

	/**
	 * Applies FNV1a Hash Function to the destination and source fields.
	 * In order for the hash value to be different at each router, we 
	 * also include the ID of the current router before computing the hash value
	 */
	@Override
	public int hash(Packet p, Node cRouter) {			

		int hash = fnv1a(cRouter.hashCode() + p.source.hashCode() + p.destination.hashCode() + p.last_hop.hashCode()) & 0x7FFFFFFF;
				
		return 	hash;
		
	}

}
