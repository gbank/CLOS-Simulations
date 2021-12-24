package Routing;

/**
 * Helper class which allows to easily compute the assignment of nodes to intervals
 * 
 * We assume nodes with IDs in the range [0,n-1] to be split into numIntervals many intervals.
 * These intervals each consists of n/numIntervals many consecutive nodes of this range.
 * If n is not divisible by numIntervals, then the first few intervals contain \lceil n / numIntervals \rceil
 * nodes and the rest contain \lfloor n/ numIntervals \rfloor.
 * 
 * Given a node ID, this class easily computes the corresponding interval of the node.
 * Similar, given an interval number, it computes the node IDs which lie in this interval.
 * 
 */
public class IntervalUtility {

	/**
	 *  For a given node index it computes to which intervall the node is assigned
	 *  
	 * @param numNodes	total number of nodes that are split into intervals
	 * @param numIntervals	number of intervals
	 * @param nodeID	ID of the node of which the interval should be computed
	 * @return	interval of the node with nodeID
	 */
	public static int getIntervalAssignment(int numNodes, int numIntervals, int nodeID) {
		int intervalSize = numNodes / numIntervals;
		if(intervalSize == 0) {
			System.err.println("Too many intervals. Some of the will be empty");
			System.exit(-1);
		}
		//Some intervals must have size > intervalSize do deal with cases where
		//numNodes is not divisible by numIntervals.
		int rest = numNodes - intervalSize * numIntervals;
		int intervalRightEnd = 0;
		int count = -1;
		do {
			count++;
			intervalRightEnd += intervalSize;
			if(rest > 0) {intervalRightEnd +=1; rest --;}
		}while(nodeID >= intervalRightEnd);
		return count;
	}
	
	/**
	 * Given an interval, this function computes the indices of nodes which belong into this interval
	 * 
	 * @param numNodes	total number of nodes that are split into intervals
	 * @param numIntervals	number of intervals to split the nodes into
	 * @param intervalID	the interval to derive the node IDs for
	 * @return	int[] array of node ids tha lie in the interval intervalID
	 */
	public static int[] getIndicesInInterval(int numNodes, int numIntervals, int intervalID) {
		int intervalSize = numNodes / numIntervals;
		if(intervalSize == 0) {
			System.err.println("Too many intervals. Some of the will be empty");
			System.exit(-1);
		}
		int rest = numNodes - intervalSize * numIntervals;
		int result[] = null;
		if(intervalID < rest) {result = new int[intervalSize+1];}
		else {result = new int[intervalSize];}
		
		int intervalStartIndex = 0;
		int intervalNum = 0;
		while(intervalID > intervalNum) {
			intervalStartIndex += intervalSize;
			if(rest > 0) {intervalStartIndex += 1; rest --;}
			intervalNum++;
		}
		for(int i = 0; i < result.length; i++) {
			result[i] = intervalStartIndex +i;
		}
		return result;
	}
	
}
