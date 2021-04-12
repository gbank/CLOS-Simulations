import Statistics.*;
import Topology.*;
import Topology.CLOSNetwork.*;

public class Main {
	public static void main(String[] args) {		
		
//		increasingKExperiment(128, 128, false,128, Type.INT_D, "alltoone", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(128, 128, false,128, Type.INT_SD, "alltoone", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(128, 128, false,128, Type.SP_D, "alltoone", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(128, 128, false,128, Type.SP_SD, "alltoone", FailType.RANDOM, 0.2, 100);
//		
//		increasingKExperiment(256, 256, false,128, Type.INT_D, "alltoone", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(256, 256, false,128, Type.INT_SD, "alltoone", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(256, 256, false,128, Type.SP_D, "alltoone", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(256, 256, false,128, Type.SP_SD, "alltoone", FailType.RANDOM, 0.2, 100);
//		
		//increasingKExperiment(896, 896, false,128, Type.INT_D, "alltoone", FailType.RANDOM, 0.2, 100);
		//increasingKExperiment(896, 896, false,128, Type.SP_D, "alltoone", FailType.RANDOM, 0.2, 100);
		//increasingKExperiment(896, 896, false,128, Type.SP_SD, "alltoone", FailType.RANDOM, 0.2, 100);
		//increasingKExperiment(896, 896, false,128, Type.INT_SD, "alltoone", FailType.RANDOM, 0.2, 100);
		
		//***********************
		
//		increasingKExperiment(128, 128, false,128, Type.INT_D, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(128, 128, false,128, Type.INT_SD, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(128, 128, false,128, Type.SP_D, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(128, 128, false,128, Type.SP_SD, "permutation", FailType.RANDOM, 0.2, 100);
//		
//		increasingKExperiment(256, 256, false,128, Type.INT_D, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(256, 256, false,128, Type.INT_SD, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(256, 256, false,128, Type.SP_D, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(256, 256, false,128, Type.SP_SD, "permutation", FailType.RANDOM, 0.2, 100);
//		
//		increasingKExperiment(384, 768, false,128, Type.INT_D, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(384, 768, false,128, Type.INT_SD, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(384, 768, false,128, Type.SP_D, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(384, 768, false,128, Type.SP_SD, "permutation", FailType.RANDOM, 0.2, 100);
		
//		increasingKExperiment(896, 896, false,128, Type.INT_D, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(896, 896, false,128, Type.SP_D, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(896, 896, false,128, Type.SP_SD, "permutation", FailType.RANDOM, 0.2, 100);
//		increasingKExperiment(896, 896, false,128, Type.INT_SD, "permutation", FailType.RANDOM, 0.2, 100);
		
		
		increasingPExperiment(0.421875, 0.421875, 128, false, 0.015625, Type.INT_D, "alltoone", FailType.DESTINATION, 100);
//		increasingPExperiment(0.0, 0.5, 128, false, 0.015625, Type.INT_SD, "alltoone",  FailType.DESTINATION, 100);
//		increasingPExperiment(0.0, 0.5, 128, false, 0.015625, Type.SP_D, "alltoone",  FailType.DESTINATION, 100);
//		increasingPExperiment(0.0, 0.5, 128, false, 0.015625, Type.SP_SD, "alltoone",  FailType.DESTINATION, 100);
		
		
//		for(CLOSNetwork.Type typ : CLOSNetwork.Type.values()) {
//			int k = 128;
//			int intervals = (int) (Math.log(k) / Math.log(2));
//			System.out.println(intervals);
//			for(int i = 0; i < 100; i++) {
//				CLOSNetwork net = new CLOSNetwork(typ, k, intervals);
//				net.initEdges();
//				Node destination = net.randomBottomLayerNode();
//				net.failEdges(CLOSNetwork.FailType.RANDOM, 0.2, null);
//				net.initRoutingState();
//				Result r = net.allToOneRouting(false, destination);
//				LoadStatistics.printLoad(r);
//				net.healAllEdges();
//			}
//		}
			
		/**
		 * How to use the CLOSNetwork class:
		 */
		//Create network 
		//CLOSNetwork net = new CLOSNetwork(CLOSNetwork.Type.INTERVAL,100, 10);
		
		//Initialize Edges
		//net.initEdges()
		
		//Choose Failure Model
		//net.failEdges(CLOSNetwork.FailType.RANDOM, 0.5, null)
		
		//Choose traffic scenario and collect result
		//Node dest = net.randomBottomLayerNode() <-- select Destination if performing all-to-one routing
		//Result r = net.allToOneRouting(false,dest)
		
		//Print basic statistics
		//LoadStatistics.writeStatistics(result);
		//LoadStatistics.loadHistogram(result);
		
		//Heal Failures to allow for choosing a different failures scenario
		//net.healAllEdges();
		
	}
	
	
	public static void increasingKExperiment(int startK, int endK, boolean multiply, double stepfactor, CLOSNetwork.Type type, String trafficPattern, CLOSNetwork.FailType fType, double fPar, int runs) {
		
		int k = startK;
		while(k <= endK){
			int log2k = (int) (Math.log(k) / Math.log(2)); //Use log2(k) as interval size (rounded down)
			CLOSNetwork net = new CLOSNetwork(type,k, log2k);
			net.initEdges();
			
			for(int r = 0; r < runs; r++) {
				System.out.println(">> k=" + k + " Run " + (r+1));
				System.out.println(">> k=" + k + " Run " + (r+1));
				System.out.println(">> k=" + k + " Run " + (r+1));
				
				Node dest = net.randomBottomLayerNode();
				net.failEdges(fType, fPar, dest);
				net.initRoutingState();
				Result result = null;
				if(trafficPattern.trim().toLowerCase() == "alltoone") {
					result = net.allToOneRouting(false, dest);
				}
				else if(trafficPattern.trim().toLowerCase() == "permutation"){
					result = net.permutationRouting();
				}
				LoadStatistics.writeStatistics(result);
				net.healAllEdges();
				
				System.gc();
			}
			
			if(multiply) {
				k = (int) (k * stepfactor);
			}
			else {
				k = (int) (k + stepfactor);
			}
		}
	}
	
	public static void increasingPExperiment(double startP, double endP, int k, boolean multiply, double stepfactor, CLOSNetwork.Type type, String trafficPattern,CLOSNetwork.FailType fType, int runs) {
		
		//For now: Set interval size to ?
		//Also: What do do with disconnected case?
		double p = startP;
		while(p<= endP){
			int log2k = (int) (Math.log(k) / Math.log(2));
			CLOSNetwork net = new CLOSNetwork(type,k, log2k);
			net.initEdges();
			
			for(int r = 0; r < runs; r++) {
				System.out.println(">> p=" + p + " Run " + (r+1));
				System.out.println(">> p=" + p + " Run " + (r+1));
				System.out.println(">> p=" + p + " Run " + (r+1));
				
				Node dest = net.randomBottomLayerNode();
				net.failEdges(fType, p, dest);
				net.initRoutingState();
				Result result = null;
				if(trafficPattern.trim().toLowerCase() == "alltoone") {
					result = net.allToOneRouting(false, dest);
				}
				else if(trafficPattern.trim().toLowerCase() == "permutation"){
					result = net.permutationRouting();
				}
				LoadStatistics.writeStatistics(result);
				net.healAllEdges();
				
				System.gc();
			}
			
			if(multiply) {
				p =  (p * stepfactor);
			}
			else {
				p =  (p + stepfactor);
			}
		}
	}
	
}
