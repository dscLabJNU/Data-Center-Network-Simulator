#!/bin/sh

# trace_option: "net_thp" will print the throughput of the network per second
# top_type: "8" corresponds to BCube
# top_bc_n: each switch has "16" ports 
# top_bc_k: this is a "2-level" BCube structure

cd ..
java -Dtrace_option=net_thp -Dtop_type=8 -Dtop_bc_n=16 -Dtop_bc_k=2 \
	-cp ./bin edu/jnu/script/UsingTrace ./trace/data.txt > ./result/bcube.thp
