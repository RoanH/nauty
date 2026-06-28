package dev.roanh.nauty.api;

import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;

public class CanonicalResult{
	private final int[] relabelling;
	private final SparseGraph canonicalGraph;
	private final StatsBlk stats;
	
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
	
	public int[] getInverseRelabelling(){
		int[] inv = new int[relabelling.length];
 		for(int i = 0; i < relabelling.length; i++){
			inv[relabelling[i]] = i;
		}
 		
 		return inv;
	}
	
	public int[] getRelabelling(){
		return relabelling;
	}
}
