package edu.jnu.core;

import java.util.HashSet;
import java.util.Set;

import edu.jnu.misc.Config;
import edu.jnu.misc.Trace;

public class Link {
	
	public final int ID;
	
	private Set<Flow> upFlows, downFlows;
	private double capRatio = 1;
	private double upCap, downCap;
	
	public final String TRACE_ID;
	private Trace trace_link_per_tc = Trace.GenTrace(Config.TRACE_LINK_PER_TC);
	private boolean trace = false;

	public Link(int id) {
		ID = id;
		upCap = downCap = 0;
		upFlows = new HashSet<Flow>();
		downFlows = new HashSet<Flow>();
		TRACE_ID = "link[" + id + "]";
	}
	
	public Link setCapRatio(double capRatio) {
		this.capRatio = capRatio;
		return this;
	}
	
	public void reset() {
		upCap = downCap = Config.LB_BC * capRatio;
	}
	
	public boolean isTrace() {
		return trace;
	}

	public Link setTrace(boolean trace) {
		this.trace = trace;
		return this;
	}

	public void traceRate() {
		if (trace) {
			double initCap = Config.LB_BC * capRatio;
			trace_link_per_tc.debug(TRACE_ID + ".rate",
				"up=" + String.format("%.1f Mbps", (initCap - upCap) / Config.LB_TC),
				"down=" + String.format("%.1f Mbps", (initCap - downCap) / Config.LB_TC)
			);
		}
	}
	
	public double getAllRate(Flow flow) {
		if (upFlows.contains(flow)) return upCap;
		else if (downFlows.contains(flow)) return downCap;
		else assert false : "this link does NOT contain this flow";
		return 0;
	}
	
	/**
	 * 
	 * @param flow
	 * @param direction true for up, false for down
	 * @return
	 */
	public Link installFlow(Flow flow, boolean direction) {
		if (direction) upFlows.add(flow);
		else downFlows.add(flow);
		return this;
	}
	
	public void assignFlow(Flow flow, double rate) {
		if (upFlows.contains(flow)) upCap -= rate;
		else if (downFlows.contains(flow)) downCap -= rate;
		else assert false : "this link does NOT contain this flow";
		return; 
	}
	
	public Set<Flow> getUpFlows() {
		return upFlows;
	}
	
	public Set<Flow> getDownFlows() {
		return downFlows;
	}
	
	
}
