package edu.jnu.core;

import java.util.LinkedList;
import java.util.List;

import edu.jnu.misc.Config;
import edu.jnu.misc.SysTime;
import edu.jnu.misc.Trace;
import edu.jnu.misc.Util;

public class Flow {

	public final int SRC, DST;
	public final long SIZE, START;
	public final String ROUTE_ID;
	private List<Link> links;								// links along the flow path
	private int hash = 0;	
	
	private double ssthresh = Config.FLOW_INF_SSTHRESH;		// slow start threshold (bit)
	private long rSize;										// remaining size (bit)
	private double rate = Config.FLOW_MSS * Config.FLOW_INIT_CWND;	// flow rate (bpus)
	private long length = 0;								// flow length, increased every tick (us)
	private long lastTime = Integer.MIN_VALUE;				// last time the flow is invoked
	private int rtt = Config.NET_INTRA_RTT;

	public final String TRACE_ID;
	private boolean trace = false;
	private Trace trace_flow_dead = Trace.GenTrace(Config.TRACE_FLOW_DEAD);
	private Trace trace_flow_per_sec = Trace.GenTrace(Config.TRACE_FLOW_PER_SEC);
	private Trace trace_flow_cwnd = Trace.GenTrace(Config.TRACE_FLOW_CWND);
	private long lastSecSize;
	
	private static LinkedList<Flow> autoFlows = new LinkedList<>();
	
	public Flow(int src, int dst, long size, int start) {
		SRC = src;
		DST = dst;
		START = start;
		ROUTE_ID = SRC + ":" + DST;
		TRACE_ID = "flow[" + SRC + "->" + DST + "," + START + "]";
		lastSecSize = rSize = SIZE = size;
	}
	
	public static Flow genAutoFlow(int src, int dst, long size, int start) {
		Flow flow = new Flow(src, dst, size, start);
		autoFlows.add(flow);
		return flow;
	}
	
	public static List<Flow> getAutoFlows() {
		return autoFlows;
	}
	
	public boolean isTrace() {
		return trace;
	}

	public Flow setTrace(boolean trace) {
		this.trace = trace;
		return this;
	}
	
	/**
	 * Is the flow started?
	 * @param time current time
	 * @return
	 */
	public boolean isStarted()	 {
		return SysTime.US >= START && rSize > 0;
	}
	
	/**
	 * Is the flow finished, namely no bytes any more?
	 * @return
	 */
	public boolean isFinished() {
		return rSize <= 0;
	}
	
	public void destroy() {
		length = SysTime.US - START;
		trace_flow_dead.info(TRACE_ID + ".dead", 
			"timeLen=" + Util.decFormat(length) + " us", 
			"pathLen=" + links.size()
		);
	}
	
	
	/**
	 * set links along the flow path
	 * @param links
	 * @return this flow
	 */
	public Flow setLinks(List<Link> links) {
		this.links = links;
		rtt = Config.NET_INTRA_RTT * links.size() / 2;
		return this;
	}
	
	/**
	 * get links along the flow path
	 * @return
	 */
	public List<Link> getLinks() {
		return links;
	}

	
	/**
	 * get the expected rate
	 * @return
	 */
	public double getExpRate() {
		return rate;
	}

	public long getRTT() {
		return rtt;
	}
	
	/**
	 * update the flow rate for next tick and return the current available rate,
	 * note that this function assumes RTT = 100us, i.e., the rate will be updated
	 * only every 100us.
	 * @param linkAC minimum actual capacity of the links along the flow path
	 * @return
	 */
	public double updateRate(double linkAC) {
		
		double curRate = 0;
		// try to saturate the link
		if (rate <= linkAC) {
			curRate = rate;
		} else if (linkAC > 0) {
			curRate = linkAC;
		}
		rSize -= curRate;

		// congestion occurs	
		if (lastTime + rtt > SysTime.US || curRate < rate) {		
			rate = Math.max(rate / 2, Config.FLOW_MSS * 2);	
			ssthresh = rate;
		} else if (rate < ssthresh) {		
			rate *= 2;					// exponential increase in slow start
		} else {					
			rate += Config.FLOW_MSS;	// additive increase
		}
		rate = rate <= rSize ? rate : rSize;	// DO NOT violate the flow size
		if (curRate > 0) {
			lastTime = SysTime.US;
		}
		if (trace && (SysTime.US - START) % 1000000 < rtt && SysTime.US > START) {
			trace_flow_per_sec.info(Trace.TimeType.SEC, TRACE_ID + ".rate=" + 
				String.format("%d Mbps", (lastSecSize - rSize) / 1000000));
			lastSecSize = rSize;
		}
		if (trace) {
			trace_flow_cwnd.debug(TRACE_ID + ".cwnd=" +
				String.format("%.1f Mbps", curRate / Config.FLOW_MSS));
		}
		return curRate;
	}

	@Override
	public int hashCode() {
		if (hash == 0) {
			String tuple = SRC + ":" + DST + ":" + START;
			hash = tuple.hashCode();
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
        if (obj instanceof Flow) {
            Flow anotherFlow = (Flow) obj;
            return anotherFlow.SRC == SRC 
            	&& anotherFlow.DST == DST
            	&& anotherFlow.START == START;
        }
        return false;
	}
	
}
