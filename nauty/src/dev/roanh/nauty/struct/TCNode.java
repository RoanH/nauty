/*
 * Copyright 2026 Roan Hofland (this Java port)
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

import dev.roanh.nauty.ds.NSet;

public class TCNode{
	public TCNode next;
	public NSet tcellptr;
	
	public TCNode(){
	}
	
	public TCNode(int n){
		tcellptr = new NSet(n);
	}
}
