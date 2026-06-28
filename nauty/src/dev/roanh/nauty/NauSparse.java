package dev.roanh.nauty;

import dev.roanh.nauty.ds.IntPtr;
import dev.roanh.nauty.ds.MarkVal;
import dev.roanh.nauty.ds.NSet;
import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;

@SuppressWarnings("javadoc")
public class NauSparse{
	private static final int[] FUZZ1 = new int[]{037541, 061532, 005257, 026416};
	private static final int[] FUZZ2 = new int[]{006532, 070236, 035523, 062437};
	private static final int DEFAULT_WORKSIZE = 500;
	private final MarkVal vmark1 = new MarkVal();
	private final MarkVal vmark2 = new MarkVal();
	private int[] work1;
	private int[] work2;
	private int[] work3;
	private int[] work4;
	private int n;

	public static void sparsenauty(Nauty nauty, SparseGraph g, int[] lab, int[] ptn, StatsBlk stats, SparseGraph h) throws InterruptedException{
		nauty.nauty(g, lab, ptn, stats, DEFAULT_WORKSIZE, g.nv, h);
	}
	
	public void prepare(int n){
		//n has to reflect the actual n for the input, but work arrays are allowed to be over sized
		this.n = n;
		if(work1 == null || work1.length < n){
			work1 = new int[n];
			work2 = new int[n];
			work3 = new int[n];
			work4 = new int[n];
			vmark1.prepare(n);
			vmark2.prepare(n);
		}
	}

