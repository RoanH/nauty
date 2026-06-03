package dev.roanh.nauty;

public class Nauty{
	/**
	 * Max graph size is 2 billion.
	 */
	public static final int NAUTY_INFINITY = 2000000002;
	
	
	
	
	
	
	
	public static int[] dynAllStat(){
		//effectively just a flag, but it keeps things more consistent with nauty
		return new int[0];
	}
	
	public static int[] dynAlloc1(int[] name, int sz){
		//JVM handles memory, so just make sure we are large enough
		if(sz > name.length){
			name = new int[sz];
		}
		
		return name;
	}
}
