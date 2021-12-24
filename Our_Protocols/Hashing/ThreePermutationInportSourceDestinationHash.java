package Hashing;

import Routing.Packet;
import Topology.Node;

public class ThreePermutationInportSourceDestinationHash extends Hash{
	
	int n;
	int logn;
	int numPerm;
	
	public ThreePermutationInportSourceDestinationHash(int n, int numPerm) {
		this.n = n;
		this.logn = (int)( Math.log(n) / Math.log(2));
		this.numPerm = numPerm;
	}
	
	@Override
	public int hash(Packet p, Node cRouter) {
		int num_permutation = (int) (p.hopCount / logn); 
		if(num_permutation >= numPerm) {
			num_permutation = numPerm -1;
		}
		
		int hash = fnv1a(cRouter.hashCode() + p.source.hashCode() +  p.destination.hashCode()  + num_permutation + p.last_hop.hashCode()) & 0x7FFFFFFF;
		
		return 	hash;
	}
	

}
