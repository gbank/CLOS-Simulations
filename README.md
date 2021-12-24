# CLOS-Simulations

Contains implementations of various local failover protocols that operate in the 3-layered Clos topology

**Our_Protocols**: This folder contains simulations of the adapted *3-Permutations* and *Intervals* protocol in JAVA

**Related_Protocols**: Contains implementations of the *DetCirc*, *PRNB*, *CASA(BIBD)* and *SquareOne* protocols of [1] in Python

# Credits
- The implementations of *DetCirc*, *PRNB*, *CASA(BIBD)* and *SquareOne* in *routing.py* was provided by [1] and modified to suit our needs
- The code *arborescences.py* is the implementation used by the experiments in [2]
- The code to compute the gravity traffix matrix in the *gravity_model.py* was written by Thomas Fenz
- In order to compute the BIBD matrix necessary for the *CASA* protocol, we used the R-library by B. N. Mandal [3]

[1] K.-T. Foerster, Y.-A. Pignolet, S. Schmid, and G. Tredan, *“Casa: Congestion and stretch aware static fast rerouting”* in Proc. IEEE INFOCOM,
2019.

[2] K.-T. Foerster, A. Kamisinski, Y.-A. Pignolet, S. Schmid, and G. Tredan, *“Bonsai: Efficient fast failover routing using small arborescences,”* in Proc. 49th IEEE/IFIP International Conference on Dependable Systems and Networks (DSN), 2019

[3] B.N. Mandal https://rdrr.io/cran/ibd/
