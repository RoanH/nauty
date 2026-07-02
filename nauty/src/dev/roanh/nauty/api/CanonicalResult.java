/*
 * Copyright 2026 Roan Hofland
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
package dev.roanh.nauty.api;

import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;

/**
 * Result of a nauty canonisation call.
 * @author Roan
 */
public class CanonicalResult{
	/**
	 * Computed relabelling function.
	 */
	private final int[] relabelling;
	/**
	 * Computed canonical graph (unsorted adjacency lists)
	 */
	private final SparseGraph canonicalGraph;
	/**
	 * Calculation statistics.
	 */
	private final StatsBlk stats;
	/**
	 * Cached inverse relabelling function.
	 */
	private int[] invRelab = null;
	
	/**
	 * Constructs a new canonical result.
	 * @param relabelling The graph relabelling.
	 * @param canonGraph The canonical graph.
	 * @param stats The calculation statistics.
	 */
	public CanonicalResult(int[] relabelling, SparseGraph canonGraph, StatsBlk stats){
		this.relabelling = relabelling;
		this.canonicalGraph = canonGraph;
		this.stats = stats;
	}
	
	/**
	 * Gets some statistics about the calculation.
	 * @return The calculation statistics.
	 */
	public StatsBlk getStats(){
		return stats;
	}
	
	/**
	 * Gets the relabelled canonical graph calculated from the input.
	 * @return The canonical graph.
	 */
	public SparseGraph getCanonicalGraph(){
		canonicalGraph.sort();
		return canonicalGraph;
	}
	
	/**
	 * Relabels a single vertex.
	 * @param original The original graph vertex.
	 * @return The vertex in the canonical graph representing the original vertex.
	 */
	public int relabel(int original){
		return getInverseRelabelling()[original];
	}
	
	/**
	 * Gets the inverse relabelling function. That is, each index stores the new
	 * label for a vertex. For example, if at index 0 the value 4 is stored, then
	 * this means that vertex 0 in the input graph becomes vertex 4 in the canonical graph.
	 * @return The inverse labelling function.
	 */
	public int[] getInverseRelabelling(){
		if(invRelab == null){
			invRelab = new int[relabelling.length];
	 		for(int i = 0; i < relabelling.length; i++){
	 			invRelab[relabelling[i]] = i;
			}
		}
 		
 		return invRelab;
	}
	
	/**
	 * Gets the relabeling function that can be used to construct the canonical graph.
	 * The returned array has the same size as there were vertices in the graph. For
	 * each index the former index of that vertex is indicated. For example if at index
	 * 0 the value 4 is stored, then this means that in the input graph vertex 0 was
	 * labeled as vertex 4. The relabelling is also placed in the original input
 	 * labelling array by nauty. The relabelled canonical graph can also directly
	 * be retrieved with {@link #getCanonicalGraph()} and individual vertices can
	 * be relabelled with {@link #relabel(int)};
	 * @return The graph relabelling.
	 * @see #getCanonicalGraph()
	 * @see #relabel(int)
	 * @see #getInverseRelabelling()
	 */
	public int[] getRelabelling(){
		return relabelling;
	}
}
