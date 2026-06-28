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
	public CanonicalResult computeCanonicalLabelling(SparseGraph graph, int[] labels, int[] ptn) throws InterruptedException{
		int[] orbits = new int[graph.nv];//TODO move
		SparseGraph canon = new SparseGraph();
		StatsBlk stats = new StatsBlk();
		NauSparse.sparsenauty(nauty, graph, labels, ptn, orbits, stats, canon);
		return new CanonicalResult(labels, canon, stats);
	}
	
	/**
	 * Constructs a new sparse graphs from the given vertex degrees
	 * and adjacencies. Derives the vertex offsets from the degree array.
	 * @param d The out degree of every vertex.
	 * @param e The target vertices for every vertex, flattened in vertex order.
	 * @return The constructed sparse graph.
	 */
	public static SparseGraph createGraph(int[] d, int[] e){
		return new SparseGraph(d, e);
	}
	
	/**
	 * Constructs a new sparse graph from the given adjacency lists.
	 * @param adj Adjacency lists with for each vertex the target vertices of edges
	 * @return The constructed sparse graph.
	 */
	public static SparseGraph createGraph(int[][] adj){
		return new SparseGraph(adj);
	}
	
	public void computeCanonicalLabellingTest(SparseGraph graph, int[] labels, int[] ptn) throws InterruptedException{
		
		
		
		int[] orbits = new int[graph.nv];
		
		
		SparseGraph canon = new SparseGraph();
		StatsBlk stats = new StatsBlk();
		NauSparse.sparsenauty(nauty, graph, labels, ptn, orbits, stats, canon);
		
		
		System.out.println(stats);
		System.out.println(Arrays.toString(labels));
		canon.sort();
		for(int i = 0; i < canon.nv; i++){
			System.out.print(i + " : " );
//			int[] adj = new int[canon.d[i]];
//			System.arraycopy(canon.e, canon.v[i], adj, 0, adj.length);
			//Arrays.sort(adj);
			for(int e = 0; e < canon.d[i]; e++){
				System.out.print(canon.e[canon.v[i] + e] + " ");
			}
			System.out.println();
		}
		
		
		//TODO cannonical is not sorted, could return relab directly
		
		
	}
	
	
	
	public static void main(String[] args) throws InterruptedException{
		NautyApi nauty = new NautyApi();
		


		
	}
	
	
	
	
	
}
