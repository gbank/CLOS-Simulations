package Hashing;

import Routing.Packet;
import Topology.*;

/**
 * Abstract class used as a blueprint to define a hash function that maps the packet header
 * to a non-negative integer value.
 */

public abstract class Hash {

	//Instances of the implemented hash functions
	public static Hash dHash = new DestinationHash();
	public static Hash diHash = new InportDestinationHash();
	public static Hash disHash = new SourceInportDestinationHash();
	public static Hash dishHash = new SIDHHash();
	public static Hash threedHash;
	public static Hash threediHash;
	public static Hash threedisHash;
	
	public abstract int hash(Packet p, Node cRouter);

	/**
	 * Implementation of the 32-bit FNV-1a hashing function.
	 * This function is known to have good randomness and is efficient.
	 * 
	 * @param input	32-bit integer value
	 * @return		32-bit hash value
	 */
	public static int fnv1a(int input) {
		int hash = 0x811c9dc5;
		for(int i = 0; i < 4; i++) {
			hash ^= ( ((input >> i*4) & 255) & 0xff);
			hash *= 16777619;
		}
		return hash;
	}
	
}
