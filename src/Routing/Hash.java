package Routing;

import Topology.*;

/**
 * Abstract class used as a blueprint to define a hash function that maps the packet header
 * to a non-negative integer value.
 */

public abstract class Hash {

	//Instances of the implemented hash functions
	public static Hash destHash = new DestinationHash();
	public static Hash destSourceHash = new SourceDestinationHash();
	
	public abstract int hash(Packet p, Node cRouter);

	/**
	 * Implementation of the 32-bit FNV-1a hashing function.
	 * This function is known to have good randomness and is very efficient.
	 * 
	 * @param input	32-bit integer value
	 * @return		32-bit hash value
	 */
	int fnv1a(int input) {
		int hash = 0x811c9dc5;
		for(int i = 0; i < 4; i++) {
			hash ^= ( ((input >> i*4) & 255) & 0xff);
			hash *= 16777619;
		}
		return hash;
	}
	
	/**
	 * Computes hash function of 32-bit integer input.
	 * Apply FNV-1a hashing function and then truncates the sign-bit in order to force
	 * the numbers to be non-negative.
	 * 
	 * @param input	32-bit integer to be hashed
	 * @return		32-bit hash value (only positive numbers used -- hence actually 31-bit number)
	 */
	int pfnv1a(int input) {
		return fnv1a(input) & 0x7FFFFFFF ;
	}
}
