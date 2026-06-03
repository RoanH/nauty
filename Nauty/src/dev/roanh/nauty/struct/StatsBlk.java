package dev.roanh.nauty.struct;

public class StatsBlk{
	/**
	 * Size of group is.
	 */
	double grpsize1;
	/**
	 * grpsize1 * 10^grpsize2
	 */
	int grpsize2;
	/**
	 * Number of orbits in group.
	 */
	int numorbits;
	/**
	 * Number of generators found.
	 */
	int numgenerators;
	/**
	 * If non-zero : an error code.
	 */
	int errstatus;
	/**
	 * Total number of nodes.
	 */
	long numnodes;
	/**
	 * Number of leaves of no use.
	 */
	long numbadleaves;
	/**
	 * Maximum depth of search.
	 */
	int maxlevel;
	/**
	 * Total size of all target cells.
	 */
	long tctotal;
	/**
	 * Number of updates of best label.
	 */
	long canupdates;
	/**
	 * Number of applications of invarproc .
	 */
	long invapplics;
	/**
	 * Number of successful uses of invarproc().
	 */
	long invsuccesses;
	/**
	 * Least level where invarproc worked.
	 */
	int invarsuclevel;
}
