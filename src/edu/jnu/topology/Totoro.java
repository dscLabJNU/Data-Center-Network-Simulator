package edu.jnu.topology;

import java.util.LinkedList;

import edu.jnu.core.Flow;
import edu.jnu.core.Link;
import edu.jnu.core.Network;
import edu.jnu.misc.Pair;
import edu.jnu.misc.Util;

public class Totoro extends Network {

	private final int N, K;
	private final int TOT_SVR, TUP_LEN;
	private final int[] REDUN_PATHS, POW_2, POW_N;
	
	public Totoro(int n, int k) {
		super((int) Math.pow(n, k + 1) * 2); // redundant
		N = n;
		K = k;
		TOT_SVR = (int) Math.pow(n, k + 1);
		TUP_LEN = k + 1;
		REDUN_PATHS = new int[k + 1];
		POW_2 = new int[k + 1];
		POW_N = new int[k + 1];
		for (int i = 0; i < k + 1; i++) {
			REDUN_PATHS[i] = (int) Math.pow(n / 2, i);
			POW_2[i] = (int) Math.pow(2, i);
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
		Pair<Integer, Integer> path = getMidPath(src, dst, lcl);
		linkList = route(flow, src, path.T1);
		linkList.add(links[path.T1 + TOT_SVR].installFlow(flow, true));
		linkList.add(links[path.T2 + TOT_SVR].installFlow(flow, false));
		linkList.addAll(route(flow, path.T2, dst));
		return linkList;
	}

	private Pair<Integer, Integer> getMidPath(int src, int dst, int lcl) {
		if (src > dst) {
			Pair<Integer, Integer> path = getMidPath(dst, src, lcl);
			return new Pair<Integer, Integer>(path.T2, path.T1);
		}
		int[] srcTup = tidToTuple(src);
		int[] dstTup = tidToTuple(dst);
		int startInd = 0;
		for (int i = K; i >= lcl; i--) startInd += srcTup[i] * POW_N[i];
		int ranPath = Util.ranInt(0, REDUN_PATHS[lcl]); // randomly select a mid-path
		for (int i = startInd; i < TOT_SVR; i++) {
			if ((i - POW_2[lcl - 1] + 1) % POW_2[lcl] == 0 && 
					i / POW_2[lcl] % REDUN_PATHS[lcl] == ranPath) {
				int[] lnTup = tidToTuple(i);
				int[] rnTup = new int[TUP_LEN];
				System.arraycopy(lnTup, 0, rnTup, 0, TUP_LEN);
				rnTup[lcl] = dstTup[lcl];
				return new Pair<Integer, Integer>(i, tupleToTid(rnTup));
			}
		}
		assert false : "impossible";
		return null;
	}
	
	private int getLCL(int src, int dst) {
		int[] srcTup = tidToTuple(src);
		int[] dstTup = tidToTuple(dst);
		int prefix = K;
		for (; prefix > 0 && srcTup[prefix] == dstTup[prefix]; prefix--);
		return prefix;
	}
	
	private int[] tidToTuple(int tid) {
		int[] tuple = new int[TUP_LEN];
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
