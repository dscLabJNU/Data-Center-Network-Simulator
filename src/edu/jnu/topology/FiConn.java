package edu.jnu.topology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.jnu.core.Flow;
import edu.jnu.core.Link;
import edu.jnu.core.Network;
import edu.jnu.misc.Pair;
import edu.jnu.misc.Util;

public class FiConn extends Network {

	private final int N, K;
	private final int TOT_SVR, TUP_LEN;
	private final int[] TK, GK;
	private final int[] POW_2;
	
	public FiConn(int n, int k) {
		super(getTotSvr(n, k) * 2); // redundant
		N = n;
		K = k;
		POW_2 = new int[k + 1];
		for (int i = 0; i < k + 1; i++) {
			POW_2[i] = (int) Math.pow(2, i);
		}
		TK = new int[k + 1];
		GK = new int[k + 1];
		TK[0] = n;
		GK[0] = 1;
		for (int level = 1; level <= k; level++) {
			GK[level] = TK[level - 1] / POW_2[level] + 1;
			TK[level] = GK[level] * TK[level - 1];
		}
		TOT_SVR = TK[k];
		TUP_LEN = k + 1;
		build(new int[TUP_LEN], k);
	}
	
	@Override
	public void installFlow(Flow flow) {
		LinkedList<Link> linkList = route(flow, flow.SRC, flow.DST);
		assert linkList.size() > 0 : Util.string("do NOT allow self-loop path");
		flow.setLinks(linkList);		
		trace_top_path.debug(flow.TRACE_ID + ".path=" + Util.LinksToIndexes(linkList));
	}
	
	private void build(int[] pref, int level) {
		if (level == 0) return;
		Arrays.fill(pref, 0, level + 1, 0);
		int base = tupleToTid(pref);
		// build child ficonns
		for (int i = 0; i < GK[level]; i++) { 
			pref[level] = i;
			build(pref, level - 1);
		}
		// connect child ficonns
		for (int i = 0; i < GK[level]; i++) {
			int k = i * POW_2[level] + POW_2[level - 1] - 1;
			for (int j = k; j < TK[level - 1]; j += POW_2[level]) {
				int left = base + i * TK[level - 1] + j;
				int right = base + ((j - POW_2[level - 1] + 1) / POW_2[level] + 1) * TK[level - 1] + k;
				assert left < right : "impossible";
				links[right + TOT_SVR] = links[left + TOT_SVR]; 
//				int[] src = tidToTuple(left);
//				int[] dst = tidToTuple(right);
//				Util.println(Util.intsToString(reverse(src)) + "->" + Util.intsToString(reverse(dst)));
			}
		}
	}
	
	private LinkedList<Link> route(Flow flow, int src, int dst) {
		LinkedList<Link> linkList = new LinkedList<>();
		if (src == dst) {
			return linkList;
		}
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
		linkList.add(links[path.T1 + TOT_SVR].installFlow(flow, path.T1 < path.T2));
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
		int[] pref = new int[TUP_LEN]; 
		System.arraycopy(srcTup, lcl + 1, pref, lcl + 1, srcTup.length - lcl - 1);
		Arrays.fill(pref, 0, lcl, 0);
		int base = tupleToTid(pref);
		int left = base + srcTup[lcl] * TK[lcl - 1] + (dstTup[lcl] - 1) * POW_2[lcl] - 1 + POW_2[lcl - 1];
		int right = base + dstTup[lcl] * TK[lcl - 1] + srcTup[lcl] * POW_2[lcl] + POW_2[lcl - 1] - 1;
//		int[] leftTup = reverse(tidToTuple(left));
//		int[] rightTup = reverse(tidToTuple(right));
		return new Pair<Integer, Integer>(left, right);
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
			tuple[k + 1] = tid / TK[k] % GK[k + 1];
		}
		tuple[0] = tid % TK[0];
		return tuple;
	}
	
	private int tupleToTid(int[] tuple){
		int tid = tuple[0];
		for(int i = K; i >= 1; i--){
			tid += tuple[i] * TK[i - 1];
		}
		return tid;
	}
	
	private static int getTotSvr(int n, int k) {
		int t = n;
		for (int level = 1; level <= k; level++) {
			t *= (t / Math.pow(2, level) + 1);
		}
		return t;
	}
	
//	private int[] reverse(int[] a) {
//		int[] b = new int[a.length];
//		for (int i = 0; i < a.length; i++) {
//			b[i] = a[a.length - 1 - i];
//		}
//		return b;
//	}
	
	private void calculateAvgPathLen(String file) {
		List<Flow> flows = new ArrayList<>(80000);
		double len = 0;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(file)));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] items = line.split(" ");
				int time = Integer.parseInt(items[0]);
				int src = Integer.parseInt(items[1]);
				int dst = Integer.parseInt(items[2]);
				int size =  Integer.parseInt(items[3]);
				flows.add(new Flow(src, dst, size, time));
			}
			for (Flow flow : flows) {
				installFlow(flow);
				len += flow.getLinks().size();
			}
			Util.println("avg path len=" + len / flows.size());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FiConn ficonn = new FiConn(8, 3);
		int[] srcTup = new int[]{3, 0, 3};
		int[] dstTup = new int[]{3, 1, 1};
		int src = ficonn.tupleToTid(srcTup);
		int dst = ficonn.tupleToTid(dstTup);
		ficonn.installFlow(Flow.genAutoFlow(src, dst, 0, 0));
		
//		ficonn.calculateAvgPathLen("e:/trace-80000-tr.txt");
//		int[] src = ficonn.tidToTuple(666);
//		int[] dst = ficonn.tidToTuple(4361);
		return;
	}

}
