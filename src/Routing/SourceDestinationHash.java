package Routing;

import Topology.Node;

/**
 * Hash function that computes a hash value out of the destination and source address of the given packet
 */
public class SourceDestinationHash extends Hash{

	/**
	 * Applies FNV1a Hash Function to the destination and source fields.
	 * In order for the hash value to be different at each router, we 
	 * also include the ID of the current router before computing the hash value
	 */
	@Override
	public int hash(Packet p, Node cRouter) {
		return pfnv1a(cRouter.hashCode() ^ p.destination.hashCode()
				^ p.source.hashCode()); 
	}

}
