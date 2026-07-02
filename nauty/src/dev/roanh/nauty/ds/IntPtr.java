/*
 * Copyright 2026 Roan Hofland (roan@roanh.dev)
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

/**
 * Simple object wrapper around an integer to mimic C pointers.
 * @author Roan
 */
public class IntPtr{
	public int val;
	
	public IntPtr(){
		this(0);
	}
	
	public IntPtr(int val){
		this.val = val;
	}
	
	/**
	 * Performs a C style truthy check on this integer value.
	 * @return True if the current value is not 0.
	 */
	public boolean check(){
		return val != 0;
	}
	
	public IntPtr incNew(){
		return new IntPtr(val + 1);
	}
}
