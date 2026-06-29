package dev.roanh.nauty;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import dev.roanh.nauty.api.NautyApi;
import dev.roanh.nauty.struct.SparseGraph;

/**
 * Some basic tests for nauty, exhaustively testing all of nauty again is not currently a goal.
 * @author Roan
 */
public class NautyTest{
	private final NautyApi nauty = new NautyApi();
	private static final int[][] TEST_GRAPH = new int[][]{
		new int[]{2, 3},
		new int[]{},
		new int[]{6},
		new int[]{7},
		new int[]{1},
		new int[]{1},
		new int[]{5},
		new int[]{4}
	};
	
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
	
	@Test
	public void fullCanon0() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{5},
			new int[]{4},
			new int[]{7, 6},
			new int[]{},
			new int[]{3},
			new int[]{3},
			new int[]{1},
			new int[]{0},
		});
		
		int[] lab = new int[]{4, 5, 6, 7, 0, 1, 2, 3};
		int[] ptn = new int[]{0, 0, 1, 0, 1, 1, 1, 0};
		
		assertArrayEquals(
			new int[]{4, 5, 6, 7, 2, 1, 0, 3},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}

	@Test
	public void fullCanon1() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{5},
			new int[]{4},
			new int[]{7, 6},
			new int[]{},
			new int[]{3},
			new int[]{3},
			new int[]{0},
			new int[]{1},
		});
		
		int[] lab = new int[]{4, 5, 6, 7, 0, 1, 2, 3};
		int[] ptn = new int[]{0, 0, 1, 0, 1, 1, 1, 0};
		
		assertArrayEquals(
			new int[]{4, 5, 7, 6, 2, 1, 0, 3},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}

	@Test
	public void fullCanon4() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{3},
			new int[]{6},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		});
		
		int[] lab = new int[]{2, 4, 5, 7, 6, 3, 0, 1};
		int[] ptn = new int[]{0, 0, 1, 0, 0, 0, 1, 0};

		assertTrue(Arrays.deepEquals(
			TEST_GRAPH,
			nauty.computeCanonicalLabelling(g, lab, ptn).getCanonicalGraph().toAdjacencyLists()
		));
	}

	@Test
	public void fullCanon5() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{3},
			new int[]{6},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		});
		
		int[] lab = new int[]{2, 4, 5, 7, 3, 6, 0, 1};
		int[] ptn = new int[]{0, 0, 1, 0, 0, 0, 1, 0};

		assertTrue(Arrays.deepEquals(
			TEST_GRAPH,
			nauty.computeCanonicalLabelling(g, lab, ptn).getCanonicalGraph().toAdjacencyLists()
		));
	}

	@Test
	public void fullCanon6() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{6},
			new int[]{3},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		});
		
		int[] lab = new int[]{2, 4, 5, 7, 3, 6, 0, 1};
		int[] ptn = new int[]{0, 0, 1, 0, 0, 0, 1, 0};

		assertTrue(Arrays.deepEquals(
			TEST_GRAPH,
			nauty.computeCanonicalLabelling(g, lab, ptn).getCanonicalGraph().toAdjacencyLists()
		));
	}

	@Test
	public void fullCanon() throws InterruptedException{
		SparseGraph g2 = new SparseGraph(new int[][]{
			new int[]{3},
			new int[]{6},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		});
		
		int[] lab2 = new int[]{2, 4, 5, 7, 3, 6, 0, 1};
		int[] ptn2 = new int[]{0, 0, 1, 0, 0, 0, 1, 0};

		SparseGraph g3 = new SparseGraph(new int[][]{
			new int[]{6},
			new int[]{3},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		});
		
		int[] lab3 = new int[]{2, 4, 5, 7, 3, 6, 0, 1};
		int[] ptn3 = new int[]{0, 0, 1, 0, 0, 0, 1, 0};

		assertEquals(
			nauty.computeCanonicalLabelling(g3, lab3, ptn3).getCanonicalGraph(),
			nauty.computeCanonicalLabelling(g2, lab2, ptn2).getCanonicalGraph()
		);
	}
	
	@Test
	public void largerGraph() throws InterruptedException{
		SparseGraph g = new SparseGraph(new int[][]{
		    new int[]{1, 4, 15, 16, 17},
		    new int[]{0, 2, 18},
		    new int[]{1, 3, 19},
		    new int[]{2, 4, 20},
		    new int[]{0, 3, 5},
		    new int[]{4, 6, 21},
		    new int[]{5, 7, 22},
		    new int[]{6, 8, 23},
		    new int[]{7, 9},
		    new int[]{8, 10, 24},
		    new int[]{9, 11},
		    new int[]{10, 12, 25},
		    new int[]{11, 13},
		    new int[]{12, 14, 26},
		    new int[]{13, 15},
		    new int[]{0, 14, 27},
		    new int[]{17, 20, 31, 0, 32},
		    new int[]{16, 18, 1},
		    new int[]{17, 19, 2},
		    new int[]{18, 20, 3},
		    new int[]{16, 19, 21},
		    new int[]{20, 22, 5},
		    new int[]{21, 23, 6},
		    new int[]{22, 24, 7},
		    new int[]{23, 25, 9},
		    new int[]{24, 26, 11},
		    new int[]{25, 27, 13},
		    new int[]{26, 28, 15},
		    new int[]{27, 29},
		    new int[]{28, 30},
		    new int[]{29, 31},
		    new int[]{16, 30},
		    new int[]{33, 36, 47, 16},
		    new int[]{32, 34},
		    new int[]{33, 35},
		    new int[]{34, 36},
		    new int[]{32, 35, 37},
		    new int[]{36, 38},
		    new int[]{37, 39},
		    new int[]{38, 40},
		    new int[]{39, 41},
		    new int[]{40, 42},
		    new int[]{41, 43},
		    new int[]{42, 44},
		    new int[]{43, 45},
		    new int[]{44, 46},
		    new int[]{45, 47},
		    new int[]{32, 46}
		});
		
		int[] lab = new int[48];
		int[] ptn = new int[48];
		for(int i = 0; i < 48; i++){
			lab[i] = i;
			ptn[i] = 1;
		}
		
		assertArrayEquals(
			new int[]{41, 42, 40, 43, 44, 45, 46, 39, 38, 34, 10, 8, 12, 35, 37, 47, 33, 14, 28, 29, 30, 31, 9, 7, 23, 6, 11, 24, 22, 5, 21, 36, 3, 19, 2, 4, 25, 13, 26, 15, 27, 18, 1, 17, 32, 20, 0, 16},
			nauty.computeCanonicalLabelling(g, lab, ptn).getRelabelling()
		);
	}
}
