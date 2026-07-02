/*
 * Copyright 2026 Roan Hofland (roan@roanh.dev) (this Java port)
 * Copyright 1984 Brendan McKay (the original C source adapted here)
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
package dev.roanh.nauty;

import dev.roanh.nauty.ds.IntPtr;
import dev.roanh.nauty.ds.NSet;
import dev.roanh.nauty.struct.SparseGraph;

@SuppressWarnings("javadoc")
public class NaUtil{
	private int[] workperm;
	private int n;
	
	public void prepare(int n){
		//n has to reflect the actual n for the input, but work arrays are allowed to be over sized
		this.n = n;
		if(workperm == null || workperm.length < n){
			workperm = new int[n];
		}
	}
	
	/**
	 * orbits represents a partition of {0,1,...,n-1}, by orbits[i] = the
	 * smallest element in the same cell as i.  map[] is any array with values
	 * in {0,1,...,n-1}.  orbjoin(orbits,map,n) joins the cells of orbits[]
	 * together to the minimum extent such that for each i, i and map[i] are in
	 * the same cell.  The function value returned is the new number of cells.
	 * 
	 * GLOBALS ACCESSED: NONE
	 */
	public int orbjoin(int[] orbits, int[] map){
		for(int i = 0; i < n; ++i){
			if(map[i] != i){
				int j1 = orbits[i];
				while(orbits[j1] != j1){
					j1 = orbits[j1];
				}
				int j2 = orbits[map[i]];
				while(orbits[j2] != j2){
					j2 = orbits[j2];
				}

				if(j1 < j2){
					orbits[j2] = j1;
				}else if(j1 > j2){
					orbits[j1] = j2;
				}
			}
		}

		int j1 = 0;
		for(int i = 0; i < n; ++i){
			if((orbits[i] = orbits[orbits[i]]) == i){
				++j1;
			}
		}

		return j1;
	}
	
	/**
	 * doref(g,lab,ptn,level,numcells,qinvar,invar,active,code,refproc,
	 *       invarproc,mininvarlev,maxinvarlev,invararg,digraph,m,n)
	 * is used to perform a refinement on the partition at the given level in
	 * (lab,ptn).  The number of cells is *numcells both for input and output.
	 * The input active is the active set for input to the refinement procedure
	 * (*refproc)(), which must have the argument list of refine().
	 * active may be arbitrarily changed.  invar is used for working storage.
	 * First, (*refproc)() is called.  Then, if invarproc!=NULL and
	 * {@code |mininvarlev| <= level <= |maxinvarlev|}, the routine (*invarproc)() is
	 * used to compute a vertex-invariant which may refine the partition
	 * further.  If it does, (*refproc)() is called again, using an active set
	 * containing all but the first fragment of each old cell.  Unless g is a
	 * digraph, this guarantees that the final partition is equitable.  The
	 * arguments invararg and digraph are passed to (*invarproc)()
	 * uninterpretted.  The output argument code is a composite of the codes
	 * from all the calls to (*refproc)().  The output argument qinvar is set
	 * to 0 if (*invarproc)() is not applied, 1 if it is applied but fails to
	 * refine the partition, and 2 if it succeeds.
	 * See the file nautinv.c for a further discussion of vertex-invariants.
	 * Note that the dreadnaut I command generates a call to  this procedure
	 * with level = mininvarlevel = maxinvarlevel = 0.
	 */
	public void doref(
			SparseGraph g,
			int[] lab,
			int[] ptn,
			int level,
			IntPtr numcells,
			IntPtr qinvar,
			int[] invar,
			NSet active,
			IntPtr code,
			NauSparse nauSparse,
		    int mininvarlev,
		    int maxinvarlev
	    ){
		int cell2;

		nauSparse.refine_sg(g, lab, ptn, level, numcells, active, code);

		int minlev = (mininvarlev < 0 ? -mininvarlev : mininvarlev);
		int maxlev = (maxinvarlev < 0 ? -maxinvarlev : maxinvarlev);
		if(numcells.val < n && level >= minlev && level <= maxlev){
			nauSparse.adjacencies_sg(g, lab, ptn, level, invar);
			active.clear();
			for(int i = n; --i >= 0;){
				workperm[i] = invar[lab[i]];
			}
			int nc = numcells.val;
			for(int cell1 = 0; cell1 < n; cell1 = cell2 + 1){
				int pw = workperm[cell1];
				boolean same = true;
				for(cell2 = cell1; ptn[cell2] > level; ++cell2){
					if(workperm[cell2 + 1] != pw){
						same = false;
					}
				}

				if(same){
					continue;
				}

				SortTemplates.sortparallel(workperm, cell1, lab, cell1, cell2 - cell1 + 1);

				for(int i = cell1 + 1; i <= cell2; ++i){
					if(workperm[i] != workperm[i - 1]){
						ptn[i - 1] = level;
						++numcells.val;
						active.addElement(i);
					}
				}
			}

			if(numcells.val > nc){
				qinvar.val = 2;
				long longcode = code.val;
				nauSparse.refine_sg(g, lab, ptn, level, numcells, active, code);
				longcode = mash(longcode, code.val);
				code.val = cleanup(longcode);
			}else{
				qinvar.val = 1;
			}
		}else{
			qinvar.val = 0;
		}
	}
	
	/**
	 * maketargetcell(g,lab,ptn,level,tcell,tcellsize,cellpos,
	 *                tc_level,digraph,hint,targetcell,m,n)
	 * calls targetcell() to determine the target cell at the specified level
	 * in the partition nest (lab,ptn).  It must be a nontrivial cell (if not,
	 * the first cell.  The intention of hint is that, if hint >= 0 and there
	 * is a suitable non-trivial cell starting at position hint in lab,
	 * that cell is chosen.
	 * tc_level and digraph are input options.
	 * When a cell is chosen, tcell is set to its contents, *tcellsize to its
	 * size, and cellpos to its starting position in lab.
	 * 
	 * GLOBALS ACCESSED: {@code bit<r>}
	 */
	public void maketargetcell(
			SparseGraph g,
			int[] lab,
			int[] ptn,
			int level,
			NSet tcell,
			IntPtr tcellsize,
			IntPtr cellpos,
			int tc_level,
			int hint,
			NauSparse nauSparse
	    ){
		int j;

		int i = nauSparse.targetcell_sg(g, lab, ptn, level, tc_level, hint);
		for(j = i + 1; ptn[j] > level; ++j){
		}

		tcellsize.val = j - i + 1;

		tcell.clear();
		for(int k = i; k <= j; ++k){
			tcell.addElement(lab[k]);
		}

		cellpos.val = i;
	}

	/**
	 * breakout(lab,ptn,level,tc,tv,active,m) operates on the partition at
	 * the specified level in the partition nest (lab,ptn).  It finds the
	 * element tv, which is in the cell C starting at index tc in lab (it had
	 * better be) and splits C in the two cells {tv} and C\{tv}, in that order.
	 * It also sets the set active to contain just the element tc.
	 * 
	 * GLOBALS ACCESSED: {@code bit<r>}
	 */
	public void breakout(int[] lab, int[] ptn, int level, int tc, int tv, NSet active){
		active.clear();
		active.addElement(tc);

		int i = tc;
		int prev = tv;

		do{
			int next = lab[i];
			lab[i++] = prev;
			prev = next;
		}while(prev != tv);

		ptn[tc] = level;
	}
	
	/**
	 * expression whose long value depends only on long l and int/long i.
	 * Anything goes, preferably non-commutative.
	 */
	private static final long mash(long l, int i){
		return ((((l) ^ 065435) + (i)) & 077777);
	}

	/**
	 * expression whose value depends on long l and is less than 077777
	 * when converted to int then short.  Anything goes.
	 */
	private static final int cleanup(long l){
		return ((int)((l) % 077777));
	}
}
