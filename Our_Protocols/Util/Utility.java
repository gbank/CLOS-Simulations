package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Small collection of useful utility functions (all static)
 *
 */
public class Utility {
	
	/**
	 * Implementation of Fisher-Yates shuffle.
	 * Randomly shuffles the elements of a given array. The input array will be modified
	 * as no copy of the array is created
	 * 
	 * @param arr	input array to be shuffled
	 */
	public static void shuffle(Object[] arr) {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		for(int i = arr.length-1; i > 0; i--) {
			int j = rng.nextInt(i+1);
			Object temp = arr[j];
			arr[j] = arr[i];
			arr[i] = temp;
		}
	}
	
	/**
	 * Implementation of above Fisher-Yates shuffle for integer arrays.
	 * 
	 * @param arr	input array to be shuffled
	 */
	public static void shuffle(int[] arr) {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		for(int i = arr.length-1; i > 0; i--) {
			int j = rng.nextInt(i+1);
			int temp = arr[j];
			arr[j] = arr[i];
			arr[i] = temp;
		}
	}
	
	public static double[][] parseMatrix(String fName, int n){
		File fMatrix = new File(fName);
		double[][] m = new double[n][n];
		try {
			FileReader reader = new FileReader(fMatrix);
			
			int i;
			String cNumber = "";
			int index = 0;
			int index2 = 0;
			while((i = reader.read()) != -1) {
				char c = (char)i;
				if(c == ' ' || c == '\n') {
					//System.out.println(cNumber);
					m[index][index2] = Double.parseDouble(cNumber);
					index2++;
					cNumber = "";
				}
				else{
					cNumber = cNumber  + c;
				}
				if(c == '\n') {
					index ++;
					index2 = 0;
				}
			}
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return m;
	}
	
}
