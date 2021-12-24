from arborescences import *
import time
import math
import os
import matplotlib.pyplot as plt
import shutil
import sys


from random import sample


def constructClos(k):
    assert k % 2 == 0
    G = nx.Graph()

    core  = [ 'core' + str(i) for i in range(0, (k//2) * (k//2))]
    aggregation = [ 'agg' + str(i) for i in range(0, k * (k//2))]
    access = [ 'acc' + str(i) for i in range(0, k * (k//2))]

    G.add_nodes_from(core)
    G.add_nodes_from(aggregation)
    G.add_nodes_from(access)

    # Add Edges between Aggregation and Access Layer
    # By creating k bipartite pods of size k//2
    for pod in range(0,k):
        for i in range(0, k//2):
            for j in range(0, k // 2):
                G.add_edge( aggregation[pod * (k//2) + i], access[pod * (k//2) + j])
    
    # Add edges between core and aggregation layer
    # i-th node in aggregation layer of each pod is connected to the i-th
    # set of k/2 nodes in core layer
    for pod in range(0,k):
        for i in range (0, k//2):
            for j in range (0,k//2):
                G.add_edge( aggregation[pod * (k//2) + i] , core[i* (k//2) + j])
    return G


def computeArbs(clos, k, numArbs, rootID):
	assert numArbs <= k // 2;
	for (u,v) in clos.edges():
		clos[u][v]['arb'] = -1
	clos.graph['k'] = numArbs
	clos.graph['root'] = 'acc' + str(rootID)
	return round_robin(clos,swap=True)

def printClos(clos,k):
	assert clos.is_directed()
	folder = 'clos' + str(k)
	if os.path.isdir(folder):
		shutil.rmtree(folder)
	os.mkdir(folder)
	nx.write_adjlist(clos, folder + "/clos.adj")


def printArbs(clos,k,source):
	clos.graph['root'] = source
	arbs = computeArbs(clos,k, k // 2,0)
	folder = 'clos' +  str(k) + '/' + source
	os.mkdir(folder)
	for i in range(len(arbs)):
		nx.write_adjlist(arbs[i], folder + "/arb"  + str(i) + ".adj")


def computeAndStoreClos(k):
    clos = constructClos(k).to_directed()
    printClos(clos,k)
    printArbs(clos,k,'acc0')

def loadClos(k, source):
    folder1 = 'clos' + str(k)
    folder2 = folder1 + '/' + source
    clos = nx.read_adjlist(folder1+ '/clos.adj', create_using=nx.DiGraph)
    arbs = [nx.read_adjlist(folder2 + '/arb' + str(i) + '.adj', create_using=nx.DiGraph) for i in range(0,k // 2)]
    clos.graph['root'] = 'acc0'
    clos.graph['k'] = k // 2
    return (clos,arbs)



#Computes isomorphism that can be used translate the arborescences
#with root acc0 to arborescences for root acc'target'
def getRelabelMap(k, target, variant):
    assert target < k*(k//2)
    assert variant == 1 or variant == 2
    
    inside_pod_shift = target % (k//2)
    pod_shift = target // (k//2)
    
    map = {}
    insidePodMorphMap = {} 
    podMorphMap = {}
    
    #Creat Map for morphing inside pod
    for p in range(0,k):
        for i in range(0, k//2):
            insidePodMorphMap['acc' + str(p*(k//2)+i)] = 'acc' + str(p*(k//2) + ((i + inside_pod_shift) % (k//2)))
    #Remap Agg Layer
    for p in range(0,k):
        for i in range(0, k//2):
            insidePodMorphMap['agg' + str(p*(k//2)+i)] = 'agg' + str(p*(k//2) + ((i + inside_pod_shift) % (k//2)))
    #Remap Core # Core group shifts by (k//2)*target
    for i in range(0, (k//2) * (k//2)):
        insidePodMorphMap['core' + str(i)] =  'core' + str((i + inside_pod_shift*(k//2)) % ((k//2)*(k//2)))
    
    #Create Map for morphing accross pod
    for i in range(0,k * (k //2)):
        podMorphMap['acc' + str(i)] = 'acc' + str((i + (pod_shift * (k//2))) % (k* (k//2)))

    #Remap Agg Layer
    for i in range(0,k * (k //2)):
        podMorphMap['agg' + str(i)] = 'agg' + str((i + (pod_shift * (k//2))) % (k* (k//2)))
        
    if variant == 1:
    #Core nodes stay the same
        for i in range(0, (k//2) * (k//2)):
            podMorphMap['core' + str(i)] =  'core' + str(i)
    else:
        for b in range(0, k//2):
            for i in range(0,k//2):
                podMorphMap['core' + str(b*(k//2) + i)] = 'core' + str(b*(k//2) + (i + pod_shift)%(k//2))
        
   
    for node in insidePodMorphMap.keys():
        map[node] = podMorphMap[insidePodMorphMap[node]]
    return map


#Given arborescences for acc0 computes arborescences
#for all nodes in the acc layer
#needs huge memory amounts
def transphormArbs(k):
    arbs = [nx.read_adjlist('clos' + str(k) + '/acc0/' + '/arb' + str(i) + '.adj', create_using=nx.DiGraph) for i in range(0,k // 2)]
    clos = nx.read_adjlist('clos' + str(k) +  '/clos.adj', create_using=nx.DiGraph)
    for i in range(1,k*(k//2)):
        folder = 'clos' + str(k) + '/acc' + str(i)
        if os.path.isdir(folder):
            shutil.rmtree(folder)
        os.mkdir(folder)
        map = getRelabelMap(k,i)
        new_arbs = [nx.relabel.relabel_nodes(a,map,copy=True) for a in arbs]
        for i in range(0,len(new_arbs)):
            nx.write_graph6(arbs[i], folder + "/arb"  + str(i) + ".adj")