	/**
	 * isautom_sg(g,perm,digraph,m,n) = TRUE iff perm is an automorphism of g
	 * (i.e., g^perm = g).  Symmetry is assumed unless digraph = TRUE.
	 */
	public boolean isautom_sg(SparseGraph g, int[] p, boolean digraph){
		int[] d = g.d;
		int[] e = g.e;
		int[] v = g.v;
		int i, pi, di;
		int vi, vpi, j;

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
	 * value returned is -1,0,1 if {@code g^lab <,=,> canong}.  *samerows is set to
	 * the number of rows (0..n) of canong which are the same as those of g^lab.
	 */
	public int testcanlab_sg(SparseGraph g, SparseGraph canong, int[] lab, IntPtr samerows){
		int[] d = g.d;
		int[] e = g.e;
		int[] cd = canong.d;
		int[] ce = canong.e;
		int i, k, di, dli;
		int j, vi, vli;
		int[] v = g.v;
		int[] cv = canong.v;
		int mina;

		final int[] INVLAB = work1;
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
	public void updatecan_sg(SparseGraph g, SparseGraph canong, int[] lab, int samerows){
		int[] d = g.d;
		int[] e = g.e;
		int[] cd = canong.d;
		int[] ce = canong.e;
		int i, dli;
		int[] v = g.v;
		int[] cv = canong.v;
		int vli, j, k;

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
	
	/**
	 * init_sg(graph *gin, graph **gout, graph *hin, graph **hout,
	 *         int *lab, int *ptn, set *active, optionblk *options,
	 *         int *status, int m, int n)
	 * Initialise routine for dispatch vector.  This one just makes sure
	 * that *hin has enough space and sets fields for n=0.
	 */
	public void init_sg(SparseGraph sg, SparseGraph sh){
		SparseGraph.sgAlloc(sh, sg.nv, sg.nde);
		sh.nv = sg.nv;
		sh.nde = sg.nde;
	}

	/**
	 * distvals(sparsegraph *sg, int v0, int *dist, int n) sets dist[i]
	 * to the distance from v0 to i, for each i, or to n if there is no such
	 * distance.  work4[] is used as a queue.
	 */
	private void distvals(SparseGraph g, int v0, int[] dist){
		int[] d = g.d;
		int[] e = g.e;
		int i, head, tail;
		int di, k;
		int[] v = g.v;
		int vi, j;

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
	public void refine_sg(SparseGraph g, int[] lab, int[] ptn, int level, IntPtr numcells, NSet active, IntPtr code){
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

		final int[] CELLSTART = work1;
		final int[] ACTIVE = work2;
		final int[] HITS = work3;
		final int[] HITCELL = work4;

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

			distvals(g, lab[isplit], HITS);

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
				if(j != v2){ /* At least two fragments
				                 * v1..v2-1 = w1; v2..v3-1 = w2  */
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
			}else{ /* non-trivial splitting */
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
					if(j != v2){ /* At least two fragments
					             * v1..v2-1 = w1; v2..v3-1 = w2  */
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
	
	/**
	 * cheapautom_sg(ptn,level,digraph,n) returns TRUE if the partition at the
	 * specified level in the partition nest (lab,ptn) {lab is not needed here}
	 * satisfies a simple sufficient condition for its cells to be the orbits of
	 * some subgroup of the automorphism group.  Otherwise it returns FALSE.
	 * It always returns FALSE if digraph!=FALSE.
	 * 
	 * nauty assumes that this function will always return TRUE for any
	 * partition finer than one for which it returns TRUE.
	 */
	public boolean cheapautom_sg(int[] ptn, int level, boolean digraph){
		if(digraph){//TODO technically always true (?)
			return false;
		}

		int k = n;
		int nnt = 0;
		for(int i = 0; i < n; ++i){
			--k;
			if(ptn[i] > level){
				++nnt;
				while(ptn[++i] > level){
				}
			}
		}

		return (k <= nnt + 1 || k <= 4);
	}
	
	/**
	 * bestcell_sg(g,lab,ptn,level,tc_level,m,n) returns the index in lab of
	 * the start of the "best non-singleton cell" for fixing.  If there is no
	 * non-singleton cell it returns n.
	 * This implementation finds the first cell which is non-trivially joined
	 * to the greatest number of other cells, assuming equitability.
	 * This is not good for digraphs!
	 */
	public int bestcell_sg(SparseGraph g, int[] lab, int[] ptn, int level){
		int nnt;
		int[] d = g.d;
		int[] e = g.e;
		int i, k, di;
		int maxcnt;
		int[] v = g.v;
		int vi, j;

		final int sizeOffset = n / 2;//alternative to work1b
		final int[] START = work1;
		final int[] NNTCELL = work2;
		final int[] HITS = work3;
		final int[] COUNT = work4;

		/* find non-singleton cells: put starts in START[0..nnt-1],
		  sizes in SIZE[0..nnt-1].
		  Also NNTCELL[i] = n if {i} is a singelton, else index of
		  nontriv cell containing i. */

		i = nnt = 0;

		while(i < n){
			if(ptn[i] > level){
				START[nnt] = i;
				j = i;
				do{
					NNTCELL[lab[j]] = nnt;
				}while(ptn[j++] > level);
				START[sizeOffset + nnt] = j - i;
				++nnt;
				i = j;
			}else{
				NNTCELL[lab[i]] = n;
				++i;
			}
		}

		if(nnt == 0){
			return n;
		}

		/* set COUNT[i] to # non-trivial neighbours of n.s. cell i */

		for(i = 0; i < nnt; ++i){
			HITS[i] = COUNT[i] = 0;
		}

		for(i = 0; i < nnt; ++i){
			vi = v[lab[START[i]]];
			di = d[lab[START[i]]];

			for(j = 0; j < di; ++j){
				k = NNTCELL[e[vi + j]];
				if(k != n){
					++HITS[k];
				}
			}
			for(j = 0; j < di; ++j){
				k = NNTCELL[e[vi + j]];
				if(k != n){
					if(HITS[k] > 0 && HITS[k] < START[sizeOffset + k]){
						++COUNT[i];
					}
					HITS[k] = 0;
				}
			}
		}

		/* find first greatest bucket value */

		j = 0;
		maxcnt = COUNT[0];
		for(i = 1; i < nnt; ++i){
			if(COUNT[i] > maxcnt){
				j = i;
				maxcnt = COUNT[i];
			}
		}

		return START[j];
	}
	
	/**
	 * targetcell_sg(g,lab,ptn,level,tc_level,digraph,hint,m,n) returns the
	 * index in lab of the next cell to split.
	 * hint is a suggestion for the answer, which is obeyed if it is valid.
	 * Otherwise we use bestcell() up to tc_level and the first non-trivial
	 * cell after that.
	 */
	public int targetcell_sg(SparseGraph g, int[] lab, int[] ptn, int level, int tc_level, int hint){
		int i;

		if(hint >= 0 && ptn[hint] > level && (hint == 0 || ptn[hint - 1] <= level)){
			return hint;
		}else if(level <= tc_level){
			return bestcell_sg(g, lab, ptn, level);
		}else{
			for(i = 0; i < n && ptn[i] <= level; ++i){
			}
			return (i == n ? 0 : i);
		}
	}
	
	/**
	 * adjacencies_sg() assigns to each vertex v a code depending on which cells
	 * it is joined to and from, and how many times.  It is intended to provide
	 * better partitioning that the normal refinement routine for digraphs.
	 * It will not help with undirected graphs in nauty at all.
	 */
	public void adjacencies_sg(SparseGraph g, int[] lab, int[] ptn, int level, int[] invar){
		int[] d = g.d;
		int[] e = g.e;
		int vwt, wwt;
		int di;
		int i;
		int[] v = g.v;

		vwt = 1;
		for(i = 0; i < n; ++i){
			work2[lab[i]] = vwt;
			if(ptn[i] <= level){
				++vwt;
			}
			invar[i] = 0;
		}

		for(i = 0; i < n; ++i){
			vwt = fuzz1(work2[i]);
			wwt = 0;
			di = d[i];
			for(int j = 0; j < di; ++j){
				wwt = accum(wwt, fuzz2(work2[e[v[i] + j]]));
				invar[e[v[i] + j]] = accum(invar[e[v[i] + j]], vwt);
			}
			invar[i] = accum(invar[i], wwt);
		}
	}
	
	private static final int accum(int x, int y){
		return (((x) + (y)) & 077777);
	}

	private static final int fuzz1(int x){
		return ((x) ^ FUZZ1[(x) & 3]);
	}
	
	private static final int fuzz2(int x){
		return ((x) ^ FUZZ2[(x) & 3]);
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
