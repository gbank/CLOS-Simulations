package Hashing;

import Routing.Packet;
import Topology.Node;

public class SIDHHash extends Hash{

	@Override
	public int hash(Packet p, Node cRouter) {
		return 	(fnv1a(cRouter.hashCode() ^ 1) ^
				fnv1a(p.destination.hashCode() ^ 2) ^
				fnv1a(p.last_hop.hashCode() ^ 3)^
				fnv1a(p.source.hashCode()^ 4) ^
				fnv1a(p.hopCount)) & 0x7FFFFFFF;
	}
}
