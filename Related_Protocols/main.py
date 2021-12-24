
from routing import PrepareSQ1
from routing import loadBIBD

from routing import RouteDetCircWithMap
from routing import RouteSQ1WithMap
from routing import RoutePRNBWithMap
from routing import RouteBIBDWithMap

from gravity_model import get_traffic_matrix
from gravity_model import set_seed

from clos import *

from random import sample
import time
import math
import sys

#Remove improt
import cProfile

def failEdgesRandomly(clos, p):
    assert clos.is_directed
    undirected_clos = clos.to_undirected()
    fEdges = sample(list(undirected_clos.edges()), int(len(undirected_clos.edges()) * p))
    fEdges = fEdges + [ (x,y) for (y,x) in fEdges]
    return set(fEdges)


isomorphism = {}
previous_variant = 0
previous_dest = None
def route(algorithm, s,d, clos, arbs,k,failedEdges, isomorphism_variant, cache_isomorphism=True):
    assert s.startswith('acc') and d.startswith('acc')
    global isomorphism
    global previous_dest
    global previous_variant
    
    if algorithm == RouteSQ1WithMap:
        PrepareSQ1(clos,'acc0', use_cached=True)

    if algorithm == RouteBIBDWithMap:
        loadBIBD(k)

    #Compute isomorphism to use arborescences also in case destination is not acc0
    if previous_dest != d or cache_isomorphism == False or previous_variant != isomorphism_variant:
        isomorphism = getRelabelMap(k,int(d[3:]),isomorphism_variant)
        previous_variant = isomorphism_variant
        
    previous_dest = d
    previous_variant = isomorphism_variant
    return algorithm(s,d,failedEdges,arbs,isomorphism)

#N must be already sorted!
def percentile(N, percent):
    k = (len(N) -1) * percent
    f = math.floor(k)
    c = math.ceil(k)
    if f == c:
        return N[int(k)]
    d0 = N[int(f)] * (c-k)
    d1 = N[int(c)] * (k-f)
    return d0+d1


