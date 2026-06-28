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

	@Test
	public void graph1() throws InterruptedException{
//		0 - 1 - 2
//		| \     |
//		3 - 4 - 5
		
		SparseGraph g = new SparseGraph(new int[][]{
			new int[]{1, 3, 4},
			new int[]{0, 2},
			new int[]{1, 5},
			new int[]{0, 4},
			new int[]{0, 3, 5},
			new int[]{2, 4}
		});
		
		int[] lab = {0,1,2,3,4,5};
		int[] ptn = {1,1,1,1,1,1};
		
		assertArrayEquals(
			new int[]{2, 1, 5, 3, 4, 0},
			nauty.computeCanonicalLabelling(g, lab, ptn).relabelling()
		);
	}
	
	
	
}
