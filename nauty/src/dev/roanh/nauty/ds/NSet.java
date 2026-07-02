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
package dev.roanh.nauty.ds;

import java.util.BitSet;

//nauty set
//TODO if this becomes a hotspot a local implementation of bitset might be more performant
public class NSet{
	private final BitSet set;
	
	public NSet(int n){
		set = new BitSet(n);
	}
	
	public int nextelement(int pos){
		//nauty spec: greater than pos
		//java spec: on or after
		return set.nextSetBit(pos + 1);
	}

	public void addElement(int pos){
		set.set(pos);
	}

	public void delElement(int pos){
		set.clear(pos);
	}

	public boolean isElement(int pos){
		return set.get(pos);
	}
	
	public void intersect(NSet other){
		set.and(other.set);
	}
	
	public boolean notSubSet(NSet other){
		//the clone might be a performance issue
		BitSet copy = (BitSet)set.clone();
		copy.andNot(other.set);
		return copy.isEmpty();
	}
	
	//EMPTYSET
	public void clear(){
		set.clear();
	}
}
