package edu.jnu.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.jnu.misc.Config;
import edu.jnu.misc.JobList;
import edu.jnu.misc.SysTime;
import edu.jnu.misc.Trace;
import edu.jnu.misc.Util;
import edu.jnu.topology.BCube;
import edu.jnu.topology.DCell;
import edu.jnu.topology.FatTree;
import edu.jnu.topology.FiConn;
import edu.jnu.topology.RedunTree;
import edu.jnu.topology.Totoro;


public class Scheduler {

	private Network network;
	private LinkedList<Flow> fList = new LinkedList<>();
	
	private Trace trace_net_thp = Trace.GenTrace(Config.TRACE_NET_THP);
	
	public Scheduler() {
		switch (Config.TOP_TYPE) {
			case 1:
				network = new Totoro(Config.TOP_TOT_N, Config.TOP_TOT_K);
				break;
			case 2: 
				network = new FatTree(Config.TOP_FTREE_PORT);
				break;
			case 3:
				network = new DCell(Config.TOP_DC_N, Config.TOP_DC_K);
				break;
			case 4:
				network = new FiConn(Config.TOP_FI_N, Config.TOP_FI_K);
				break;
			case 6:
				network = new RedunTree(Config.TOP_TREE_BRAN);
				break;
			case 8:
				network = new BCube(Config.TOP_BC_N, Config.TOP_BC_K);
				break;
		}
	}
	
	
	public void schedule() {
		if (!Config.CHECK_CONFIG()) {
			Util.println("Option error!");
			return;
		}
		
		JobList jobList = new JobList();
		double usRate = 0;
		double msRate = 0;

		addFlows(Flow.getAutoFlows());
		traceLink();
		
		while (true) {
			if (jobList.isEmpty() && fList.isEmpty()) {
				break;
			}
			
			network.flushLinkCapacity();
			
			// current rate (throughput) of the total network
			double curRate = 0;
			
			LinkedList<Flow> curList = jobList.getFlows(SysTime.US);
			if (null == curList) {
				curList = new LinkedList<Flow>();
			}
			Iterator<Flow> it = fList.iterator();
			while (it.hasNext()) {
				Flow flow = it.next();
				if (!flow.isStarted()) {
					break;
				}
				assert !flow.isFinished() : "dead flow is NOT possible to be scheduled";
				curList.add(flow);
				it.remove();
			}
			
			if (!curList.isEmpty()) {
			
				Set<Flow> doneFlows = new HashSet<>();
				doneFlows.addAll(curList);
				
				Collections.shuffle(curList); // shuffle to estimate the fairness
//				Collections.sort(curList, new Comparator<Flow>(){
//					@Override
//					public int compare(Flow f1, Flow f2) {
//						return f2.getLinks().size() - f1.getLinks().size();
//					}});
				Iterator<Flow> cIt = curList.iterator();
				cIt = curList.iterator();
				while (cIt.hasNext()) {
					Flow flow = cIt.next();
					double rate = network.assignFlow(flow, getBottleneckCapacity(flow));
					if (rate > 0) {
						curRate += rate;
						cIt.remove();
					} else {
						doneFlows.remove(flow);
					}
				}
				
				// push alive flows to next RTT
				for (Flow flow : doneFlows) {
					if (!flow.isFinished()) {
						jobList.addFlow(flow, SysTime.US + flow.getRTT());	// rtt to be improved
					} else {
						flow.destroy();
					}
				}	
				for (Flow flow : curList) {
					if (!flow.isFinished()) {
						jobList.addFlow(flow, ((long) (SysTime.US / Config.LB_TC) + 1) * Config.LB_TC);
					} else assert false : "impossible";
				}
			
			}
			// sum up the rate
			usRate += curRate;
			if (SysTime.US % 1000 == 0 && SysTime.US > 0) {		// one millisecond
				msRate += usRate;
				usRate = 0;
				if (SysTime.MS % 1000 == 0 && SysTime.MS > 0) {	// one second
					trace_net_thp.info(Trace.TimeType.SEC, 
						"net.thp=" + String.format("%.1f", msRate / 1000000));
					msRate = 0;
				}
			}
			SysTime.increase();
		}
	}
	
	public void addFlow(Flow... flow) {
		for (Flow f : flow)	{
			fList.add(f);
			network.installFlow(f);
		}
	}
	
	public void addFlows(List<Flow> flow) {
		for (Flow f : flow)	{
			fList.add(f);
			network.installFlow(f);
		}
	}
	
	private void traceLink() {
		String[] items = Config.TRACE_LINK_LIST.split(",");
		for (String item : items) {
			if (item.length() == 0) continue;
			network.getLink(Integer.parseInt(item)).setTrace(true);
		}
	}
	
	
	
	/**
	 * get the bottleneck (minimum) capacity among the links along the flow,
	 * actually the capacity the tenant can gain along the path of this flow
	 * @param flow indicates the tenant who owns this flow
	 * @return the bottleneck (minimum) capacity
	 */
	private double getBottleneckCapacity(Flow flow) {
		double bc = Double.MAX_VALUE;
		for (Link link : flow.getLinks()) {
			double allRate = link.getAllRate(flow);
			bc = allRate < bc ? allRate : bc;
		}
		return bc;
	}
	
}
