/*
 * Copyright 2026 Roan Hofland (roan@roanh.dev) (this Java port)
 * Copyright 2004 Brendan McKay (the original C source adapted here)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.roanh.nauty.struct;

public class StatsBlk{
	/**
	 * Size of group is.
	 */
	public double grpsize1;
	/**
	 * grpsize1 * 10^grpsize2
	 */
	public int grpsize2;
	/**
	 * Number of orbits in group.
	 */
	public int numorbits;
	/**
	 * Number of generators found.
	 */
	public int numgenerators;
	/**
	 * Total number of nodes.
	 */
	public long numnodes;
	/**
	 * Number of leaves of no use.
	 */
	public long numbadleaves;
	/**
	 * Maximum depth of search.
	 */
	public int maxlevel;
	/**
	 * Total size of all target cells.
	 */
	public long tctotal;
	/**
	 * Number of updates of best label.
	 */
	public long canupdates;
	/**
	 * Number of applications of invarproc .
	 */
	public long invapplics;
	/**
	 * Number of successful uses of invarproc().
	 */
	public long invsuccesses;
	/**
	 * Least level where invarproc worked.
	 */
	public int invarsuclevel;
	
	@Override
	public String toString(){
		return "StatsBlk[grpsize=" + (grpsize1 * Math.powExact(10L, grpsize2)) + ", numorbits=" + numorbits + ", numgenerators=" + numgenerators + ", numnodes=" + numnodes + ", numbadleaves=" + numbadleaves + ", maxlevel=" + maxlevel + ", tctotal=" + tctotal + ", canupdates=" + canupdates + ", invapplics=" + invapplics + ", invsuccesses=" + invsuccesses + ", invarsuclevel=" + invarsuclevel + "]";
	}
}
