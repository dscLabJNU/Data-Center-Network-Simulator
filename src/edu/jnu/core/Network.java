package edu.jnu.core;

import java.util.List;

import edu.jnu.misc.Config;
import edu.jnu.misc.SysTime;
import edu.jnu.misc.Trace;


public abstract class Network {
	
	/**
	 * links exist in this topology
	 */
	protected Link[] links;

	protected Trace trace_top_path = Trace.GenTrace(Config.TRACE_TOP_PATH);

	
	/**
	 * Constructor
	 * @param linkCount how many links in this topology
	 */
	protected Network(int linkCount) {
		links = new Link[linkCount];
		for (int i = 0; i < links.length; i++) {
			links[i] = new Link(i);
		}
	}
	
	
	public final Link[] getLinks() {
		return links;
	}
	
	public final Link getLink(int index) {
		return links[index];
	}
	
	/**
	 * before each tick, link SHOULD be set empty, namely fully available
	 */
	public final void flushLinkCapacity() {
		if (SysTime.US % Config.LB_TC == 0) {
			for (Link link : links) {
				if (link == null) {
					continue;
				}
				link.traceRate();
				link.reset();
			}
		}
	}
	
	/**
	 * update the link capacity of the whole network by the given 
	 * links, namely minus the given rate in corresponding links
	 * @param flow
	 * @param rate
	 * @return total capacity consumed 
	 */
	private double updateLinkCapacity(Flow flow, double rate) {
		List<Link> links = flow.getLinks();
		for (Link link : links) {
			link.assignFlow(flow, rate);
		}
		return rate;
	}
	
	
	/**
	 * assign a flow to the current network, namely determine the 
	 * available capacity for the given flow and update the capacity
	 * of the whole network
	 * @param flow
	 * @return network capacity consumed by the given flow 
	 */
	
	public final double assignFlow(Flow flow, double linkAC) {
		// feed the link available capacity to the flow and get the actually consumed capacity
		double rate = flow.updateRate(linkAC);
		// update the affected links
		return updateLinkCapacity(flow, rate);
	}

	
	/**
	 * install a flow to the current network, namely allocate links
	 * for this flow according to its source and destination hosts
	 * @param flow
	 */
	public abstract void installFlow(Flow flow);

	
}
