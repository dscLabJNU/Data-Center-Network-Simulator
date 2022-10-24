#!/bin/sh

# trace_option: "net_thp" will print the throughput of the network per second
# top_type: "1" corresponds to Totoro
# top_tot_n: each switch has "16" ports 
# top_tot_k: this is a "2-level" Totoro structure

cd ..
java -Dtrace_option=net_thp -Dtop_type=1 -Dtop_tot_n=16 -Dtop_tot_k=2 \
	-cp ./bin edu/jnu/script/UsingTrace ./trace/data.txt > ./result/totoro.thp
