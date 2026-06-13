package dev.roanh.nauty.struct;

import dev.roanh.nauty.ds.NSet;

public class TCNode{
	public TCNode next;
	public NSet tcellptr;
	
	public TCNode(){
	}
	
	public TCNode(int n){
		tcellptr = new NSet(n);
	}
}
