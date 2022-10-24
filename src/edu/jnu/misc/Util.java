package edu.jnu.misc;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.jnu.core.Link;


public class Util {

	public static int ranInt (int down, int up) {
		return (int) (Math.random() * Integer.MAX_VALUE) % (up - down) + down;
	}
	
	public static int[] ranInts (int down, int up, int count) {
		assert count <= (up - down) : "too many ints to return"; 
		int[] ret = new int[count];
		ArrayList<Integer> array = new ArrayList<>();
		for (int i = down; i < up; i++) array.add(i);
		Collections.shuffle(array);
		for (int i = 0; i < count; i++) ret[i] = array.get(i);
		return ret;
	}
	
	public static int[] ranSortedInts (int down, int up, int count) {
		int[] ret =  ranInts(down, up, count);
		Arrays.sort(ret);
		return ret;
	}
	
	/**
	 * base on Fisher-Yates-algorithm
	 * @param list
	 * @param time
	 */
	public static void shuffle(ArrayList<Integer> list, int time) {
		int size = list.size();
	    Integer temp;
	    for (int t = 0; t < time; t++) {
		    int i = size, j;
		    if (i == 0) return;
		    while (--i != 0) {
		        j = ranInt(0, list.size()) % (i + 1);
		        temp = list.get(i);
		        list.set(i, list.get(j));
		        list.set(j, temp);
		    }
	    }
	}
	
	public static double getMinValInArray(double[] array, int[] indexes) {
		double minVal = Double.MAX_VALUE;
		for (int index : indexes) {
			if (array[index] < minVal) {
				minVal = array[index];
			}
		}
		return minVal;
	}
	
	public static void minusValInArray(double[] array, int[] indexes, double val) { 
		for (int index : indexes) {
			array[index] -= val;
		}
	}
	
	public static void println(String... str) {
		for (int i = 0; i < str.length - 1; i++) {
			System.out.print(str[i] + ", ");
		}
		System.out.println(str[str.length - 1]);
	}
	
	public static void print(String... str) {
		for (int i = 0; i < str.length - 1; i++) {
			System.out.print(str[i] + ", ");
		}
		System.out.print(str[str.length - 1]);
	}
	
	public static String intsToString(int... val) {
		String txt = "{";
		for (int i = 0; i < val.length - 1; i++) {
			txt += val[i] + ", ";
		}
		txt += val[val.length - 1] + "}";
		return txt;
	}
	
	public static String LinksToIndexes(List<Link> links) {
		String txt = "{";
		for (int i = 0; i < links.size() - 1; i++) {
			txt += links.get(i).ID + ", ";
		}
		txt += links.get(links.size() - 1).ID + "}";
		return txt;
	}
	
	public static String string(String str) {
		return str;
	}
	
	public static boolean le(double n1, double n2) {
		return n1 < n2 || Math.abs(n1 - n2) < Config.MATH_ERR;
	}

	public static boolean eq(double n1, double n2) {
		return Math.abs(n1 - n2) < Config.MATH_ERR;
	}

	public static long GB(long num) {
		return num * 1000000000 * 8;
	}
	
	public static long MB(long num) {
		return num * 1000000 * 8;
	}
	
	public static long KB(long num) {
		return num * 1000 * 8;
	}
	
	public static String decFormat(long num) {
		return new DecimalFormat("#,###").format(num);
	}
	
}
