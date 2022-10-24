#!/bin/sh

# trace_option: "net_thp" will print the throughput of the network per second
# top_type: "4" corresponds to FiConn
# top_fi_n: each switch has "16" ports 
# top_fi_k: this is a "2-level" FiConn structure

cd ..
java -Dtrace_option=net_thp -Dtop_type=4 -Dtop_fi_n=16 -Dtop_fi_k=2 \
	-cp ./bin edu/jnu/script/UsingTrace ./trace/data.txt > ./result/ficonn.thp
