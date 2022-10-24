#!/bin/sh

# trace_option: "net_thp" will print the throughput of the network per second
# top_type: "6" corresponds to Three-tier Tree
# top_tree_bran: each switch has "16" ports 
# top_rtree_ratio: we suppose no redundant ratio for Three-tier tree
# top_tree_core_cap_ratio: the rate of core link is "10Gbps" (10 * 1Gbps)
# top_tree_row_cap_ratio: the rate of aggregate link is "5Gbps" (5 * 10Gbps)

cd ..
java -Dtrace_option=net_thp -Dtop_type=6 -Dtop_tree_bran=16 -Dtop_rtree_ratio=1 \
	-Dtop_tree_core_cap_ratio=10 -Dtop_tree_row_cap_ratio=5 \
	-cp ./bin edu/jnu/script/UsingTrace ./trace/data.txt > ./result/threetietree.thp
