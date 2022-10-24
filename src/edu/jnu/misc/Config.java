package edu.jnu.misc;

public class Config {

	// flow section
	
	/**
	 * max segment size (bit), 1460 bytes
	 */
	public static final double FLOW_MSS = getDouble("flow_mss", 1460 * 8);		
	
	/**
	 * initial congestion window 
	 * 10 MSS for linux version 3.2.12
	 */
	public static final int FLOW_INIT_CWND = getInt("flow_init_cwnd", 1);
	
	/**
	 * infinite slow start threshold, linux version 3.2.12
	 */
	public static final double FLOW_INF_SSTHRESH = getDouble("flow_inf_ssthresh", Double.MAX_VALUE);	
	
	//////////////////////////////////////////////////////////////////////
	// network section
	
	/**
	 * link capacity (Mbps)
	 */
	public static final double NET_LINK_CAP = getDouble("net_link_cap", 1000);
	
	/**
	 * intra-rack delay (us), from paper:
	 * 
	 */
	public static final int NET_INTRA_RTT = getInt("net_intra_rtt", 100);

	/**
	 * inter-rack delay (us), from paper:
	 * 
	 */
	public static final int NET_INTER_RTT = 250;
	
	//////////////////////////////////////////////////////////////////////
	// leak bucket section
	
	/**
	 * burst size, 
	 * now we assume that we add tokens per 1ms (= 1000us)  
	 */
	public static final double LB_BC = getDouble("lb_bc", NET_LINK_CAP * 1000); 

	/**
	 * time interval to add token (us)
	 */
	public static final long LB_TC =  (long) (NET_LINK_CAP * 1000000 / LB_BC);
	
	
	
	
	//////////////////////////////////////////////////////////////////////
	// topology section
	
	/**
	 * 0: 3-tier tree
	 * 1: totoro
	 * 2: fat-tree
	 * 3: dcell
	 * 4: ficonn
	 * 5: special tree
	 * 6: redundant tree
	 * 7: 2-level tree
	 * 8: bcube
	 * 9: exccc
	 * 10: hcube
	 */
	public static final int TOP_TYPE = getInt("top_type"); 
	
	// tree, redundant tree, 2-level tree 
	public static final int TOP_TREE_BRAN = getInt("top_tree_bran", 2); 
	
	// fat-tree
	public static final int TOP_FTREE_PORT = getInt("top_ftree_port", 4); 
	
	// totoro
	public static final int TOP_TOT_N = getInt("top_tot_n", 4); 
	public static final int TOP_TOT_K = getInt("top_tot_k", 1); 

	// redundant tree
	public static final int TOP_RTREE_RATIO = getInt("top_rtree_ratio", 2); 
	
	// tree, special tree, redundant tree, 2-level tree  
	public static final double TOP_TREE_CORE_CAP_RATIO = getInt("top_tree_core_cap_ratio", 1); 
	public static final double TOP_TREE_ROW_CAP_RATIO = getInt("top_tree_row_cap_ratio", 1); 
	public static final double TOP_TREE_RACK_CAP_RATIO = getInt("top_tree_rack_cap_ratio", 1);
	
	// bcube
	public static final int TOP_BC_N = getInt("top_bc_n", 4); 
	public static final int TOP_BC_K = getInt("top_bc_k", 1); 
	
	// dcell
	public static final int TOP_DC_N = getInt("top_dc_n", 4); 
	public static final int TOP_DC_K = getInt("top_dc_k", 1); 
	
	// ficonn
	public static final int TOP_FI_N = getInt("top_fi_n", 4); 
	public static final int TOP_FI_K = getInt("top_fi_k", 1); 
	
	// exccc
	public static final int TOP_EXCCC_S = getInt("top_exccc_s", 3); 
	public static final int TOP_EXCCC_T = getInt("top_exccc_t", 3); 
	
	// hcube
	public static final int TOP_HCUBE_M = getInt("top_hcube_m", 3); 
	
	
	//////////////////////////////////////////////////////////////////////
	// math section
	
	public static final double MATH_ERR = 1;
	
	
	
	//////////////////////////////////////////////////////////////////////
	// trace section
	
	/**
	 * "I" for info, "D" for debug and "ID"/"DI" for both 
	 */
	public static final String TRACE_LVL = getString("trace_lvl", "ID");
	
	public static final String TRACE_TXT_SEP = ", ";
	
	private static final String TRACE_OPTION = getString("trace_option", "");
	
	public static final boolean TRACE_FLOW_DEAD = getTraceOption("flow_dead", false);
	public static final boolean TRACE_FLOW_PER_SEC = getTraceOption("flow_per_sec", false);
	public static final boolean TRACE_FLOW_CWND = getTraceOption("flow_cwnd", false);
	
	public static final boolean TRACE_NET_THP = getTraceOption("net_thp", false);
	
	public static final boolean TRACE_TOP_PATH = getTraceOption("top_path", false);
	
	public static final boolean TRACE_LINK_PER_TC = getTraceOption("link_per_tc", false);
	public static final String TRACE_LINK_LIST = getString("trace_link_list", "");
	
	
	//////////////////////////////////////////////////////////////////////
	// others 
	
	public static final int ENV_ADD_TRY_TIME = 10;
	
	public static boolean CHECK_CONFIG() {
		switch(TOP_TYPE) {
		case 0: if (!(containProperty("top_tree_bran"))) return false; break;
		case 1: if (!(containProperty("top_tot_n") && containProperty("top_tot_k"))) return false; break;
		case 2: if (!(containProperty("top_ftree_port"))) return false; break;
		case 3: if (!(containProperty("top_dc_n") && containProperty("top_dc_k"))) return false; break;
		case 4: if (!(containProperty("top_fi_n") && containProperty("top_fi_k"))) return false; break;
		case 5: if (!(containProperty("top_tree_bran"))) return false; break;
		case 6: if (!(containProperty("top_tree_bran") && containProperty("top_rtree_ratio"))) return false; break;
		case 7: if (!(containProperty("top_tree_bran"))) return false; break;
		case 8: if (!(containProperty("top_bc_n") && containProperty("top_bc_k"))) return false; break;
		}
		return true;
	}
	
	public static int getInt(String key, int def) {System.getProperty(key);
		return Integer.parseInt(System.getProperty(key, String.valueOf(def)));
	}
	
	public static long getLong(String key, long def) {
		return Long.parseLong(System.getProperty(key, String.valueOf(def)));
	}
	
	public static double getDouble(String key, double def) {
		return Double.parseDouble(System.getProperty(key, String.valueOf(def)));
	}
	
	public static String getString(String key, String def) {
		return System.getProperty(key, def);
	}
	
	public static int getInt(String key) {
		return Integer.parseInt(System.getProperty(key));
	}
	
	public static long getLong(String key) {
		return Long.parseLong(System.getProperty(key));
	}
	
	public static double getDouble(String key) {
		return Double.parseDouble(System.getProperty(key));
	}
	
	public static String getString(String key) {
		return System.getProperty(key);
	}
	
	private static boolean containProperty(String key) {
		return System.getProperty(key) != null;
	}
	
	private static boolean getTraceOption(String key, boolean def) {
		return TRACE_OPTION.contains(key) | def;
	}
	
}
