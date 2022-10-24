package edu.jnu.topology;

import java.util.LinkedList;

import edu.jnu.core.Flow;
import edu.jnu.core.Link;
import edu.jnu.core.Network;
import edu.jnu.misc.Config;
import edu.jnu.misc.Util;

public class RedunTree extends Network {

	private final int BRANCHES;
	private final int ROW_PHY_MAR;
	private final int POW_2;
	private final int CORE_PHY_MAR;
	
	public RedunTree(int branches) {
		super((int) (Math.pow(branches, 3) + // rack, not redundant
			Math.pow(branches, 2) * Config.TOP_RTREE_RATIO + // row
			Math.pow(branches, 1) * Config.TOP_RTREE_RATIO) // core
		);
		BRANCHES = branches;
		POW_2 = (int) Math.pow(branches, 2);
		ROW_PHY_MAR = (int) Math.pow(branches, 3);
		CORE_PHY_MAR = ROW_PHY_MAR + POW_2 * Config.TOP_RTREE_RATIO;
		int i = 0;
		while (i < ROW_PHY_MAR) {
			links[i++].setCapRatio(Config.TOP_TREE_RACK_CAP_RATIO);
		}
		while (i < CORE_PHY_MAR) {
			links[i++].setCapRatio(Config.TOP_TREE_ROW_CAP_RATIO);
		}
		while (i < links.length) {
			links[i++].setCapRatio(Config.TOP_TREE_CORE_CAP_RATIO);
		}
	}

	@Override
	public void installFlow(Flow flow) {
		if (flow.SRC == flow.DST) return;
		LinkedList<Link> linkList = new LinkedList<>();
		linkList.addLast(links[flow.SRC].installFlow(flow, true));
		int srcRack = flow.SRC / BRANCHES;
		int dstRack = flow.DST / BRANCHES;
		if (srcRack != dstRack) { // different rack
			int srcRow = srcRack / BRANCHES;
			int dstRow = dstRack / BRANCHES;
			int ranBran = POW_2 * Util.ranInt(0, Config.TOP_RTREE_RATIO) + ROW_PHY_MAR;
			int srcPhyRow = srcRack + ranBran;
			int dstPhyRow = dstRack + ranBran;
			if (srcRow == dstRow) {
				linkList.addLast(links[srcPhyRow].installFlow(flow, true));
				linkList.addLast(links[dstPhyRow].installFlow(flow, false));
			} else { // different row
				int srcPhyCore = (srcPhyRow - ROW_PHY_MAR) / BRANCHES + CORE_PHY_MAR;
				int dstPhyCore = (dstPhyRow - ROW_PHY_MAR) / BRANCHES + CORE_PHY_MAR;
				linkList.addLast(links[srcPhyRow].installFlow(flow, true));
				linkList.addLast(links[srcPhyCore].installFlow(flow, true));
				linkList.addLast(links[dstPhyCore].installFlow(flow, false));
				linkList.addLast(links[dstPhyRow].installFlow(flow, false));
			}
		}
		linkList.addLast(links[flow.DST].installFlow(flow, false));
		flow.setLinks(linkList);
		trace_top_path.debug(flow.TRACE_ID + ".path=" + Util.LinksToIndexes(linkList));
	}

}
