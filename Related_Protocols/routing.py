import random
import networkx as nx
import numpy as np

from networkx.algorithms.connectivity import build_auxiliary_edge_connectivity
from networkx.algorithms.flow import build_residual_network

# Route according to deterministic circular routing as described by Chiesa et al.
# source s
# destination d
# link failure set fails
# arborescence decomposition T
# isomorphism map
def RouteDetCircWithMap(s, d, fails, T, map):

    #Idea: Route along the arborescences with root acc0
    #But check for failed edges in isomorph arborescence
    for key,val in map.items(): 
        if val == s:
            s = key
            break

    detour_edges = []
    hops = 0
    switches = 0
    n = len(T[0].nodes())
    k = len(T)
    curT = hash(s) % len(T) # start on random arborescence
    hop_list = [map[s]]
    last_hop = s
    
    
    while (s != 'acc0'):

        nxt = list(T[curT].neighbors(s))
        if len(nxt) != 1:
            print("Bug: too many or to few neighbours")
        nxt = nxt[0]
        if (map[nxt], map[s]) in fails or (map[s], map[nxt]) in fails: #check failed edges with help of isomorphism
            curT = (curT+1) % k
            switches += 1
        else:
            if switches > 0 and curT > 0:
                detour_edges.append((s, nxt))
            s = nxt
            hops += 1
        if hops > n or switches > k*n:
            return (True, hop_list, switches, detour_edges)
        if s != last_hop:
            hop_list = hop_list + [map[s]]
            last_hop = s
    return (False, hop_list, switches, detour_edges)  




#build data structure for square one algorithm
SQ1 = {}
def PrepareSQ1(G, d, use_cached=False):
    global SQ1

    if use_cached and SQ1:
        return # Do not compute SQ1 Data if already computed previously
    
    H = build_auxiliary_edge_connectivity(G)
    R = build_residual_network(H, 'capacity')
    SQ1 = {n: {} for n in G}
    for u in G.nodes():
        if u.startswith('acc') and (u != d):

            paths = list(nx.edge_disjoint_paths(G, u, d, auxiliary=H, residual=R))
            k = sorted(paths, key=lambda x: len(x))
            SQ1[u][d] = k


# Route with Square One algorithm
# source s
# destination d
# link failure set fails
# arborescence decomposition T
# isomorphism map
def RouteSQ1WithMap(s, d, fails, T, map):
   
    for key,val in map.items(): 
        if val == s:
            s = key
            break

    k = len(SQ1[s]['acc0'])
    
    # Start on random shortest path instead of always in the first one to reduce congestion
    #Only works for CLOS as all paths in SQ1[s]['acc0'] are of minmal length exactly 5 if s lies in access layer
    firstPath = hash((s,d)) % k 
    curRoute = SQ1[s]['acc0'][firstPath]
    
    detour_edges = []
    index = 1
    hops = 0
    switches = 0
    c = s  # current node
    n = len(T[0].nodes())
    hop_list = [map[s]]
    last_hop = s
    while (c != 'acc0'):
        nxt = curRoute[index]
        if (map[nxt], map[c]) in fails or (map[c], map[nxt]) in fails:
            for i in range(2, index+1):
                detour_edges.append((c, curRoute[index-i]))
                c = curRoute[index-i]
                if c != s: # dont include starting point twice in list of hops
                    hop_list = hop_list + [map[c]]
            switches += 1
            c = s
            hops += (index-1)
            #
            curRoute = SQ1[s]['acc0'][(firstPath + switches) % k]
            
            index = 1
        else:
            if switches > 0:
                detour_edges.append((c, nxt))
            c = nxt
            index += 1
            hops += 1
        if hops > 3*n or switches > k*n:
            return (True, hop_list, switches, detour_edges)
        if c != last_hop:
            hop_list = hop_list + [map[c]]
            last_hop = c
    return (False, hop_list, switches, detour_edges)
    

  

matrix = []
def loadBIBD(k):
    global matrix
    if len(matrix) == 0:
        matrix = np.loadtxt("BIBD_CLOS" + str(k) + ".txt",dtype=int)
    

# Route with BIBD matrix
# source s
# destination d
# link failure set fails
# arborescence decomposition T
def RouteBIBDWithMap(s, d, fails, T, map):
    k = len(T)
    if len(matrix) == 0:
        print("Need to load matrix beforehand!")
        exit(-1)
        
    for key,val in map.items(): 
        if val == s:
            s = key
            break  
        
    detour_edges = []
    curT = matrix[hash(s) % k][0] # was int(s) need to compute larger bibd! also: why is this done via switches mode k?
    hops = 0
    switches = 0
    n = len(T[0].nodes())
    hop_list = [map[s]]
    last_hop = s
    while (s != 'acc0'):
        nxt = list(T[curT].neighbors(s))
        if len(nxt) != 1:
            print("Bug: too many or to few neighbours")
        nxt = nxt[0]
        if (map[nxt], map[s]) in fails or (map[s], map[nxt]) in fails:
            switches += 1
            curT = matrix[hash(s) % k][switches % k] 
        else:
            if switches > 0:
                detour_edges.append((s, nxt))
            s = nxt
            hops += 1
        if hops > 3*n or switches > k*n:
            return (True, hops, switches, detour_edges)
        if s != last_hop:
            hop_list = hop_list + [map[s]]
            last_hop = s
    return (False, hop_list, switches, detour_edges)






# Route randomly without bouncing as described by Chiesa et al.
# source s
# destination d
# link failure set fails
# arborescence decomposition T
# isomorphism map
def RoutePRNBWithMap(s, d, fails, T, map):
    for key,val in map.items(): 
        if val == s:
            s = key
            break

    detour_edges = []
    curT = hash((s,d)) % len(T) 

    hops = 0
    switches = 0
    n = len(T[0].nodes())
    k = len(T)
    hop_list = [map[s]]
    last_hop = s
    while (s != 'acc0'):
      
        nxt = list(T[curT].neighbors(s))
        if len(nxt) != 1:
            print("Bug: too many or to few neighbours")
        nxt = nxt[0]
        if (map[nxt], map[s]) in fails or (map[s], map[nxt]) in fails:
            newT = random.randint(0, len(T)-2)
            if newT >= curT:
                newT = (newT+1) % len(T)
            curT = newT
            switches += 1
        else:
            if switches > 0:
                detour_edges.append((s, nxt))
            s = nxt
            hops += 1
        if hops > 3*n or switches > k*n:
            return (True, hops, switches, detour_edges)
        if s != last_hop:
            hop_list = hop_list + [map[s]]
            last_hop = s
    return (False, hop_list, switches, detour_edges)

