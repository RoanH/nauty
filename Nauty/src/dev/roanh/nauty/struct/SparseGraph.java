package dev.roanh.nauty.struct;

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
	public int vlen;
	/**
	 * Size of d.
	 */
	public int dlen;
	/**
	 * Size of e.
	 */
	public int elen;
	//note weights (w) are not implemented so ignore anything that uses it
}
