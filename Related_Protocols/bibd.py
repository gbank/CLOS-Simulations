


import networkx as nx
import random

import numpy as np

X = [i for i in range(1,22)]

#(q^2 +q +1, q+1, 1) BIBD for q=4
#Source: https://rdrr.io/cran/ibd/ bibd(21,21,5,5,1,pbar=FALSE)
A=[
[ 3,5 ,8, 10, 11],
[4,11 ,14 ,16, 18],
[3,6,12,18,20],
[2,5,9,17,18],
[1,2,11,12,13],
[2,10,14,19,20],
[1,10,15,18,21],
[2,6,8,16,21],
[8,9,12,14,15],
[7,9,11,20,21],
[7,8,13,18,19],
[1,5,6,7,14],
[4,6,9,10,13],
[4,5,12,19,21],
[3,13,14,17,21],
[2,3,4,7,15],
[6,11,15,17,19],
[5,13,15,16,20],
[7,10,12,16,17],
[1,4,8,17,20],
[1,3,9,16,19]
]


def alg2(X,A):
    n = len(X)
    k = len(A[0])
    
    m = [ [0 for _ in range(0,k)] for _ in range (0,n)]
    
    G = nx.Graph()
    for x in X:
        G.add_node('x' + str(x))
        G.add_node('A' + str(x))

    for i in range(1, len(A)+1):
        for x in A[i-1]:
            G.add_edge( 'x'+str(x),'A'+str(i))
    for j in range(1,k+1):
        P = nx.max_weight_matching(G)
        for i in range(1,n+1):
            for (u,v) in P:
                if u == 'x' + str(i):
                    m[i-1][j-1] = int(v[1:])
                elif v == 'x' + str(i):
                    m[i-1][j-1] = int(u[1:])
        for i in range(1,n+1):
            G.remove_edge('x' + str(i), 'A' + str(m[i-1][j-1]))
    return m
    

def alg3(X,A):
    n = len(X)
    k = len(A[0])
    
    Mk = alg2(X,A)
    A2 = [ list( set(X) - set(a) ) for a in A]
    Mc = alg2(X,A2)
    return [ Mk[i] + Mc[i] for i in range(0, len(Mk))] 



def extendMatrix(m,k):
    l = len(m)
    #Extend rows by copying some existing rows
    rows_to_copy = random.sample(range(0,l), k - l)
    for r in rows_to_copy:
        m.append(m[r].copy())
    #Add remaining numbers between l and k to each row 
    #in form of a random permutation of these numbers
    
    missing_numbers = list(range(l+1, k+1))
    for i in range(0,k):
        random.shuffle(missing_numbers)
        m[i] = m[i] + missing_numbers
    return m


temp_mat = alg3(X,A)

result = extendMatrix(temp_mat,40)

zero_base = [ [x-1 for x in result[i]] for i in range(0, len(result))]

np.savetxt("BIBD_CLOS80.txt",zero_base)

np.loadtxt("BIBD_CLOS80.txt",dtype=int)
