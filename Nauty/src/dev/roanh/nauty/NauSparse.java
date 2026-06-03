package dev.roanh.nauty;

import dev.roanh.nauty.ds.MarkVal;
import dev.roanh.nauty.ds.NSet;
import dev.roanh.nauty.ptr.IntPtr;
import dev.roanh.nauty.struct.OptionBlk;
import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;

public class NauSparse{

	//just extracts v/d/e
	//#define SG_VDE(sgp,vv,dd,ee) do { vv = ((sparsegraph*)(sgp))->v; \
	//  dd = ((sparsegraph*)(sgp))->d; ee = ((sparsegraph*)(sgp))->e; } while(0)

	/*	DispatchVec dispatch_sparse =
		  {
		  isautom_sg,
		  testcanlab_sg,
		  updatecan_sg,
		  refine_sg,
		  refine_sg,
		  cheapautom_sg,
		   targetcell_sg,
		   nausparse_freedyn,
		   nausparse_check,
		   init_sg,NULL
		   };
		*/

	private final MarkVal vmark1 = new MarkVal();
	private final MarkVal vmark2 = new MarkVal();
	private int[] work1 = Nauty.dynAllStat();
	private int[] work2 = Nauty.dynAllStat();
	private int[] work3 = Nauty.dynAllStat();
	private int[] work4 = Nauty.dynAllStat();

	public static void sparsenauty(SparseGraph g, int[] lab, int[] ptn, int[] orbits, OptionBlk options, StatsBlk stats, SparseGraph h){

	}

