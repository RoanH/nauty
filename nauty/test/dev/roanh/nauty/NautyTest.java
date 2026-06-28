package dev.roanh.nauty;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.nauty.api.NautyApi;
import dev.roanh.nauty.struct.SparseGraph;

/**
 * Some basic tests for nauty, exhaustively testing all of nauty again is not currently a goal.
 * @author Roan
 */
public class NautyTest{
	private final NautyApi nauty = new NautyApi();
	
	/*
	 * Creating expected test output with nauty's Dreadnaut (2.8.6):
	 * 
	 * General configuration:
	 * > As
	 * > d
	 * > c
	 * 
	 * Vertex count and graph (example):
	 * > n=4
	 * > g
	 *  0 : 1 3;
	 *  1 : 0 2;
	 *  2 : 1 3;
	 *  3 : 0 2;
	 * 
	 * Execute:
	 * > x
	 * (1 3)
	 * level 2:  3 orbits; 1 fixed; index 2
	 * (0 1)(2 3)
	 * level 1:  1 orbit; 0 fixed; index 4
	 * 1 orbit; grpsize=8; 2 gens; 6 nodes; maxlev=3
	 * canupdates=1; cpu time = 0.00 seconds
	 * > b
	 * 0 2 1 3      --> relabelling
	 *  0 :  2 3;   \
	 *  1 :  2 3;    | canonical graph
	 *  2 :  0 1;    | relabelled and sorted
	 *  3 :  0 1;   /
	 */

	@Test
	public void graph1() throws InterruptedException{
		// 0 - 1 - 2
		// | \     |
		// 3 - 4 - 5
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{1, 3, 4},
			new int[]{0, 2},
			new int[]{1, 5},
			new int[]{0, 4},
			new int[]{0, 3, 5},
			new int[]{2, 4}
		});
		
		int[] lab = new int[]{0, 1, 2, 3, 4, 5};
		int[] ptn = new int[]{1, 1, 1, 1, 1, 1};
		
		assertArrayEquals(
			new int[]{2, 1, 5, 3, 4, 0},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}
	
	@Test
	public void graph2Perm0() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{},
			new int[]{0},
			new int[]{0}
		});
		
		int[] lab = new int[]{0, 1, 2};
		int[] ptn = new int[]{1, 1, 1};
		
		assertArrayEquals(
			new int[]{1, 2, 0},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}
	
	@Test
	public void graph2Perm1() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{1},
			new int[]{},
			new int[]{1}
		});
		
		int[] lab = new int[]{0, 1, 2};
		int[] ptn = new int[]{1, 1, 1};
		
		assertArrayEquals(
			new int[]{0, 2, 1},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}
	
	@Test
	public void graph2Perm2() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{2},
			new int[]{2},
			new int[]{}
		});
		
		int[] lab = new int[]{0, 1, 2};
		int[] ptn = new int[]{1, 1, 1};
		
		assertArrayEquals(
			new int[]{0, 1, 2},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}
	
	@Test
	public void graph3Perm1() throws InterruptedException{
		//0 -- 1 -- 2 -- 3
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{1},
			new int[]{0, 2},
			new int[]{1, 3},
			new int[]{2}
		});
		
		int[] lab = new int[]{0, 1, 2, 3};
		int[] ptn = new int[]{1, 1, 1, 1};
		
		assertArrayEquals(
			new int[]{0, 3, 2, 1},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}
	
	@Test
	public void graph3Perm2() throws InterruptedException{
		//2 -- 0 -- 3 -- 1
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{2, 3},
			new int[]{3},
			new int[]{0},
			new int[]{0, 1}
		});
		
		int[] lab = new int[]{0, 1, 2, 3};
		int[] ptn = new int[]{1, 1, 1, 1};
		
		assertArrayEquals(
			new int[]{1, 2, 0, 3},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}
	
	@Test
	public void graph4Perm1() throws InterruptedException{
		// 0
		// | \
		// 3  1
		//  \ |
		//   2
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{1, 3},
			new int[]{0, 2},
			new int[]{1, 3},
			new int[]{0, 2}
		});
		
		int[] lab = new int[]{0, 1, 2, 3};
		int[] ptn = new int[]{1, 1, 1, 1};
		
		assertArrayEquals(
			new int[]{0, 2, 1, 3},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
		
	}
	
	@Test
	public void graph4Perm2() throws InterruptedException{
		// 0
		// | \
		// 2  3
		//  \ |
		//   1
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{2, 3},
			new int[]{2, 3},
			new int[]{0, 1},
			new int[]{0, 1}
		});
		
		int[] lab = new int[]{0, 1, 2, 3};
		int[] ptn = new int[]{1, 1, 1, 1};
		
		assertArrayEquals(
			new int[]{0, 1, 2, 3},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
		
	}
}
