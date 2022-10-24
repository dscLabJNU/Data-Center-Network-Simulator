#!/bin/sh

# trace_option: "net_thp" will print the throughput of the network per second
# top_type: "2" corresponds to Fat-tree Tree
# top_ftree_port: each switch has "26" ports 

cd ..
java -Dtrace_option=net_thp -Dtop_type=2 -Dtop_ftree_port=26 \
	-cp ./bin edu/jnu/script/UsingTrace ./trace/data.txt > ./result/fattree.thp
