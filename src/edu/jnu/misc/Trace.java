package edu.jnu.misc;


public class Trace {

	public static enum TimeType {US, MS, SEC};
	private static final boolean infoOn = Config.TRACE_LVL.contains("I");
	private static final boolean debugOn = Config.TRACE_LVL.contains("D");
	
	private boolean isOn = false;
	
	private Trace(boolean isOn) { 
		this.isOn = isOn;
	}

	public static Trace GenTrace(boolean isOn) {
		return new Trace(isOn);
	}
	
	public void info(String... item) {
		info(TimeType.US, item);
	}
	
	public void info(TimeType type, String... item) {
		if (!isOn || !infoOn) return;
		printTrace(type, item);
	}
	
	public void debug(String... item) {
		debug(TimeType.US, item);
	}
	
	public void debug(TimeType type, String... item) {
		if (!isOn || !debugOn) return;
		printTrace(type, item);
	}
	
	private void printTrace(TimeType type, String... item) {
		String txt = genTimeTxt(type);
		for (int i = 0; i < item.length - 1; i++) {
			txt += item[i] + Config.TRACE_TXT_SEP;
		}
		System.out.println(txt + item[item.length - 1]);
	}
	
	
	private String genTimeTxt(TimeType type) {
		String txt = "time=";
		switch(type) {
			case SEC: txt += Util.decFormat(SysTime.SEC) + "s"; break;
			case MS: txt += Util.decFormat(SysTime.MS) + "ms"; break;
			default: txt += Util.decFormat(SysTime.US) + "us"; 
		}
		return String.format("%-20s", txt + Config.TRACE_TXT_SEP);
	}
	
}
