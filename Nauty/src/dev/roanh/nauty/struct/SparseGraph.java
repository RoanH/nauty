package dev.roanh.nauty.struct;

import java.util.Arrays;

import dev.roanh.nauty.Nauty;

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
	
	public void sort(){
		for(int i = 0; i < nv; i++){
			int from = v[i];
			Arrays.sort(e, from, from + d[i]);
		}
	}
	
	public static void sgAlloc(SparseGraph sg, int nlen, int ndelen){
		sg.v = Nauty.dynAlloc1(sg.v, nlen);
		sg.d = Nauty.dynAlloc1(sg.d, nlen);
		sg.e = Nauty.dynAlloc1(sg.e, ndelen);
	}
}