	/**
	 * isautom_sg(g,perm,digraph,m,n) = TRUE iff perm is an automorphism of g
	 * (i.e., g^perm = g).  Symmetry is assumed unless digraph = TRUE.
	 */
	boolean is_autom_sg(SparseGraph g, int[] p, boolean digraph, int m, int n){
		int[] d = g.d;
		int[] e = g.e;
		int[] v = g.v;
		int i, pi, di;
		int vi, vpi, j;

		vmark1.prepare(n);

		for(i = 0; i < n; ++i){
			if(p[i] != i || digraph){
				pi = p[i];
				di = d[i];
				if(d[pi] != di){
					return false;
				}

				vi = v[i];
				vpi = v[pi];
				vmark1.reset();
				for(j = 0; j < di; ++j){
					vmark1.mark(p[e[vi + j]]);
				}
				for(j = 0; j < di; ++j){
					if(vmark1.isNotMarked(e[vpi + j])){
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * testcanlab_sg(g,canong,lab,samerows,m,n) compares g^lab to canong,
	 * using an ordering which is immaterial since it's only used here.  The
	 * value returned is -1,0,1 if g^lab <,=,> canong.  *samerows is set to
	 * the number of rows (0..n) of canong which are the same as those of g^lab.
	 */
	int testcanlab_sg(SparseGraph g, SparseGraph canong, int[] lab, IntPtr samerows, int m, int n){
		int[] d = g.d;
		int[] e = g.e;
		int[] cd = canong.d;
		int[] ce = canong.e;
		int i, k, di, dli;
		int j, vi, vli;
		int[] v = g.v;
		int[] cv = canong.v;
		int mina;

		work1 = Nauty.dynAlloc1(work1, n);
		final int[] INVLAB = work1;

		vmark1.prepare(n);

		for(i = 0; i < n; ++i){
			INVLAB[lab[i]] = i;
		}

		for(i = 0; i < n; ++i){
			/* compare g[lab[i]]^INVLAB to canong[i] */
			vi = cv[i];
			di = cd[i];
			vli = v[lab[i]];
			dli = d[lab[i]];

			if(di != dli){
				samerows.val = i;
				if(di < dli){
					return -1;
				}
				return 1;
			}

			vmark1.reset();
			mina = n;
			for(j = 0; j < di; ++j){
				vmark1.mark(ce[vi + j]);
			}

			for(j = 0; j < di; ++j){
				k = INVLAB[e[vli + j]];
				if(vmark1.isMarked(k)){
					vmark1.unmark(k);
				}else if(k < mina){
					mina = k;
				}
			}

			if(mina != n){
				samerows.val = i;
				for(j = 0; j < di; ++j){
					k = ce[vi + j];
					if(vmark1.isMarked(k) && k < mina){
						return -1;
					}
				}
				return 1;
			}
		}

		samerows.val = n;
		return 0;
	}

	/**
	 * updatecan_sg(g,canong,lab,samerows,m,n) sets canong = g^lab, assuming
	 * the first samerows vertices of canong are ok already.  Also assumes
	 * contiguity and ample space in canong.
	 */
	void updatecan_sg(SparseGraph g, SparseGraph canong, int[] lab, int samerows, int m, int n){
		int[] d = g.d;
		int[] e = g.e;
		int[] cd = canong.d;
		int[] ce = canong.e;
		int i, dli;
		int[] v = g.v;
		int[] cv = canong.v;
		int vli, j, k;

		work1 = Nauty.dynAlloc1(work1, n);
		final int[] INVLAB = work1;

		canong.nv = n;
		canong.nde = g.nde;

		for(i = 0; i < n; ++i){
			INVLAB[lab[i]] = i;
		}

		if(samerows == 0){
			k = 0;
		}else{
			k = cv[samerows - 1] + cd[samerows - 1];
		}

		for(i = samerows; i < n; ++i){
			cv[i] = k;
			cd[i] = dli = d[lab[i]];
			vli = v[lab[i]];
			for(j = 0; j < dli; ++j){
				ce[k++] = INVLAB[e[vli + j]];
			}
		}
	}

//	   init_sg TODO

	/**
	 * distvals(sparsegraph *sg, int v0, int *dist, int n) sets dist[i]
	 * to the distance from v0 to i, for each i, or to n if there is no such
	 * distance.  work4[] is used as a queue.
	 */
	void distvals(SparseGraph g, int v0, int[] dist, int n){
		int[] d = g.d;
		int[] e = g.e;
		int i, head, tail;
		int di, k;
		int[] v = g.v;
		int vi, j;

		work4 = Nauty.dynAlloc1(work4, n);
		final int[] QUEUE = work4;

		for(i = 0; i < n; ++i){
			dist[i] = n;
		}

		QUEUE[0] = v0;
		dist[v0] = 0;

		head = 0;
		tail = 1;
		while(tail < n && head < tail){
			i = QUEUE[head++];
			vi = v[i];
			di = d[i];
			for(j = 0; j < di; ++j){
				k = e[vi + j];
				if(dist[k] == n){
					dist[k] = dist[i] + 1;
					QUEUE[tail++] = k;
				}
			}
		}
	}

	/**
	 * refine_sg(g,lab,ptn,level,numcells,count,active,code,m,n) performs a
	 * refinement operation on the partition at the specified level of the
	 * partition nest (lab,ptn).  *numcells is assumed to contain the number of
	 * cells on input, and is updated.  The initial set of active cells (alpha
	 * in the paper) is specified in the set active.  Precisely, x is in active
	 * iff the cell starting at index x in lab is active.
	 * The resulting partition is equitable if active is correct (see the paper
	 * and the Guide).
	 * *code is set to a value which depends on the fine detail of the
	 * algorithm, but which is independent of the labelling of the graph.
	 * count is used for work space.
	 */
	void refine_sg(SparseGraph g, int[] lab, int[] ptn, int level, IntPtr numcells, IntPtr count, NSet active, IntPtr code, int m/*obsolete?*/, int n){
		int i, j, k, l, v1, v2, v3, isplit;
		int w1, w2, w3;
		long longcode;
		int[] d = g.d;
		int[] e = g.e;
		int size, bigsize, bigpos;
		int nactive, hitcells;
		int lj, di, splitv;
		boolean trivsplit;
		int[] v = g.v;
		int vi, ii;

		work1 = Nauty.dynAlloc1(work1, n);
		work2 = Nauty.dynAlloc1(work2, n);
		work3 = Nauty.dynAlloc1(work3, n);
		work4 = Nauty.dynAlloc1(work4, n);
		final int[] CELLSTART = work1;
		final int[] ACTIVE = work2;
		final int[] HITS = work3;
		final int[] HITCELL = work4;

		vmark1.prepare(n);
		vmark2.prepare(n);

		longcode = numcells.val;

		/* Set ACTIVE[0..nactive-1] = queue of active cell starts */

		nactive = 0;
		for(i = -1; (i = active.nextelement(i)) >= 0;){
			ACTIVE[nactive++] = i;
		}

		if(nactive == 0){
			code.val = cleanup(longcode);
			return;
		}

		/* Set CELLSTART[i] = starting point in lab[] of nontrivial cell
		containing i, or n if i is a singleton */

		for(i = 0; i < n;){
			/* Just here, i is a cell starting position */
			if(ptn[i] <= level){
				CELLSTART[lab[i]] = n;
				++i;
			}else{
				j = i;
				do{
					CELLSTART[lab[i]] = j;
				}while(ptn[i++] > level);
			}
		}

		if(level <= 2 && nactive == 1 && ptn[ACTIVE[0]] <= level && numcells.val <= n / 8){
			isplit = ACTIVE[--nactive];
			active.delElement(isplit);

			distvals(g, lab[isplit], HITS, n);

			for(v1 = 0; v1 < n;){
				if(ptn[v1] <= level){
					++v1;
					continue;
				}

				longcode = mash(longcode, v1);
				w1 = HITS[lab[v1]];

				v2 = v1 + 1;
				while(ptn[v2 - 1] > level && HITS[lab[v2]] == w1){
					++v2;
				}

				if(ptn[v2 - 1] <= level){
					v1 = v2;
					continue;
				}

				w2 = Nauty.NAUTY_INFINITY;
				v3 = j = v2;

				do{
					lj = lab[j];
					w3 = HITS[lj];
					if(w3 == w1){
						lab[j] = lab[v3];
						lab[v3] = lab[v2];
						lab[v2] = lj;
						++v2;
						++v3;
					}else if(w3 == w2){
						lab[j] = lab[v3];
						lab[v3] = lj;
						++v3;
					}else if(w3 < w1){
						lab[j] = lab[v2];
						lab[v2] = lab[v1];
						lab[v1] = lj;
						v3 = v2 + 1;
						v2 = v1 + 1;
						w2 = w1;
						w1 = w3;
					}else if(w3 < w2){
						lab[j] = lab[v2];
						lab[v2] = lj;
						v3 = v2 + 1;
						w2 = w3;
					}
				}while(ptn[j++] > level);

				longcode = mash(longcode, w2);
				longcode = mash(longcode, v2);
				if(j != v2) /* At least two fragments
				                 * v1..v2-1 = w1; v2..v3-1 = w2  */
				{
					if(v2 == v1 + 1){
						CELLSTART[lab[v1]] = n;
					}

					if(v3 == v2 + 1){
						CELLSTART[lab[v2]] = n;
					}else{
						for(k = v2; k < v3; ++k){
							CELLSTART[lab[k]] = v2;
						}
					}
					
					++numcells.val;
					ptn[v2 - 1] = level;

					if(j == v3){
						/* Two fragments only */
						if(v2 - v1 <= v3 - v2 && !active.isElement(v1)){
							active.addElement(v1);
							ACTIVE[nactive++] = v1;
						}else{
							active.addElement(v2);
							ACTIVE[nactive++] = v2;
						}
					}else{
						/* Extra fragments: v3..j-1 > w2 */
						SortTemplates.sortindirect(lab, v3, HITS, j - v3);
						ACTIVE[nactive++] = v2;
						active.addElement(v2);
						if(v2 - v1 >= v3 - v2){
							bigpos = -1;
							bigsize = v2 - v1;
						}else{
							bigpos = nactive - 1;
							bigsize = v3 - v2;
						}
						
						for(k = v3 - 1; k < j - 1;){
							ptn[k] = level;
							longcode = mash(longcode, k);
							++numcells.val;
							l = k + 1;
							active.addElement(l);
							ACTIVE[nactive++] = l;
							w3 = HITS[lab[l]];
							for(k = l; k < j - 1 && HITS[lab[k + 1]] == w3; ++k){
								CELLSTART[lab[k + 1]] = l;
							}
							
							size = k - l + 1;
							if(size == 1){
								CELLSTART[lab[l]] = n;
							}else{
								CELLSTART[lab[l]] = l;
								if(size > bigsize){
									bigsize = size;
									bigpos = nactive - 1;
								}
							}
						}

						if(bigpos >= 0 && !active.isElement(v1)){
							longcode = mash(longcode, bigpos);
							active.delElement(ACTIVE[bigpos]);
							active.addElement(v1);
							ACTIVE[bigpos] = v1;
						}
					}
				}
				v1 = j;
			}
		}

		/* Iterate until complete */
		while(nactive > 0 && numcells.val < n){
			for(i = 0; i < nactive && i < 10; ++i){
				if(ptn[ACTIVE[i]] <= level){
					break;
				}
			}

			if(i < nactive && i < 10){
				trivsplit = true;
				isplit = ACTIVE[i];
				ACTIVE[i] = ACTIVE[--nactive];
			}else{
				isplit = ACTIVE[--nactive];
				trivsplit = ptn[isplit] <= level;
			}

			active.delElement(isplit);
			longcode = mash(longcode, isplit);

			if(trivsplit){
				vmark1.reset();
				vmark2.reset();
				hitcells = 0;
				splitv = lab[isplit];
				vi = v[splitv];
				di = d[splitv];
				for(ii = 0; ii < di; ++ii){
					j = e[vi + ii];
					vmark2.mark(j);
					k = CELLSTART[j];
					if(k != n && vmark1.isNotMarked(k)){
						vmark1.mark(k);
						HITCELL[hitcells++] = k;
					}
				}

				if(hitcells > 1){
					SortTemplates.sortints(HITCELL, hitcells);
				}
				longcode = mash(longcode, hitcells);

				/* divide cells according to which vertices are hit */

				for(i = 0; i < hitcells; ++i){
					j = v1 = v2 = HITCELL[i];
					longcode = mash(longcode, v2);
					k = 0;
					do{
						lj = lab[j];
						if(vmark2.isMarked(lj)){
							HITS[k++] = lj;
						}else{
							lab[v2++] = lj;
						}
					}while(ptn[j++] > level);

					longcode = mash(longcode, k);
					v3 = v2;
					while(--k >= 0){
						j = HITS[k];
						CELLSTART[j] = v2;
						lab[v3++] = j;
					}

					if(v2 != v3 && v2 != v1){
						++numcells.val;
						if(v2 == v1 + 1){
							CELLSTART[lab[v1]] = n;
						}
						if(v3 == v2 + 1){
							CELLSTART[lab[v2]] = n;
						}
						ptn[v2 - 1] = level;
						longcode = mash(longcode, v2);
						if(v2 - v1 <= v3 - v2 && !active.isElement(v1)){
							active.addElement(v1);
							ACTIVE[nactive++] = v1;
						}else{
							active.addElement(v2);
							ACTIVE[nactive++] = v2;
						}
					}
				}
			}else /* non-trivial splitting */
			{
				/* isplit is the start of the splitting cell.
				Set HITS[i] = hits of i for i in non-trivial cells,
				HITCELL[0..hitcells-1] = starts of hit non-trivial cells */

				vmark1.reset();
				hitcells = 0;
				do{
					vi = v[lab[isplit]];
					di = d[lab[isplit]];
					for(ii = 0; ii < di; ++ii){
						j = e[vi + ii];
						k = CELLSTART[j];
						if(k != n){
							if(vmark1.isNotMarked(k)){
								vmark1.mark(k);
								HITCELL[hitcells++] = k;
								do{
									HITS[lab[k]] = 0;
								}while(ptn[k++] > level);
							}
							++HITS[j];
						}
					}
				}while(ptn[isplit++] > level);

				if(hitcells > 1){
					SortTemplates.sortints(HITCELL, hitcells);
				}
					
				/* divide cells according to hit counts */

				longcode = mash(longcode, hitcells);
				for(i = 0; i < hitcells; ++i){
					v1 = HITCELL[i];
					w1 = HITS[lab[v1]];
					longcode = mash(longcode, v1);

					v2 = v1 + 1;
					while(ptn[v2 - 1] > level && HITS[lab[v2]] == w1){
						++v2;
					}

					if(ptn[v2 - 1] <= level){
						continue;
					}
					w2 = Nauty.NAUTY_INFINITY;
					v3 = j = v2;

					do{
						lj = lab[j];
						w3 = HITS[lj];
						if(w3 == w1){
							lab[j] = lab[v3];
							lab[v3] = lab[v2];
							lab[v2] = lj;
							++v2;
							++v3;
						}else if(w3 == w2){
							lab[j] = lab[v3];
							lab[v3] = lj;
							++v3;
						}else if(w3 < w1){
							lab[j] = lab[v2];
							lab[v2] = lab[v1];
							lab[v1] = lj;
							v3 = v2 + 1;
							v2 = v1 + 1;
							w2 = w1;
							w1 = w3;
						}else if(w3 < w2){
							lab[j] = lab[v2];
							lab[v2] = lj;
							v3 = v2 + 1;
							w2 = w3;
						}
					}while(ptn[j++] > level);

					longcode = mash(longcode, w1);
					longcode = mash(longcode, v2);
					if(j != v2) /* At least two fragments
					             * v1..v2-1 = w1; v2..v3-1 = w2  */
					{
						if(v2 == v1 + 1){
							CELLSTART[lab[v1]] = n;
						}

						if(v3 == v2 + 1){
							CELLSTART[lab[v2]] = n;
						}else{
							for(k = v2; k < v3; ++k){
								CELLSTART[lab[k]] = v2;
							}
						}
						++numcells.val;
						ptn[v2 - 1] = level;

						if(j == v3){
							/* Two fragments only */
							if(v2 - v1 <= v3 - v2 && !active.isElement(v1)){
								active.addElement(v1);
								ACTIVE[nactive++] = v1;
							}else{
								active.addElement(v2);
								ACTIVE[nactive++] = v2;
							}
						}else{
							/* Extra fragments: v3..j-1 > w2 */
							longcode = mash(longcode, v3);
							SortTemplates.sortindirect(lab, v3, HITS, j - v3);
							ACTIVE[nactive++] = v2;
							active.addElement(v2);
							if(v2 - v1 >= v3 - v2){
								bigpos = -1;
								bigsize = v2 - v1;
							}else{
								bigpos = nactive - 1;
								bigsize = v3 - v2;
								longcode = mash(longcode, bigsize);
							}
							
							for(k = v3 - 1; k < j - 1;){
								ptn[k] = level;
								++numcells.val;
								l = k + 1;
								active.addElement(l);
								ACTIVE[nactive++] = l;
								w3 = HITS[lab[l]];
								longcode = mash(longcode, w3);
								for(k = l; k < j - 1 && HITS[lab[k + 1]] == w3; ++k){
									CELLSTART[lab[k + 1]] = l;
								}
								size = k - l + 1;
								if(size == 1){
									CELLSTART[lab[l]] = n;
								}else{
									CELLSTART[lab[l]] = l;
									if(size > bigsize){
										bigsize = size;
										bigpos = nactive - 1;
									}
								}
							}

							if(bigpos >= 0 && !active.isElement(v1)){
								active.delElement(ACTIVE[bigpos]);
								active.addElement(v1);
								ACTIVE[bigpos] = v1;
							}
						}
					}
				}
			}
		}

		longcode = mash(longcode, numcells.val);
		code.val = cleanup(longcode);
	}

//	  cheapautom_sg,
//	   targetcell_sg,
//	   nausparse_freedyn,
//	   nausparse_check,

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
