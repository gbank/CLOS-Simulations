package Util;
import Topology.Node;

public class Edge{
	public final Node u;
	public final Node v;
	
	public Edge(Node u, Node v) {
		this.u = u;
		this.v = v;
	}
	
	@Override
	public int hashCode() {
		return u.hashCode() + v.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		
		Edge other = (Edge) o;
		
		if ((u == other.u && v == other.v) || (u == other.v && v == other.u)) { //Consider both directions of the edge to be the same
			return true;
		}
		else {
			return false;
		}
	}
	
}
