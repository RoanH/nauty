package dev.roanh.nauty.api;

import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;

public record CanonicalResult(
		int[] relabelling,
		SparseGraph canonicalGraph,
		StatsBlk stats
	){
	
	//TODO inverse
}
