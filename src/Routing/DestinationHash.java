package Routing;

import Topology.Node;

/**
 * Hash function that hashes the destination of the arriving packet
 */
public class DestinationHash extends Hash{

	/**
	 * Applies FNV1a Hash Function to the destination header field.
	 * In order for the hash value to be different at each router, we 
	 * also include the ID of the current router before computing the hash value
	 */
	@Override
	public int hash(Packet p, Node cRouter) {
		return pfnv1a(cRouter.hashCode() ^ p.destination.hashCode());
	}
	
}
