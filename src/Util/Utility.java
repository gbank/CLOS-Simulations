package Util;

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
}
