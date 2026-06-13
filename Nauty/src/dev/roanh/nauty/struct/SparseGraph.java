package dev.roanh.nauty.struct;

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
	
	/**
	 * Size of v.
	 */
	public int vlen(){
		return v.length;
	}

	/**
	 * Size of d.
	 */
	public int dlen(){
		return d.length;
	}

	/**
	 * Size of e.
	 */
	public int elen(){
		return e.length;
	}
	
	//note weights (w) are not implemented so ignore anything that uses it
	
	public static void sgAlloc(SparseGraph sg, int nlen, int ndelen){
		sg.v = Nauty.dynAlloc1(sg.v, nlen);
		sg.d = Nauty.dynAlloc1(sg.d, nlen);
		sg.e = Nauty.dynAlloc1(sg.e, ndelen);
	}
}
