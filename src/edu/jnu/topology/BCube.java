package edu.jnu.topology;

import java.util.LinkedList;

import edu.jnu.core.Flow;
import edu.jnu.core.Link;
import edu.jnu.core.Network;
import edu.jnu.misc.Util;

public class BCube extends Network {

	private final int N, K;
	private final int TOT_SVR;
	private final int[] POW_N;
	
	public BCube(int n, int k) {
		super((int) Math.pow(n, k + 1) * (k + 1)); 
		N = n;
		K = k;
		TOT_SVR = (int) Math.pow(n, k + 1);
		POW_N = new int[k + 1];
		for (int i = 0; i < k + 1; i++) {
			POW_N[i] = (int) Math.pow(n, i);
		}
	}

	@Override
	public void installFlow(Flow flow) {
		LinkedList<Link> linkList = route(flow, flow.SRC, flow.DST);
		assert linkList.size() > 0 : Util.string("do NOT allow self-loop path");
		flow.setLinks(linkList);		
		trace_top_path.debug(flow.TRACE_ID + ".path=" + Util.LinksToIndexes(linkList));
	}
	
	private LinkedList<Link> route(Flow flow, int src, int dst) {
		LinkedList<Link> linkList = new LinkedList<>();
		if (src == dst) return linkList;
		int lcl = getLCL(src, dst);
		// intra rack
		if (0 == lcl) {
			linkList.add(links[src].installFlow(flow, true));
			linkList.add(links[dst].installFlow(flow, false));
			return linkList;
		} 
		// inter rack
		int[] midTup = tidToTuple(src);
		int[] dstTup = tidToTuple(dst);
		midTup[lcl] = dstTup[lcl];
		int mid = tupleToTid(midTup);
		linkList.add(links[src + TOT_SVR * lcl].installFlow(flow, true));
		linkList.add(links[mid + TOT_SVR * lcl].installFlow(flow, false));
		linkList.addAll(route(flow, mid, dst));
		return linkList;
	}
	
	private int getLCL(int src, int dst) {
		int[] srcTup = tidToTuple(src);
		int[] dstTup = tidToTuple(dst);
		int prefix = K;
		for (; prefix > 0 && srcTup[prefix] == dstTup[prefix]; prefix--);
		return prefix;
	}
	
	private int[] tidToTuple(int tid) {
		int[] tuple = new int[K + 1];
		for (int k = K - 1; k >= 0; k--) {
			tuple[k + 1] = tid / POW_N[k + 1] % N;
		}
		tuple[0] = tid % N;
		return tuple;
	}
	
	private int tupleToTid(int[] tuple){
		int tid = tuple[0];
		for(int i = K; i >= 1; i--){
			tid += tuple[i] * POW_N[i];
		}
		return tid;
	}
}
