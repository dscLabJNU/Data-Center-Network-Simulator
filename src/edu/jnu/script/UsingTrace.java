package edu.jnu.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import edu.jnu.core.Flow;
import edu.jnu.core.Scheduler;
import edu.jnu.misc.Util;

public class UsingTrace {

	public static void main(String[] args) throws Exception {
		Scheduler sch = new Scheduler();
		BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] items = line.split(" ");
			int time = Integer.parseInt(items[0]);
			int src = Integer.parseInt(items[1]);
			int dst = Integer.parseInt(items[2]);
			int size =  Integer.parseInt(items[3]);
			Flow.genAutoFlow(src, dst, Util.KB(size), time);
		}
		br.close();
		sch.schedule();
	}
}
