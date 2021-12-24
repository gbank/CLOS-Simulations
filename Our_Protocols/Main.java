import Statistics.*;
import Topology.*;
import Topology.CLOSNetwork.*;
import Util.DisconnectException;
import Util.Utility;

public class Main {
	public static void main(String[] args) {		
	
		increasingPExperiment(0.05, 0.05, 80, false, 0.02, Type.INT_ID,7, "gravity3200_3200x3200.txt", FailType.RANDOM, 3);
		increasingPExperiment(0.05, 0.05, 80, false, 0.02, Type.INT_ID,7, "alltoone", FailType.RANDOM, 3);
		
		increasingPExperiment(0.05, 0.05, 80, false, 0.02, Type.TP_ID,6, "gravity3200_3200x3200.txt", FailType.RANDOM, 3);
		increasingPExperiment(0.05, 0.05, 80, false, 0.02, Type.TP_ID,6, "alltoone", FailType.RANDOM, 3);
	}

	public static void increasingPExperiment(double startP, double endP, int k, boolean multiply, double stepfactor, CLOSNetwork.Type type, int numIntervals, String trafficPattern,CLOSNetwork.FailType fType, int runs) {
				
		double[][] trafficMatrix = null;
		
		if(trafficPattern.trim().toLowerCase() != "alltoone") {
			System.out.println("** Reading Traffic Matrix: " + trafficPattern);
			trafficMatrix = Utility.parseMatrix(trafficPattern, k* (k / 2));
			System.out.println("** Done reading TM");
		}
		
		
		double p = startP;
		while(p<= endP){
			CLOSNetwork net = new CLOSNetwork(type,k, numIntervals);
			net.initEdges();
			
			for(int r = 0; r < runs; r++) {
				
				long start = System.currentTimeMillis();
				
				System.out.println(">> p=" + p + " Run " + (r+1));
				System.out.println(">> p=" + p + " Run " + (r+1));
				System.out.println(">> p=" + p + " Run " + (r+1));
				
				Node dest = net.randomBottomLayerNode();
				net.failEdges(fType, p, dest);
				try {
					net.initRoutingState();
				} catch (DisconnectException ex) {
					
					Result result = net.createResultObj(); result.experimentType = trafficPattern;
					LoadStatistics.writeFailedRun(result);
					
					System.err.println("ERROR:" + ex.getMessage());
					System.err.println("ERROR: DROPPING THIS RUN!");
					continue;
				}
				Result result = null;
				if(trafficPattern.trim().toLowerCase() == "alltoone") {
					result = net.allToOneRouting(dest);

				}
				else { 
					result = net.trafficMatrixRouting(trafficMatrix, trafficPattern);
				}
				LoadStatistics.writeStatistics(result);
				net.healAllEdges();
								
				
				
				long end = System.currentTimeMillis();
				System.out.println(">> Done after " + (end - start) + " ms\n");
			}
			
			System.gc();
			
			if(multiply) {
				p =  (p * stepfactor);
			}
			else {
				p =  (p + stepfactor);
			}
		}
	}
	
}