#m[i][j] contains packets to send from i to j
#First fix j, then send all i
#isomorphism_variant describes which isomorphism should be used for routing: 1 for default case, 2 for shuffled core nodes
def simulateTrafficMatrix(clos, arbs, fEdges, edgeFailRate, k, matrix,  algo_list, isomorphism_variant, tag):
    assert len(matrix) == k * (k//2)
    for i in range(0,len(matrix)):
        assert len(matrix[i]) == k * (k//2)
    
    for algo in algo_list:
    
        loads = {}
        node_loads = {}
        cycles = 0
    
        avg_hop = 0.0
        max_hop = 0
        
        count = 0
        
        sTime = time.time()
        
        
        
        for dest in range(0,k*(k//2)): # TODO THIS LOOP KILLS THE PERFORMANCE! (IN ALL-TO-ONE CASE ONLY?)
            for source in range(0, k*(k//2)):
                if dest != source and matrix[source][dest] > 0.0000001:
                    (has_cycle,hops,_,_) = route(algo, 'acc' + str(source), 'acc' + str(dest) , clos, arbs,k,fEdges, isomorphism_variant)
                    count = count +1

                    weight = matrix[source][dest] # Packet weight. TODO How to include into average hop calculation? 
                    
                    if has_cycle:
                        cycles = cycles + 1
                        edges = [] # Do not count load for packets that travelled in a forwarding loop
                    else: 
                        edges = [ (hops[i], hops[i+1]) for i in range(0, len(hops) - 1)]
                        avg_hop = avg_hop + len(hops) - 1 
                        if len(hops) -1 > max_hop:
                            max_hop = len(hops) -1
            
                    for edge in edges:
                        e = edge
                        
                        if node_loads.get(e[0]) is not None:
                            node_loads[e[0]] = node_loads[e[0]] + weight
                        else:
                            node_loads[e[0]] = weight
                                                
                        
                        if e[0] > e[1]: #Consider undirected edges for edge weight
                            e = (e[1], e[0])
                        else:
                            e = (e[0], e[1])
                            
                        if loads.get(e) is not None:
                            loads[e] = loads[e] + weight
                        else:
                            loads[e] = weight

                    if not has_cycle: 
                        last_node = edges[-1][1]
                        if node_loads.get(last_node) is not None:
                            node_loads[last_node] = node_loads[last_node] + weight
                        else:
                            node_loads[last_node] = weight
    
    
        
        sorted_edge_load = sorted(list(loads.values()))
        sorted_node_load = sorted(list(node_loads.values()))
        
        #Some edges may not have been visited by any flows. Those have load 0
        total_edges = k*(k//2)*(k//2) + k * (k//2) * (k//2)
        total_nodes = (k)*(k) + (k//2)*(k//2)
        
        sorted_edge_load = [0.0] * (total_edges - len(sorted_edge_load)) + sorted_edge_load
        sorted_node_load = [0.0] * (total_nodes - len(sorted_node_load)) + sorted_node_load
        
        node_load_string = ""
        
        max_node_load = sorted_node_load[len(sorted_node_load) -1]
        second_node_load = sorted_node_load[len(sorted_node_load) - 2]
        avg_node_load = sum(sorted_node_load) / len(sorted_node_load)
        
        node_load_string = str(max_node_load) + ";" + str(second_node_load) + ";" + str(percentile(sorted_node_load,0.9999)) + ";"  + str(percentile(sorted_node_load,0.9995)) + ";" + str(percentile(sorted_node_load,0.999)) + ";" + str(percentile(sorted_node_load,0.99)) + ";" + str(percentile(sorted_node_load,0.95)) + ";" + str(percentile(sorted_node_load,0.90)) + ";" + str(percentile(sorted_node_load,0.50)) + ";"  + str(percentile(sorted_node_load,0.1)) + ";" + str(avg_node_load)
        
        max_edge_load = sorted_edge_load[len(sorted_edge_load) - 1]
        avg_edge_load = sum(sorted_edge_load) / len(sorted_edge_load)
        
        edge_load_string = str(max_edge_load) + ";" + str(percentile(sorted_edge_load,0.9999)) +  ";" + str(percentile(sorted_edge_load,0.999)) + ";" + str(percentile(sorted_edge_load,0.99)) + ";" + str(percentile(sorted_edge_load,0.95)) + ";" + str(percentile(sorted_edge_load,0.90)) + ";" + str(percentile(sorted_edge_load,0.50)) + ";"  + str(percentile(sorted_edge_load,0.1)) + ";" + str(avg_edge_load)
        
        avg_hop = avg_hop / (count - cycles) # do not include packets that went in a cycle into avg hop (they have infinity)
    
        eTime = time.time()
        print(algo.__name__  + str(tag) + ";" + str(isomorphism_variant)+ ";" +str(edgeFailRate) + ";" + str(len(fEdges))+ ";" + str(cycles) + ";" + node_load_string + ";" + edge_load_string + ";" + str(avg_hop) + ";" + str(max_hop)+ ";" + str(eTime - sTime))
    

def allToOneMatrix(k, dest):
    assert dest.startswith('acc')
    destIndex = int(dest[3:])
    
    matrix = []
    for i in range(0, k*(k//2)):
        matrix.append([0]*(destIndex) + [1] + [0]*(k*(k//2) - (destIndex+1)))
    return matrix
    
def storeMatrix(mat, fName):
    np.savetxt(fName, mat)
    
def debugMatrix(k):
    mat = np.array([np.array([1] * (k * (k//2))) for i in range(0,k*k//2)])
    return mat



def simulateAlltoOneLoad(k, destination, edgeFailRate, algo_list):
    (clos,arbs) = loadClos(k,'acc0')
    fEdges = failEdgesRandomly(clos,edgeFailRate)
    for a in algo_list:
        simulateAllToOne(a, destination, clos, arbs, k, fEdges, edgeFailRate)


list_of_possible_algos = [RouteDetCircWithMap, RoutePRNBWithMap, RouteSQ1WithMap, RouteBIBDWithMap]

k = 80

algos = [RouteDetCircWithMap]

repeats = 3


print("Loading CLOS Topology for k=" + str(k))
(clos,arbs) = loadClos(k,'acc0')

print("Computing Gravity Traffic Matrix seed=123 total=" + str((k * (k//2)) * (k * (k//2))))
set_seed(123)
gravity_matrix = get_traffic_matrix(k * (k//2), fixed_total=((k * (k//2)) * (k * (k//2))))

#storeMatrix(gravity_matrix,"gravity3200_3200x3200.txt")

all_to_one_matrix = allToOneMatrix(k, 'acc0')


print("Starting Simulation of " + str(algos))
for j in range(0,32,2):
    for repeats in range(0, repeats):
        fEdges = failEdgesRandomly(clos,j / 100)
        simulateTrafficMatrix(clos, arbs, fEdges, j / 100, k, gravity_matrix,  algos, 1, "GRAVITY")
        simulateTrafficMatrix(clos, arbs, fEdges, j / 100, k, gravity_matrix,  algos, 2, "GRAVITY")
        simulateTrafficMatrix(clos, arbs, fEdges, j / 100, k, all_to_one_matrix,  algos, 2, "A20")

