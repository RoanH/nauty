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
	
	//relab = labels
	public void computeCanonicalLabelling2(SparseGraph graph, int[] labels, int[] ptn) throws InterruptedException{
		int[] orbits = new int[graph.nv];
		SparseGraph canon = new SparseGraph();
		StatsBlk stats = new StatsBlk();
		NauSparse.sparsenauty(nauty, graph, labels, ptn, orbits, stats, canon);
	}
	
	public void computeCanonicalLabelling(SparseGraph graph, int[] labels, int[] ptn) throws InterruptedException{
		
		
		
		int[] orbits = new int[graph.nv];
		
		
		SparseGraph canon = new SparseGraph();
		StatsBlk stats = new StatsBlk();
		NauSparse.sparsenauty(nauty, graph, labels, ptn, orbits, stats, canon);
		
		
		System.out.println(stats);
		System.out.println(Arrays.toString(labels));
		for(int i = 0; i < canon.nv; i++){
			System.out.print(i + " : " );
			int[] adj = new int[canon.d[i]];
			System.arraycopy(canon.e, canon.v[i], adj, 0, adj.length);
			Arrays.sort(adj);
			for(int e : adj){
				System.out.print(e + " ");
			}
			System.out.println();
		}
		
		
		//TODO cannonical is not sorted, could return relab directly
		
		
	}
	
	
	
	public static void main(String[] args) throws InterruptedException{
		NautyApi nauty = new NautyApi();
		
//		0 - 1 - 2
//		| \     |
//		3 - 4 - 5
		
//		SparseGraph g = new SparseGraph();
//		SparseGraph.sgAlloc(g, 6, 14);
//		g.nv = 6;
//		g.nde = 14;
//		g.d = new int[]{3,2,2,2,3,2};
//		g.v = new int[]{0,3,5,7,9,12};
//		g.e = new int[]{
//			1,3,4,
//			 0,2,
//			 1,5,
//			 0,4,
//			 0,3,5,
//			 2,4
//		};
//
//		int[] lab = {0,1,2,3,4,5};
//		int[] ptn = {1,1,1,1,1,1};
		
		
		
//		{
//			SparseGraph g = new SparseGraph();
//			g.nv = 3;
//			g.nde = 2;
//			g.d = new int[]{0, 1, 1};
//			g.v = new int[]{0, 0, 1};
//			g.e = new int[]{
//				0,
//				0
//			};
//
//			int[] lab = new int[]{0, 1, 2};
//			int[] ptn = new int[]{1, 1, 1};
//
//			nauty.computeCanonicalLabelling(g, lab, ptn);
//		}
//
//		{
//			SparseGraph g = new SparseGraph();
//			SparseGraph.sgAlloc(g, 3, 2);
//			g.nv = 3;
//			g.nde = 2;
//			g.d = new int[]{1, 0, 1};
//			g.v = new int[]{0, 1, 1};
//			g.e = new int[]{
//				1,
//				1
//			};
//
//			int[] lab = {0,1,2};
//			int[] ptn = {1,1,1};
//
//
//			nauty.computeCanonicalLabelling(g, lab, ptn);
//		}
//
//		{
//			SparseGraph g = new SparseGraph();
//			SparseGraph.sgAlloc(g, 3, 2);
//			g.nv = 3;
//			g.nde = 2;
//			g.d = new int[]{1, 1, 0};
//			g.v = new int[]{0, 1, 2};
//			g.e = new int[]{
//				2,
//				2
//			};
//
//			int[] lab = {0,1,2};
//			int[] ptn = {1,1,1};
//
//
//			nauty.computeCanonicalLabelling(g, lab, ptn);
//		}
		
		//TEST 1
//		{
//			//0 -- 1 -- 2 -- 3
//			SparseGraph g = new SparseGraph();
//			g.nv = 4;
//			g.nde = 6;
//
//			g.d = new int[]{1,2,2,1};
//			g.v = new int[]{0,1,3,5};
//			g.e = new int[]{
//			    1,
//			    0,2,
//			    1,3,
//			    2
//			};
//
//			int[] lab = {0,1,2,3};
//			int[] ptn = {1,1,1,1};
//
//			nauty.computeCanonicalLabelling(g, lab, ptn);
//		}
//
//		{
//			//2 -- 0 -- 3 -- 1
//			SparseGraph g = new SparseGraph();
//			g.nv = 4;
//			g.nde = 6;
//
//			g.d = new int[]{2,1,1,2};
//			g.v = new int[]{0,2,3,4};
//			g.e = new int[]{
//			    2,3,
//			    3,
//			    0,
//			    0,1
//			};
//
//			int[] lab = {0,1,2,3};
//			int[] ptn = {1,1,1,1};
//
//			nauty.computeCanonicalLabelling(g, lab, ptn);
//		}
		
		//TEST 2
		{
//			0
//			| \
//			3  1
//			 \ |
//			  2
			SparseGraph g = new SparseGraph();
			g.nv = 4;
			g.nde = 8;

			g.d = new int[]{2,2,2,2};
			g.v = new int[]{0,2,4,6};
			g.e = new int[]{
			    1,3,
			    0,2,
			    1,3,
			    0,2
			};
			
			int[] lab = {0,1,2,3};
			int[] ptn = {1,1,1,1};
			
			nauty.computeCanonicalLabelling(g, lab, ptn);
		}
		
		{
//			0
//			| \
//			2  3
//			 \ |
//			  1
			SparseGraph g = new SparseGraph();
			g.nv = 4;
			g.nde = 8;

			g.d = new int[]{2,2,2,2};
			g.v = new int[]{0,2,4,6};
			g.e = new int[]{
			    2,3,
			    2,3,
			    0,1,
			    0,1
			};
			
			int[] lab = {0,1,2,3};
			int[] ptn = {1,1,1,1};
			
			nauty.computeCanonicalLabelling(g, lab, ptn);
		}
		
		
//		Dreadnaut version 2.8.6 (64 bits).
//		> As
//		> d
//		> c
//		> n=4
//		> g
//		 0 : 1 3;
//		 1 : 0 2;
//		 2 : 1 3;
//		 3 : 0 2;
//		> x
//		(1 3)
//		level 2:  3 orbits; 1 fixed; index 2
//		(0 1)(2 3)
//		level 1:  1 orbit; 0 fixed; index 4
//		1 orbit; grpsize=8; 2 gens; 6 nodes; maxlev=3
//		canupdates=1; cpu time = 0.00 seconds
//		> b
//		 0 2 1 3
//		  0 :  2 3;
//		  1 :  2 3;
//		  2 :  0 1;
//		  3 :  0 1;
//		> g
//		 0 : 2 3;
//		 1 : 2 3;
//		 2 : 0 1;
//		 3 : 0 1;
//		> x
//		(2 3)
//		level 2:  3 orbits; 2 fixed; index 2
//		(0 1)
//		(0 2)(1 3)
//		level 1:  1 orbit; 0 fixed; index 4
//		1 orbit; grpsize=8; 3 gens; 8 nodes; maxlev=3
//		canupdates=1; cpu time = 0.00 seconds
//		> b
//		 0 1 2 3
//		  0 :  2 3;
//		  1 :  2 3;
//		  2 :  0 1;
//		  3 :  0 1;
//		>
		
	}
	
	
	
	
	
}
