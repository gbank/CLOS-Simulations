package Statistics;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;


/**
 * Class containing static methods that, given a Result object, print some basic information
 * about the CLOS-Topology experiments into a file.
 */

public class LoadStatistics {
	
	//Header fields of the log file
	public static final String[] HEAD = {"k",
			"NumRouter", 	//Number of routers in the CLOS topology
			"Type",			//Employed forwarding strategy
			"NumInt",		//Number of intervals when employing the interval strategy
			"ExpType",		//Name of the performed experiment
			"FailModel",	//Strategy used to decide which edges are failed
			"FailPara",		//Parameter for Edge failure strategy (e.g. fraction of failed edges)
			"FailedEdges",	//Number of failed edges in total
			"PFromServ",	//True if packets only sent from Bottom nodes. False if each server sends a packet (that is, each bottom node sends k/2 packets)
			"TotalPacks",	//Total number of packets sent throughout experiment 
			"PinCycle", 	//Absolute number of packets stuck in a forwarding loop
			"MaxLoad", 		//Node with the highest load, that is, node visited by the most packets (Integer.MAX_VALUE in case there was a forwarding loop)
			"MeanLoad", 	//Load average over all nodes in the topology
			"MaxNoCyc", 	//Node with highest load EXCLUDING nodes that lie on a cycle
			"MeanNoCyc",	//Load average over all nodes that DO NOT lie on a cycle
			"50Perc", 		//Median node load value (includes node on cycles)
			"90Perc", 		//90-th percentile of load value (includes nodes in forwarding loops)
			"95Perc"};		//95-th percentile
	
	//Note that all statistics about load do not include the destination node.

	/**
	 * Evaluates information given in result object and writes basic statistics into file.
	 * 
	 * This information is always written into the file "log.csv". If this file already exists, then a line
	 * is appended at the end of the file.
	 * 
	 * @param 	r 	Result to print into log file
	 */
	public static void writeStatistics(Result r){
		try {
			File out = new File("log.csv");
			FileWriter fw;
			if (!out.exists()) {
				fw = new FileWriter("log.csv");
				String headLine = "";
				for(String s : HEAD) {
					headLine = appendString(headLine, s);
				}
				fw.write(headLine + "\n");
			} else {
				fw = new FileWriter("log.csv",true);
			}
			String os = "";
			int k = r.k;
			
			//Basic statistics
			os = appendString(os, "" + k);
			os = appendString(os, "" + ((k*k) + (k/2)* (k/2)));
			os = appendString(os, r.type.toString());
			os = appendString(os, r.experimentType);
			os = appendString(os, "" + r.numInt);
			os = appendString(os, r.failType.toString());
			os = appendString(os, "" + r.failP);
			os = appendString(os, "" + r.numFailedEdges);
			os = appendString(os, "" + r.sentFromServers);
			os = appendString(os, "" + r.totalPacks);
			os = appendString(os, "" + r.packsInCycle);
			
			
			//Derive some more statistics from load[]
			int n = r.load.length;
			int[] load = r.load;
			
			int maxLoad;
			double mean = 0.0;
			int perc50;
			int perc90;
			int perc95;
			Arrays.sort(load);

			//If some packet travels in a cycle -> infinite load
			if(r.packsInCycle > 0) {
				os = appendString(os, "infty");
				os = appendString(os, "infty");
			}
			else {
				maxLoad = load[n-2]; //Take n-2 because in n-1 is load of d which is not interesting
				for(int i = 0; i < (n-1) ; i++) {
					mean += load[i];
				}
				mean = mean / (n-1);
				os = appendString(os, "" + maxLoad);
				os = appendString(os, "" + mean);
			}
			
			//Calculate mean but exclude cycle nodes
			double meanNoCycle = 0.0;
			int maxNoCycle = 0;
			int loadofD = r.totalPacks - r.packsInCycle; //Load of the destination node
			int counter = 0;
			for(int i = 0; i < n; i++) {
				if(load[i] != Integer.MAX_VALUE && load[i] != loadofD) {
					meanNoCycle += load[i]; counter ++;
					if(maxNoCycle < load[i]) {
						maxNoCycle = load[i];
					}
				}
			}
			meanNoCycle = meanNoCycle / counter;
			os = appendString(os, "" + maxNoCycle);
			os = appendString(os, "" + meanNoCycle);
			
			//Percentiles
			perc50 = load[(int) ((n-1) * 0.5)]; 
			perc95 = load[(int) ((n-1) * 0.95)];
			perc90 = load[(int) ((n-1) * 0.9)];
			
			os = appendString(os, "" + numberOrInfty(perc50));
			os = appendString(os, "" + numberOrInfty(perc90));
			os = appendString(os, "" + numberOrInfty(perc95));
			
			fw.append(os + "\n");

			fw.close();
		}
		catch(Exception ex) {
			System.err.println("Error when writing results to file!");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Given the result object, writes a load histogram into the file with name
	 * "hist" + r.k + ".out"
	 * 
	 * In line i this file contains the number of nodes with load i.
	 * Note that we assume the line number to be indexed starting with 0.
	 * 
	 * @param r	Result object containing load histogram to be written to file
	 */
	public static void loadHistogram(Result r) {
		try {
			FileWriter fw  = new FileWriter("hist" + r.k + ".out");
			
			int[] load = r.load;
			int maxNonCycle = 0;
			for(int i = 0; i < load.length; i++) { //length -1 because we exclude d
				if(load[i] > maxNonCycle && load[i] != Integer.MAX_VALUE && load[i] != r.totalPacks - r.packsInCycle ) {
					maxNonCycle = load[i];
				}
			}
			int[] hist = new int[maxNonCycle + 1];
			for(int i = 0; i < load.length - 1; i++) {
				if(load[i] != Integer.MAX_VALUE && load[i] != r.totalPacks - r.packsInCycle)
					hist[load[i]]++;
			}
			String os = "";
			for(int i = 0; i  < hist.length; i++) {
				os = os + hist[i] + "\n";
			}
			fw.write(os);
			fw.close();
		}catch(Exception ex) {
			System.err.println("Error when writing results to file!");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Prints the load[] of given Result object into a file called r.type + r.k + ".lst".
	 * The load values are separated by newlines ('\n').
	 * 
	 * @param r	Result object to write the load array from
	 */
	public static void printLoad(Result r) {
		try {
			FileWriter fw = new FileWriter("" + r.type + r.k  + ".lst",true);
			
			int destinationLoad = r.totalPacks - r.packsInCycle;
			
			for(int load : r.load) {
				if(load == destinationLoad) {
					System.out.println("FOUND DEST LOAD!");
				}
				else if(load == Integer.MAX_VALUE) {
					fw.write(load + "\n");
				}
				else {
					fw.write(load + "\n");
				}
			}
			fw.close();
		}catch(Exception ex) {
			System.err.println("Error when writing results to file!");
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Small helper function
	 */
	private static String appendString(String s, String value) {
		return s +  value + ";";
	}
	
	/**
	 * Returns either the given value, or "infty" in case value=Integer.MAX_INT
	 */
	private static String numberOrInfty(int value) {
		if(value == Integer.MAX_VALUE) { return "infty";}
		else {return "" +value;}
	}
}
