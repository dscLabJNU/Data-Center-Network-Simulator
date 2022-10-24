package edu.jnu.misc;

public class SysTime {

	public static long US = 0;
	public static long MS = 0;
	public static long SEC = 0;
	
	public static void increase() {
		US++;
		if (US % 1000 == 0 && US > 0) {
			MS++;
			if (MS % 1000 == 0 && MS > 0) SEC++;
		}
	}
	
}
