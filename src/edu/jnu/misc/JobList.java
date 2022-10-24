package edu.jnu.misc;

import java.util.LinkedList;

import edu.jnu.core.Flow;

public class JobList {
 
	private LinkedList<Job> jobs = new LinkedList<>();
	private Job cache;
		
	public void addFlow(Flow flow, long time) {
		if (null == cache || cache.time != time) {
			cache = new Job(time);
			int i = jobs.size() - 1;
			for (; i >= 0; i--) {
				if (jobs.get(i).time < time) {
					jobs.add(i + 1, cache);
					break;
				} else if (jobs.get(i).time == time) {
					cache = jobs.get(i);
					break;
				}
			}
			if (i < 0) {
				jobs.add(0, cache);
			}
		}
		cache.addFlow(flow);
	}
	
	public LinkedList<Flow> getFlows (long time) {
		Job job = jobs.peek();
		if (null == job || job.getTime() != time) {
			return null;
		}
		if (job == cache) {	// do NOT cache out-of-time job
			cache = null;
		}
		return jobs.pop().getFlows();
	}
	
	public boolean isEmpty() {
		return jobs.isEmpty();
	}
	
	
	private class Job {
		private long time;
		private LinkedList<Flow> flows = new LinkedList<>();
		
		Job(long time) {
			this.time = time;
		}
		
		public long getTime() {
			return time;
		}
		
		public void addFlow(Flow flow) {
			flows.add(flow);
		}
		
		public LinkedList<Flow> getFlows() {
			return flows;
		}
		
	}
}
