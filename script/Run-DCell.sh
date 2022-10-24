#!/bin/sh

# trace_option: "net_thp" will print the throughput of the network per second
# top_type: "3" corresponds to DCell
# top_dc_n: each switch has "8" ports 
# top_dc_k: this is a "2-level" DCell structure

cd ..
java -Dtrace_option=net_thp -Dtop_type=3 -Dtop_dc_n=8 -Dtop_dc_k=2 \
	-cp ./bin edu/jnu/script/UsingTrace ./trace/data.txt > ./result/dcell.thp
