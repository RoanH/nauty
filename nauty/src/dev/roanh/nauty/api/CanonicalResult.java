package dev.roanh.nauty.api;

import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;

public class CanonicalResult{
	private final int[] relabelling;
	private final SparseGraph canonicalGraph;
	private final StatsBlk stats;
	private int[] invRelab = null;
	
	public CanonicalResult(int[] relabelling, SparseGraph canonGraph, StatsBlk stats){
		this.relabelling = relabelling;
		this.canonicalGraph = canonGraph;
		this.stats = stats;
	}
	
	public StatsBlk getStats(){
		return stats;
	}
	
	public SparseGraph getCanonicalGraph(){
		canonicalGraph.sort();
		return canonicalGraph;
	}
	
	public int remap(int original){
		return getInverseRelabelling()[original];
	}
	
	public int[] getInverseRelabelling(){
		if(invRelab == null){
			invRelab = new int[relabelling.length];
	 		for(int i = 0; i < relabelling.length; i++){
	 			invRelab[relabelling[i]] = i;
			}
		}
 		
 		return invRelab;
	}
	
	public int[] getRelabelling(){
		return relabelling;
	}
}
