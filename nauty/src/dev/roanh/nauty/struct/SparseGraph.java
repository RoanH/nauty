package dev.roanh.nauty.struct;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

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
	
	public SparseGraph(){
	}
	
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
	
	public void print(PrintStream out){
		for(int i = 0; i < nv; i++){
			out.print(i + " : " );
			for(int ei = 0; ei < d[i]; ei++){
				out.print(e[v[i] + ei] + " ");
			}
			out.println();
		}
	}
	
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
