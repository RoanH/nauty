package dev.roanh.nauty;

import dev.roanh.nauty.Nauty.PruneRecord;
import dev.roanh.nauty.ds.NSet;
import dev.roanh.nauty.ptr.IntPtr;
import dev.roanh.nauty.struct.SparseGraph;

public class NaUtil{
	private int[] workperm = Nauty.dynAllStat();

	/**
	 * doref(g,lab,ptn,level,numcells,qinvar,invar,active,code,refproc,
	 *       invarproc,mininvarlev,maxinvarlev,invararg,digraph,m,n)
	 * is used to perform a refinement on the partition at the given level in
	 * (lab,ptn).  The number of cells is *numcells both for input and output.
	 * The input active is the active set for input to the refinement procedure
	 * (*refproc)(), which must have the argument list of refine().
	 * active may be arbitrarily changed.  invar is used for working storage.
	 * First, (*refproc)() is called.  Then, if invarproc!=NULL and
	 * |mininvarlev| <= level <= |maxinvarlev|, the routine (*invarproc)() is
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
			    //void (*refproc)(graph*,int*,int*,int,int*,int*,set*,int*,int,int), -> refine_sg
			    //void (*invarproc)(graph*,int*,int*,int,int,int,int*,int,boolean,int,int), -> adjacencies_sg
		    int mininvarlev,
		    int maxinvarlev,
//		    int invararg,
//		    boolean digraph,
//		    int m,
		    int n
	    ){
		
		int pw;
		int i, cell1, cell2, nc, /*tvpos,*/minlev, maxlev;
		long longcode;
		boolean same;

		workperm = Nauty.dynAlloc1(workperm, n);

//	    if ((tvpos = active.nextelement(-1)) < 0){ tvpos = 0;} //tvpos unused by sparse invarproc

//	    (*refproc)(g,lab,ptn,level,numcells,invar,active,code,M,n);
		nauSparse.refine_sg(g, lab, ptn, level, numcells, active, code, n);

		minlev = (mininvarlev < 0 ? -mininvarlev : mininvarlev);
		maxlev = (maxinvarlev < 0 ? -maxinvarlev : maxinvarlev);
		if(/*invarproc != NULL && */numcells.val < n && level >= minlev && level <= maxlev){
			//(*invarproc)(g,lab,ptn,level,*numcells,tvpos,invar,invararg,digraph,M,n);
			nauSparse.adjacencies_sg(g, lab, ptn, level, invar, n);
			active.clear();
			for(i = n; --i >= 0;){
				workperm[i] = invar[lab[i]];
			}
			nc = numcells.val;
			for(cell1 = 0; cell1 < n; cell1 = cell2 + 1){
				pw = workperm[cell1];
				same = true;
				for(cell2 = cell1; ptn[cell2] > level; ++cell2){
					if(workperm[cell2 + 1] != pw){
						same = false;
					}
				}

				if(same){
					continue;
				}

				SortTemplates.sortparallel(workperm, cell1, lab, cell1, cell2 - cell1 + 1);

				for(i = cell1 + 1; i <= cell2; ++i){
					if(workperm[i] != workperm[i - 1]){
						ptn[i - 1] = level;
						++numcells.val;
						active.addElement(i);
					}
				}
			}

			if(numcells.val > nc){
				qinvar.val = 2;
				longcode = code.val;
//	            (*refproc)(g,lab,ptn,level,numcells,invar,active,code,M,n);
				nauSparse.refine_sg(g, lab, ptn, level, numcells, active, code, n);
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
	 * maketargetcell(g,lab,ptn,level,tcell,tcellsize,&cellpos,
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
	 * GLOBALS ACCESSED: bit<r>
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
//			boolean digraph,
			int hint,
			NauSparse nauSparse,
		    	//int (*targetcell)(graph*,int*,int*,int,int,boolean,int,int,int), -> targetcell_sg
//		    int m,
		    int n
	    ){
		int i, j, k;

//	    i = (*targetcell)(g,lab,ptn,level,tc_level,digraph,hint,m,n);
		i = nauSparse.targetcell_sg(g, lab, ptn, level, tc_level, hint, n);
		for(j = i + 1; ptn[j] > level; ++j){
		}

		tcellsize.val = j - i + 1;

		tcell.clear();
		for(k = i; k <= j; ++k){
			tcell.addElement(lab[k]);
		}

		cellpos.val = i;
	}

	/**
	 * shortprune(set1,set2,m) ANDs the contents of set set2 into set set1.
	 * 
	 * GLOBALS ACCESSED: NONE
	 */
	static void shortprune(NSet set1, NSet set2/*, int m*/){
		set1.intersect(set2);
	}

	/**
	 * breakout(lab,ptn,level,tc,tv,active,m) operates on the partition at
	 * the specified level in the partition nest (lab,ptn).  It finds the
	 * element tv, which is in the cell C starting at index tc in lab (it had
	 * better be) and splits C in the two cells {tv} and C\{tv}, in that order.
	 * It also sets the set active to contain just the element tc.
	 * 
	 * GLOBALS ACCESSED: bit<r>
	 */
	public void breakout(int[] lab, int[] ptn, int level, int tc, int tv, NSet active/*, int m*/){
		int i, prev, next;

		active.clear();
		active.addElement(tc);

		i = tc;
		prev = tv;

		do{
			next = lab[i];
			lab[i++] = prev;
			prev = next;
		}while(prev != tv);

		ptn[tc] = level;
	}
	

	/**
	 * longprune(tcell,fix,bottom,top,m) removes zero or elements of the set
	 * tcell.  It is assumed that addresses bottom through top-1 contain
	 * contiguous pairs of sets (f1,m1),(f2,m2), ... .  tcell is intersected
	 * with each mi such that fi is a subset of fix.
	 * 
	 * GLOBALS ACCESSED: NONE
	 */
	//longprune(set *tcell, set *fix, set *bottom, set *top, int m) | workspace = bottom
	static void longprune(NSet tcell, NSet fix, PruneRecord[] workspace, int top){
		for(int i = 0; i < top; i++){
	        PruneRecord record = workspace[i];
			if(!fix.notSubSet(record.f())){
				tcell.intersect(record.m());
			}
		}
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
