package dev.roanh.nauty.ds;

import java.util.BitSet;

//nauty set
public class NSet{
	private final BitSet set;
	
	public NSet(int n){
		set = new BitSet(n);
	}

	//word size is irrelevant dropped as input
	public int nextelement(int pos){
		//nauty spec: greater than pos
		//java spec: on or after
		return set.nextSetBit(pos + 1);
	}

	public void addElement(int pos){
		set.set(pos);
	}

	public void delElement(int pos){
		set.clear(pos);
	}

	public boolean isElement(int pos){
		return set.get(pos);
	}
	
	public void intersect(NSet other){
		set.and(other.set);
	}
	
	public boolean notSubSet(NSet other){
		//TODO
		#define NOTSUBSET(word1,word2) ((word1) & ~(word2))  /* test if the 1-bits
        in setword word1 do not form a subset of those in word2  */
	}
	
	//EMPTYSET
	public void clear(){
		set.clear();
	}
}
