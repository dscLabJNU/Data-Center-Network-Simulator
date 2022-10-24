# Data Center Network Simulator (mtCloudSim)
mtCloudSim is a flow-level simulator for multi-tenant Cloud data center network

In this simulator, we have implemented the data center network architectures  of BCube, DCell, FatTree, FiConn,ThreeTierTree, and Totoro. This simulator is downloaded for free. 


# Cite
If you use the simulator, please cite the following papers.

Junjie Xie, Yuhui Deng, Geyong Min, Yongtao Zhou. An Incrementally Scalable and Cost-efficient Interconnection Structure for Datacenters. IEEE Transactions on Parallel and Distributed Systems. IEEE Press. Vol.28, No.6, 2017, pp.1578-1592. 

Junjie Xie, Yuhui Deng. mtCloudSim: A Flow-level Network Simulator for Multi-tenant Cloud. In Proceedings of the 22th IEEE International Conference on Parallel and Distributed Systems. Wuhan,China, 2016.

# Usage
The whole project is a eclipse java project. 

Overview of directories:

- src: the source code of Totoro flow-based simulator

- script: the script files to run the experiment of throughput

- trace: the workload used in the experiment; each line corresponds to a flow with its time(us), src(host id), dst(host id) and size(KB)

- result: the output of experiments


Usage of simulator:
1) Go into the "script" directory;
2) Run the script in the command line, e.g., 
	./Run-Totoro.sh
3) Find the output result in the "result" directory, e.g., totoro.thp.


```shell
#####################################################################
# The flow-based simulator for Totoro throughput experiments.
# 
# Data Management and Cloud Computing Research Lab.
# All rights reserved.
# site:		http://dsc.jnu.edu.cn
# mailto:	tyhdeng@jnu.edu.cn; xiejunjiejnu@gmail.com
#####################################################################
```