package dev.roanh.nauty.api;

import dev.roanh.nauty.NauSparse;
import dev.roanh.nauty.Nauty;
import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;

/**
 * More Java style interface to nauty. Note that instances of this class
 * are not thread safe.
 * @author Roan
 * @see <a href="https://pallini.di.uniroma1.it/Guide.html">nauty and Traces User’s Guide</a>
 */
public class NautyApi{
	/**
	 * Nauty instance used to calculate results.
	 */
	private final Nauty nauty = new Nauty();
	
	/**
	 * Computes the canonical form of the given graph with the given colouring.
	 * @param graph The directed input graph.
	 * @param labels The array containing all the vertex indices for the color information,
	 *        combined with the ptn array this describes the graph coloring. Note: this
	 *        array will be <b>modified</b> by nauty to contain the graph relabelling when this
	 *        method returns, see {@link CanonicalResult#getRelabelling()}.
	 * @param ptn Array indicating at which indices in the labels array a new block of vertices
	 *        with the same color starts. Start indices will have a value of 0 while
	 *        all other indices will have a value of 1. More information is available in the
	 *        nauty and Traces User’s Guide.
	 * @return The computed canonisation result.
	 * @throws InterruptedException When the current thread is interrupted while running nauty.
	 * @see SparseGraph
	 * @see CanonicalResult
	 * @see <a href="https://pallini.di.uniroma1.it/Guide.html">nauty and Traces User’s Guide</a>
	 */
	public CanonicalResult computeCanonicalLabelling(SparseGraph graph, int[] labels, int[] ptn) throws InterruptedException{
		SparseGraph canon = new SparseGraph();
		StatsBlk stats = new StatsBlk();
		NauSparse.sparsenauty(nauty, graph, labels, ptn, stats, canon);
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
}
