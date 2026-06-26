package dev.roanh.nauty.api;

import java.util.Arrays;

import dev.roanh.nauty.NauSparse;
import dev.roanh.nauty.Nauty;
import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;

/**
 * More Java style interface to nauty.
 * @author Roan
 */
public class NautyApi{
	private final Nauty nauty = new Nauty();
	
	public void computeCanonicalLabelling(SparseGraph graph, int[] labels, int[] ptn) throws InterruptedException{
		
		
		
		int[] orbits = new int[graph.nv];
		
		
		NauSparse.sparsenauty(nauty, graph, labels, ptn, orbits, new StatsBlk(), new SparseGraph());
		
		
		System.out.println(Arrays.toString(labels));
		
		
		//TODO cannonical is not sorted, could return relab directly
		
		
	}
	
	
	
	public static void main(String[] args) throws InterruptedException{
		NautyApi nauty = new NautyApi();
		
//		0 - 1 - 2
//		| \     |
//		3 - 4 - 5
		
		SparseGraph g = new SparseGraph();
		SparseGraph.sgAlloc(g, 6, 14);
		g.nv = 6;
		g.nde = 14;
		g.v = new int[]{0,3,5,7,9,12};
		g.d = new int[]{3,2,2,2,3,2};
		g.e = new int[]{
			1,3,4,
			 0,2,
			 1,5,
			 0,4,
			 0,3,5,
			 2,4
		};
		
		int[] lab = {0,1,2,3,4,5};
			int[] ptn = {1,1,1,1,1,0};
		
		
		nauty.computeCanonicalLabelling(g, lab, ptn);
		
		System.out.println(Arrays.toString(lab));
		
		
		
		
	}
	
	
	
	
	
}
