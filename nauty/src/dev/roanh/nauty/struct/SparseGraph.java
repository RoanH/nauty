package dev.roanh.nauty.struct;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * Main graph data structure used in sparse nauty.
 * @author Roan
 */
public class SparseGraph{
	/**
	 * Number of directed edges (loops contribute only 1).
	 */
	public int nde;
	/**
	 * Array of indexes into e[*].
	 */
	public int[] v;
	/**
	 * Number of vertices.
	 */
	public int nv;
	/**
	 * Array with out-degree of each vertex.
	 */
	public int[] d;
	/**
	 * Array to hold lists of neighbours.
	 */
	public int[] e;
	
	/**
	 * Constructs a new uninitialised sparse graph.
	 * @see #sgAlloc(SparseGraph, int, int)
	 */
	public SparseGraph(){
	}
	
	/**
	 * Constructs a new empty sparse graph with room
	 * for the given number of vertices and directed edges.
	 * @param vertices The number of vertices.
	 * @param edges The number of edges.
	 */
	public SparseGraph(int vertices, int edges){
		sgAlloc(this, vertices, edges);
		nv = vertices;
		nde = edges;
	}
	
	/**
	 * Constructs a new sparse graphs from the given vertex degrees
	 * and adjacencies. Derives the vertex offsets from the degree array.
	 * @param d The out degree of every vertex.
	 * @param e The target vertices for every vertex, flattened in vertex order.
	 */
	public SparseGraph(int[] d, int[] e){
		this.d = d;
		this.e = e;
		nv = d.length;
		nde = e.length;
		v = new int[d.length];
		for(int i = 1; i < v.length; i++){
			v[i] = v[i - 1] + d[i - 1];
		}
	}
	
	/**
	 * Constructs a sparse graph from the given array.
	 * @param v Array of indexes into e[*] where each vertex starts.
	 * @param d Array with out-degree of each vertex.
	 * @param e Array with lists of neighbours for each vertex.
	 */
	public SparseGraph(int[] v, int[] d, int[] e){
		this.v = v;
		this.d = d;
		this.e = e;
		nv = v.length;
		nde = e.length;
	}
	
	/**
	 * Constructs a new sparse graph from the given adjacency lists.
	 * @param adj Adjacency lists with for each vertex the target vertices of edges
	 */
	public SparseGraph(int[][] adj){
		nv = adj.length;
		v = new int[nv];
		d = new int[nv];
		
		for(int[] row : adj){
			nde += row.length;
		}
		
		e = new int[nde];
		
		int off = 0;
		for(int i = 0; i < nv; i++){
			int[] row = adj[i];
			System.arraycopy(row, 0, e, off, row.length);
			v[i] = off;
			d[i] = row.length;
			off += row.length;
		}
	}
	
	/**
	 * Sorts vertex adjacency lists.
	 */
	public void sort(){
		for(int i = 0; i < nv; i++){
			int from = v[i];
			Arrays.sort(e, from, from + d[i]);
		}
	}
	
	/**
	 * Prints this graph to the given stream.
	 * @param out The stream to write to.
	 */
	public void print(PrintStream out){
		for(int i = 0; i < nv; i++){
			out.print(i + " : " );
			for(int ei = 0; ei < d[i]; ei++){
				out.print(e[v[i] + ei] + " ");
			}
			out.println();
		}
	}
	
	/**
	 * Converts this graph to a 2D array adjacency list representation.
	 * Each index contains an array the neighbours of that vertex.
	 * @return The 2D adjacency list representation of this graph.
	 */
	public int[][] toAdjacencyLists(){
		int[][] g = new int[nv][];
		for(int i = 0; i < nv; i++){
			int len = d[i];
			g[i] = new int[len];
			System.arraycopy(e, v[i], g[i], 0, len);
		}
		
		return g;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof SparseGraph g){
			sort();
			g.sort();
			
			return nv == g.nv
				&& nde == g.nde
				&& Arrays.equals(v, 0, nv, g.v, 0, nv)
				&& Arrays.equals(d, 0, nv, g.d, 0, nv)
				&& Arrays.equals(e, 0, nde, g.e, 0, nde);
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		int result = Objects.hash(nv, nde);
		result = 31 * result + Arrays.hashCode(v);
		result = 31 * result + Arrays.hashCode(d);
		return 31 * result + Arrays.hashCode(e);
	}
	
	/**
	 * Allocates array with sufficient space in the given graph if not yet available.
	 * @param sg The graph to allocate space in.
	 * @param nlen The number of vertices.
	 * @param ndelen The number of directed edges.
	 */
	public static void sgAlloc(SparseGraph sg, int nlen, int ndelen){
		if(sg.v == null || sg.v.length < nlen){
			sg.v = new int[nlen];
		}
		
		if(sg.d == null || sg.d.length < nlen){
			sg.d = new int[nlen];
		}
		
		if(sg.e == null || sg.e.length < ndelen){
			sg.e = new int[ndelen];
		}
	}
}
