package Statistics;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import Util.Edge;
import Topology.Node;


/**
 * Class containing static methods that, given a Result object, print some basic information
 * about the CLOS-Topology experiments into a file.
 */

public class LoadStatistics {
	
	//Header fields of the log file
	public static final String[] HEAD = {
			"k",
			"NumRouter", 	//Number of routers in the CLOS topology
			"Type",			//Employed forwarding strategy
			"NumInt",		//Number of intervals when employing the interval strategy
			"ExpType",		//Name of the performed experiment
			"FailModel",	//Strategy used to decide which edges are failed
			"FailPara",		//Parameter for Edge failure strategy (e.g. fraction of failed edges)
			"FailedEdges",	//Number of failed edges in total
			"TotalPacks",	//Total number of packets sent throughout experiment 
			"PinCycle", 	//Absolute number of packets stuck in a forwarding loop
			
			//All following metrics are computed only considering packets that do not travel in a loop
			
			"MaxNode", 		//Node with highest load
			"SecondNode",   //Node with second highest load
			"p9999Node",     //99.99 node load percentile
			"p9995Node",     
			"p999Node",
			"p99Node",
			"p95Node",
			"p90Node",
			"p50Node",
			"p10Node",
			"MeanNode", 		//Average Node Load
			
			"MaxEdge",		//Edge with highest Load
			"p9999Edge",	//99.99 percentile Edeg Load
			"p999Edge",
			"p99Edge",
			"p95Edge",
			"p90Edge",
			"p50Edge",
			"p10Edge",
			"MeanEdge",
			
			"AvgHops",		//Avg. number of hops to reach destination 
			"MaxHops"};		//Maximum number of hops  to reach destination
	

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
			int n = (k*k) + (k/2)* (k/2);
			
			//Basic statistics
			os = appendString(os, "" + k);
			os = appendString(os, "" + n);
			os = appendString(os, r.type.toString());
			os = appendString(os, r.experimentType);
			os = appendString(os, "" + r.numInt);
			os = appendString(os, r.failType.toString());
			os = appendString(os, "" + r.failP);
			os = appendString(os, "" + r.numFailedEdges);
			os = appendString(os, "" + r.totalPacks);
			os = appendString(os, "" + r.packsInCycle);
			
			
			
			int totalNodes = n;
			int totalEdges = (k/2)*(k/2)*k + k*(k/2)*(k/2);
			
			
			//Node Load Statistics
			
			ArrayList<Double> nodeLoads = new ArrayList<Double>(totalNodes);
			for(double load : r.nodeLoad.values()) {
				nodeLoads.add(load);
			}
			for(int i = nodeLoads.size(); i < totalNodes; i++) {
				nodeLoads.add(0.0);
			}
			Collections.sort(nodeLoads);
			
			double maxNodeLoad = nodeLoads.get(nodeLoads.size() -1);
			double secondNodeLoad = nodeLoads.get(nodeLoads.size() - 2);
			double averageNodeLoad = 0.0;
			for(double load : nodeLoads) {
				averageNodeLoad += load;
			}
			averageNodeLoad= averageNodeLoad / nodeLoads.size();
			
			String nodeStatistics = maxNodeLoad + ";" + secondNodeLoad + ";" + percentile(nodeLoads,0.9999)  +
													";" + percentile(nodeLoads,0.9995) +
													";" + percentile(nodeLoads,0.999) +
													";" + percentile(nodeLoads,0.99) + 
													";" + percentile(nodeLoads,0.95) + 
													";" + percentile(nodeLoads,0.9) + 
													";" + percentile(nodeLoads,0.5) +
													";" + percentile(nodeLoads,0.1) + 
													";" + averageNodeLoad;
			
			
			
			//Edge Load Statistics
			
			ArrayList<Double> edgeLoads = new ArrayList<Double>(totalEdges);
			for(double load : r.edgeLoad.values()) {
				edgeLoads.add(load);
			}
			for(int i = edgeLoads.size(); i < totalEdges; i++) {
				edgeLoads.add(0.0);
			}
			Collections.sort(edgeLoads);
			
			double maxEdgeLoad = edgeLoads.get(edgeLoads.size() -1);
			double averageEdgeLoad = 0.0;
			for(double load : edgeLoads) {
				averageEdgeLoad += load;
			}
			averageEdgeLoad = averageEdgeLoad / edgeLoads.size();
			
			String edgeStatistics = maxEdgeLoad + ";" + percentile(edgeLoads,0.9999)  +
													";" + percentile(edgeLoads,0.999) +
													";" + percentile(edgeLoads,0.99) + 
													";" + percentile(edgeLoads,0.95) + 
													";" + percentile(edgeLoads,0.9) + 
													";" + percentile(edgeLoads,0.5) +
													";" + percentile(edgeLoads,0.1) + 
													";" + averageEdgeLoad;


			os = appendString(os, "" + nodeStatistics);
			os = appendString(os, "" + edgeStatistics);

			
			os = appendString(os, "" + r.avgHops);
			os = appendString(os, "" + r.maxHops);
			
			fw.append(os + "\n");

			fw.close();
		}
		catch(Exception ex) {
			System.err.println("Error when writing results to file!");
			ex.printStackTrace();
		}
	}
	
	public static void writeFailedRun(Result r) {
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
			int n = (k*k) + (k/2)* (k/2);
			
			//Basic statistics
			os = appendString(os, "" + k);
			os = appendString(os, "" + n);
			os = appendString(os, r.type.toString());
			os = appendString(os, r.experimentType + "_FAILED");
			os = appendString(os, "" + r.numInt);
			os = appendString(os, r.failType.toString());
			os = appendString(os, "" + r.failP);
			os = appendString(os, "" + r.numFailedEdges);
			
			fw.append(os + "\n");

			fw.close();
			
		}	
			catch(Exception ex) {
				System.err.println("Error when writing results to file!");
				ex.printStackTrace();
			}
	}
	
	
	/**
	 * Small helper function
	 */
	private static String appendString(String s, String value) {
		return s +  value + ";";
	}
	
	/**
	 * Helper to calculate the percentile out of list of values
	 */
	
	public static double  percentile(ArrayList<Double> N, double percent) {
		double k = (N.size()  - 1) * percent;
		int f = (int) Math.floor(k);
		int c = (int) Math.ceil(k);
		if(f == c) {
			return N.get( (int) k);
		}
		else {
			double d0 = N.get( (int) k) * (c-k);
			double d1 = N.get( (int) c) * (k-f);
			return d0+d1;
		}
	}
	
}
