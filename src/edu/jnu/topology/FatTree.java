package edu.jnu.topology;

import java.util.LinkedList;

import edu.jnu.core.Flow;
import edu.jnu.core.Link;
import edu.jnu.core.Network;
import edu.jnu.misc.Util;

public class FatTree extends Network {

	private final int PORT;
	private final int TOT_SVR;
	private final int EDG_PHY_MAR, AGG_PHY_MAR;
	private final int SVR_IN_RACK, SVR_IN_POD;
	
	public FatTree(int port) {
		super((int) Math.pow(port, 3) * 3 / 4);
		PORT = port;
		TOT_SVR = (int) Math.pow(port, 3) / 4;
		EDG_PHY_MAR = TOT_SVR;
		AGG_PHY_MAR = EDG_PHY_MAR + TOT_SVR;
		SVR_IN_RACK = PORT / 2;
		SVR_IN_POD = (int) Math.pow(port, 2) / 4;
	}

	@Override
	public void installFlow(Flow flow) {
		if (flow.SRC == flow.DST) return;
		LinkedList<Link> linkList = new LinkedList<>();
		linkList.addLast(links[flow.SRC].installFlow(flow, true));
		int srcRack = flow.SRC / SVR_IN_RACK;
		int dstRack = flow.DST / SVR_IN_RACK;
		if (srcRack != dstRack) { // different rack
			int srcPod = flow.SRC / SVR_IN_POD;
			int dstPod = flow.DST / SVR_IN_POD;
			int ranAgg = Util.ranInt(0, SVR_IN_RACK);
			int srcPhyEdg = srcRack * SVR_IN_RACK + ranAgg + EDG_PHY_MAR;
			int dstPhyEdg = dstRack * SVR_IN_RACK + ranAgg + EDG_PHY_MAR;
			if (srcPod == dstPod) {
				linkList.addLast(links[srcPhyEdg].installFlow(flow, true));
				linkList.addLast(links[dstPhyEdg].installFlow(flow, false));
			} else { // different pod
				int ranCore = Util.ranInt(0, SVR_IN_RACK) + ranAgg * SVR_IN_RACK + AGG_PHY_MAR;
				int srcPhyCore = srcPod * SVR_IN_POD + ranCore;
				int dstPhyCore = dstPod * SVR_IN_POD + ranCore;
				linkList.addLast(links[srcPhyEdg].installFlow(flow, true));
				linkList.addLast(links[srcPhyCore].installFlow(flow, true));
				linkList.addLast(links[dstPhyCore].installFlow(flow, false));
				linkList.addLast(links[dstPhyEdg].installFlow(flow, false));
			}
		}
		linkList.addLast(links[flow.DST].installFlow(flow, false));
		flow.setLinks(linkList);
		trace_top_path.debug(flow.TRACE_ID + ".path=" + Util.LinksToIndexes(linkList));
	}

}
