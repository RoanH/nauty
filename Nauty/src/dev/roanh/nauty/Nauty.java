package dev.roanh.nauty;

import dev.roanh.nauty.ds.IntPtr;
import dev.roanh.nauty.ds.NSet;
import dev.roanh.nauty.ds.Workspace;
import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;
import dev.roanh.nauty.struct.TCNode;

public class Nauty{
	/**
	 * Max graph size is 2 billion.
	 */
	public static final int NAUTY_INFINITY = 2000000002;
	/**
	 * We only support sparse nauty the dispatch vector and invar procedure are hard wired.
	 */
	private final NauSparse nauSparse = new NauSparse();
	/**
	 * Utilities with matching worksize.
	 */
	private final NaUtil naUtil = new NaUtil();
	
	//TODO fix the copies below to final constants equal to the sparse options and clear some branches
	/* copies of some of the options: */
	/* make canong and canonlab? */
	final boolean getcanon = true;
	/* multiple edges or loops? */
	final boolean digraph = true;
	/* max level for smart target cell choosing */
	private int tc_level;
	/* min level for invariant computation */
	private int mininvarlevel = 0;
	/* max level for invariant computation */
	private int maxinvarlevel = 999;
	
	/* local versions of some of the arguments: */
	int n;//input n
	SparseGraph g,canong;
	int[] orbits;
	StatsBlk stats;
	
	/* temporary versions of some stats: */
	long invapplics,invsuccesses;
	int invarsuclevel;
	
 /* working variables: <the "bsf leaf" is the leaf which is best guess so
                            far at the canonical leaf>  */
int gca_first, /* level of greatest common ancestor of
                                  current node and first leaf */
    gca_canon,     /* ditto for current node and bsf leaf */
    noncheaplevel, /* level of greatest ancestor for which cheapautom==FALSE */
    allsamelevel,  /* level of least ancestor of first leaf for
                      which all descendant leaves are known to be
                      equivalent */
    eqlev_first,   /* level to which codes for this node match those
                      for first leaf */
    eqlev_canon,   /* level to which codes for this node match those
                      for the bsf leaf. */
    comp_canon,    /* -1,0,1 according as code at eqlev_canon+1 is
                       <,==,> that for bsf leaf.  Also used for
                       similar purpose during leaf processing */
    samerows,      /* number of rows of canong which are correct for
                      the bsf leaf  BDM:correct description? */
    canonlevel,    /* level of bsf leaf */
    stabvertex,    /* point fixed in ancestor of first leaf at level
                      gca_canon */
    cosetindex;    /* the point being fixed at level gca_first */

	boolean needshortprune;  /* used to flag calls to shortprune */
	
	private NSet fixedpts;
	private NSet active;
	private int[] workperm;
	private int[] firstlab;
	private int[] canonlab;
	private int[] firsttc;
	private int[] firstcode;
	private int[] canoncode;
	
	private Workspace workspace;
	
	/* In the dynamically allocated case (MAXN=0), each level of recursion
	   needs one set (tcell) to represent the target cell.  This is
	   implemented by using a linked list of tcnode anchored at the root
	   of the search tree.  Each node points to its child (if any) and to
	   the dynamically allocated tcell.  Apart from the first node of
	   the list, each node always has a tcell good for m up to alloc_m.
	   tcnodes and tcells are kept between calls to nauty, except that
	   they are freed and reallocated if m gets bigger than alloc_m.  */
	private TCNode tcnode0 = new TCNode();
	private int alloc_n = -1;// 'n' allocated capacity
	
	public void nauty(SparseGraph g_arg, int[] lab, int[] ptn, int[] orbits_arg, StatsBlk stats_arg, int worksize, int n_arg, SparseGraph canong_arg) throws InterruptedException{
		final IntPtr numcells = new IntPtr();

		/* check for excessive sizes: */

		if(n_arg > NAUTY_INFINITY - 2){
			throw new IllegalArgumentException("nauty: need n <= %d, but n=%d".formatted(NAUTY_INFINITY - 2, n_arg));
		}

		if(n_arg == 0) /* Special code for zero-sized graph */
		{
			stats_arg.grpsize1 = 1.0;
			stats_arg.grpsize2 = 0;
			stats_arg.numorbits = 0;
			stats_arg.numgenerators = 0;
			stats_arg.numnodes = 1;
			stats_arg.numbadleaves = 0;
			stats_arg.maxlevel = 1;
			stats_arg.tctotal = 0;
			stats_arg.canupdates = getcanon ? 1 : 0;
			stats_arg.invapplics = 0;
			stats_arg.invsuccesses = 0;
			stats_arg.invarsuclevel = 0;

			nauSparse.init_sg(g_arg, canong_arg);

			return;
		}

		/* take copies of some args, and options: */
		prepare(n_arg);

		/* OLD g = g_arg; */
		orbits = orbits_arg;
		stats = stats_arg;

		tc_level = digraph ? 0 : 100;
		mininvarlevel = 0;
		maxinvarlevel = 999;

		/* initialize everything: */

		ptn[n - 1] = 0;
		numcells.val = 0;
		for(int i = 0; i < n; ++i){
			if(ptn[i] != 0){
				ptn[i] = NAUTY_INFINITY;
			}else{
				++numcells.val;
			}
		}

		active.clear();
		for(int i = 0; i < n; ++i){
			active.addElement(i);
			while(ptn[i] != 0){
				++i;
			}
		}

		g = null;
		canong = null;

		nauSparse.init_sg(g_arg, canong_arg);

		g = g_arg;
		canong = canong_arg;

		for(int i = 0; i < n; ++i){
			orbits[i] = i;
		}
		stats.grpsize1 = 1.0;
		stats.grpsize2 = 0;
		stats.numgenerators = 0;
		stats.numnodes = 0;
		stats.numbadleaves = 0;
		stats.tctotal = 0;
		stats.canupdates = 0;
		stats.numorbits = n;
		fixedpts.clear();
		noncheaplevel = 1;
		eqlev_canon = -1; /* needed even if !getcanon */

		if(workspace == null || !workspace.canFit(worksize, n)){
			workspace = new Workspace(worksize, n);
		}
		workspace.reset();

		/* here goes: */
		needshortprune = false;
		invarsuclevel = NAUTY_INFINITY;
		invapplics = invsuccesses = 0;

		firstpathnode0(lab, ptn, 1, numcells, tcnode0);

		if(getcanon){
			nauSparse.updatecan_sg(g, canong, canonlab, samerows);
			for(int i = 0; i < n; ++i){
				lab[i] = canonlab[i];
			}
		}
		stats.invarsuclevel = (invarsuclevel == NAUTY_INFINITY ? 0 : invarsuclevel);
		stats.invapplics = invapplics;
		stats.invsuccesses = invsuccesses;
	}
	
	private void prepare(int n){
		this.n = n;
		if(n > alloc_n){
			nauSparse.prepare(n);
			naUtil.prepare(n);
			fixedpts = new NSet(n);
			active = new NSet(n);
			workperm = new int[n];
			firstlab = new int[n];
			canonlab = new int[n];
			firstcode = new int[n + 2];
			canoncode = new int[n + 2];
			firsttc = new int[n + 2];
			tcnode0.next = null;
			alloc_n = n;
		}
	}
	
	/**
	 * firstpathnode(lab,ptn,level,numcells) produces a node on the leftmost
	 * path down the tree.  The parameters describe the level and the current
	 * colour partition.  The set of active cells is taken from the global set
	 * 'active'.  If the refined partition is not discrete, the leftmost child
	 * is produced by calling firstpathnode, and the other children by calling
	 * othernode.
	 * For MAXN=0 there is an extra parameter: the address of the parent tcell
	 * structure.
	 * The value returned is the level to return to.
	 * 
	 * FUNCTIONS CALLED: (*usernodeproc)(),doref(),cheapautom(),
	 *                   firstterminal(),nextelement(),breakout(),
	 *                   firstpathnode(),othernode(),recover(),writestats(),
	 *                   (*userlevelproc)(),(*tcellproc)(),shortprune()
	 */
	private int firstpathnode0(int[] lab, int[] ptn, int level, IntPtr numcells, TCNode tcnode_parent) throws InterruptedException{
		int tv;
		int tv1, index, rtnlevel;
		IntPtr tcellsize = new IntPtr();
		IntPtr tc = new IntPtr();
		IntPtr qinvar = new IntPtr();
		IntPtr refcode = new IntPtr();
		NSet tcell;
		TCNode tcnode_this;

		tcnode_this = tcnode_parent.next;
		if(tcnode_this == null){
			tcnode_this = new TCNode(alloc_n);
			tcnode_parent.next = tcnode_this;
			tcnode_this.next = null;
		}
		tcell = tcnode_this.tcellptr;

		++stats.numnodes;

		/* refine partition : */
		naUtil.doref(g, lab, ptn, level, numcells, qinvar, workperm, active, refcode, nauSparse, mininvarlevel, maxinvarlevel);
		firstcode[level] = (short)refcode.val;
		if(qinvar.val > 0){
			++invapplics;
			if(qinvar.val == 2){
				++invsuccesses;
				if(mininvarlevel < 0){
					mininvarlevel = level;
				}
				if(maxinvarlevel < 0){
					maxinvarlevel = level;
				}
				if(level < invarsuclevel){
					invarsuclevel = level;
				}
			}
		}

		tc.val = -1;
		if(numcells.val != n){
			/* locate new target cell, setting tc to its position in lab, tcell
			              to its contents, and tcellsize to its size: */
			naUtil.maketargetcell(g, lab, ptn, level, tcell, tcellsize, tc, tc_level, -1, nauSparse);
			stats.tctotal += tcellsize.val;
		}
		firsttc[level] = tc.val;

		if(numcells.val == n){/* found first leaf? */
			firstterminal(lab, level);
			return level - 1;
		}

		if(Thread.interrupted()){
			throw new InterruptedException();
		}

		if(noncheaplevel >= level && !nauSparse.cheapautom_sg(ptn, level, digraph)){
			noncheaplevel = level + 1;
		}

		/* use the elements of the target cell to produce the children: */
		index = 0;
		for(tv1 = tv = tcell.nextelement(-1); tv >= 0; tv = tcell.nextelement(tv)){
			if(orbits[tv] == tv){ /* ie, not equiv to previous child */
				naUtil.breakout(lab, ptn, level + 1, tc.val, tv, active);
				fixedpts.addElement(tv);
				cosetindex = tv;
				if(tv == tv1){
					rtnlevel = firstpathnode0(lab, ptn, level + 1, numcells.incNew(), tcnode_this);
					gca_first = level;
					stabvertex = tv1;
				}else{
					rtnlevel = othernode0(lab, ptn, level + 1, numcells.incNew(), tcnode_this);
				}
				fixedpts.delElement(tv);
				if(rtnlevel < level){
					return rtnlevel;
				}
				if(needshortprune){
					needshortprune = false;
					workspace.shortprune(tcell);
				}
				recover(ptn, level);
			}
			if(orbits[tv] == tv1){ /* ie, in same orbit as tv1 */
				++index;
			}
		}
		multiply(stats, index);

		if(tcellsize.val == index && allsamelevel == level + 1){
			--allsamelevel;
		}

		return level - 1;
	}
	
	/**
	 * othernode(lab,ptn,level,numcells) produces a node other than an ancestor
	 * of the first leaf.  The parameters describe the level and the colour
	 * partition.  The list of active cells is found in the global set 'active'.
	 * The value returned is the level to return to.
	 * 
	 * FUNCTIONS CALLED: (*usernodeproc)(),doref(),refine(),recover(),
	 *                    processnode(),cheapautom(),(*tcellproc)(),shortprune(),
	 *                   nextelement(),breakout(),othernode(),longprune()
	 */
	private int othernode0(int[] lab, int[] ptn, int level, IntPtr numcells, TCNode tcnode_parent) throws InterruptedException{
		int tv;
		int tv1, rtnlevel;
		IntPtr tcellsize = new IntPtr();
		IntPtr tc = new IntPtr();
		IntPtr refcode = new IntPtr();
		IntPtr qinvar = new IntPtr();
		short code;
		NSet tcell;
		TCNode tcnode_this;

		tcnode_this = tcnode_parent.next;
		if(tcnode_this == null){
			tcnode_this = new TCNode(alloc_n);
			tcnode_parent.next = tcnode_this;
			tcnode_this.next = null;
		}
		tcell = tcnode_this.tcellptr;

		if(Thread.interrupted()){
			throw new InterruptedException();
		}

		++stats.numnodes;

		/* refine partition : */
		naUtil.doref(g, lab, ptn, level, numcells, qinvar, workperm, active, refcode, nauSparse, mininvarlevel, maxinvarlevel);
		code = (short)refcode.val;
		if(qinvar.val > 0){
			++invapplics;
			if(qinvar.val == 2){
				++invsuccesses;
				if(level < invarsuclevel){
					invarsuclevel = level;
				}
			}
		}

		if(eqlev_first == level - 1 && code == firstcode[level]){
			eqlev_first = level;
		}
		if(getcanon){
			if(eqlev_canon == level - 1){
				if(code < canoncode[level]){
					comp_canon = -1;
				}else if(code > canoncode[level]){
					comp_canon = 1;
				}else{
					comp_canon = 0;
					eqlev_canon = level;
				}
			}
			if(comp_canon > 0){
				canoncode[level] = code;
			}
		}

		tc.val = -1;
		/* If children will be required, find new target cell and set tc to its
		  position in lab, tcell to its contents, and tcellsize to its size: */

		if(numcells.val < n && (eqlev_first == level || (getcanon && comp_canon >= 0))){
			if(!getcanon || comp_canon < 0){
				naUtil.maketargetcell(g, lab, ptn, level, tcell, tcellsize, tc, tc_level, firsttc[level], nauSparse);
				if(tc.val != firsttc[level]){
					eqlev_first = level - 1;
				}
			}else{
				naUtil.maketargetcell(g, lab, ptn, level, tcell, tcellsize, tc, tc_level, -1, nauSparse);
			}
			stats.tctotal += tcellsize.val;
		}

		/* call processnode to classify the type of this node: */

		rtnlevel = processnode(lab, ptn, level, numcells.val);
		if(rtnlevel < level){ /* keep returning if necessary */
			return rtnlevel;
		}
		if(needshortprune){
			needshortprune = false;
			workspace.shortprune(tcell);
		}

		if(!nauSparse.cheapautom_sg(ptn, level, digraph)){
			noncheaplevel = level + 1;
		}

		/* use the elements of the target cell to produce the children: */
		for(tv1 = tv = tcell.nextelement(-1); tv >= 0; tv = tcell.nextelement(tv)){
			naUtil.breakout(lab, ptn, level + 1, tc.val, tv, active);
			fixedpts.addElement(tv);
			rtnlevel = othernode0(lab, ptn, level + 1, numcells.incNew(), tcnode_this);
			fixedpts.delElement(tv);

			if(rtnlevel < level){
				return rtnlevel;
			}
			/* use stored automorphism data to prune target cell: */
			if(needshortprune){
				needshortprune = false;
				workspace.shortprune(tcell);
			}
			if(tv == tv1){
				workspace.longprune(tcell, fixedpts);
			}

			recover(ptn, level);
		}

		return level - 1;
	}
	
	/**
	 * Process the first leaf of the tree.
	 * 
	 * FUNCTIONS CALLED: NONE
	 */
	private void firstterminal(int[] lab, int level){
		stats.maxlevel = level;
		gca_first = allsamelevel = eqlev_first = level;
		firstcode[level + 1] = 077777;
		firsttc[level + 1] = -1;

		for(int i = 0; i < n; ++i){
			firstlab[i] = lab[i];
		}

		if(getcanon){
			canonlevel = eqlev_canon = gca_canon = level;
			comp_canon = 0;
			samerows = 0;
			for(int i = 0; i < n; ++i){
				canonlab[i] = lab[i];
			}
			for(int i = 0; i <= level; ++i){
				canoncode[i] = firstcode[i];
			}
			canoncode[level + 1] = 077777;
			stats.canupdates = 1;
		}
	}
	
	/**
	 * Process a node other than the first leaf or its ancestors.  It is first
	 * classified into one of five types and then action is taken appropriate
	 * to that type.  The types are
	 * 
	 * 0:   Nothing unusual.  This is just a node internal to the tree whose
	 *        children need to be generated sometime.
	 * 1:   This is a leaf equivalent to the first leaf.  The mapping from
	 *        firstlab to lab is thus an automorphism.  After processing the
	 *        automorphism, we can return all the way to the closest invocation
	 *        of firstpathnode.
	 * 2:   This is a leaf equivalent to the bsf leaf.  Again, we have found an
	 *        automorphism, but it may or may not be as useful as one from a
	 *        type-1 node.  Return as far up the tree as possible.
	 * 3:   This is a new bsf node, provably better than the previous bsf node.
	 *        After updating canonlab etc., treat it the same as type 4.
	 * 4:   This is a leaf for which we can prove that no descendant is
	 *        equivalent to the first or bsf leaf or better than the bsf leaf.
	 *        Return up the tree as far as possible, but this may only be by
	 *        one level.
	 * 
	 * Types 2 and 3 can't occur if getcanon==FALSE.
	 * The value returned is the level in the tree to return to, which can be
	 * anywhere up to the closest invocation of firstpathnode.
	 * 
	 * FUNCTIONS CALLED:    isautom(),updatecan(),testcanlab(),fmperm(),
	 *                      writeperm(),(*userautomproc)(),orbjoin(),
	 *                      shortprune(),fmptn()
	 */
	private int processnode(int[] lab, int[] ptn, int level, int numcells){
		int save, newlevel;
		boolean ispruneok;
		IntPtr sr = new IntPtr();

		int code = 0;
		if(eqlev_first != level && (!getcanon || comp_canon < 0)){
			code = 4;
		}else if(numcells == n){
			if(eqlev_first == level){
				for(int i = 0; i < n; ++i){
					workperm[firstlab[i]] = lab[i];
				}

				if(gca_first >= noncheaplevel || nauSparse.isautom_sg(g, workperm, digraph)){
					code = 1;
				}
			}
			if(code == 0){
				if(getcanon){
					sr = new IntPtr();
					if(comp_canon == 0){
						if(level < canonlevel){
							comp_canon = 1;
						}else{
							nauSparse.updatecan_sg(g, canong, canonlab, samerows);
							samerows = n;
							comp_canon = nauSparse.testcanlab_sg(g, canong, lab, sr);
						}
					}
					if(comp_canon == 0){
						for(int i = 0; i < n; ++i){
							workperm[canonlab[i]] = lab[i];
						}
						code = 2;
					}else if(comp_canon > 0){
						code = 3;
					}else{
						code = 4;
					}
				}else{
					code = 4;
				}
			}
		}

		if(code != 0 && level > stats.maxlevel){
			stats.maxlevel = level;
		}

		switch(code){
		case 0: /* nothing unusual noticed */
			return level;

		case 1: /* lab is equivalent to firstlab */
			workspace.fmperm(workperm, n);
			stats.numorbits = naUtil.orbjoin(orbits, workperm);
			++stats.numgenerators;
			return gca_first;

		case 2: /* lab is equivalent to canonlab */
			workspace.fmperm(workperm, n);
			save = stats.numorbits;
			stats.numorbits = naUtil.orbjoin(orbits, workperm);
			if(stats.numorbits == save){
				if(gca_canon != gca_first){
					needshortprune = true;
				}
				return gca_canon;
			}
			++stats.numgenerators;
			if(orbits[cosetindex] < cosetindex){
				return gca_first;
			}
			if(gca_canon != gca_first){
				needshortprune = true;
			}
			return gca_canon;

		case 3: /* lab is better than canonlab */
			++stats.canupdates;
			for(int i = 0; i < n; ++i){
				canonlab[i] = lab[i];
			}
			canonlevel = eqlev_canon = gca_canon = level;
			comp_canon = 0;
			canoncode[level + 1] = 077777;
			samerows = sr.val;
			break;

		case 4: /* non-automorphism terminal node */
			++stats.numbadleaves;
			break;
		} /* end of switch statement */

		/* only cases 3 and 4 get this far: */
		if(level != noncheaplevel){
			ispruneok = true;
			workspace.fmptn(lab, ptn, noncheaplevel, n);
		}else{
			ispruneok = false;
		}

		save = (allsamelevel > eqlev_canon ? allsamelevel - 1 : eqlev_canon);
		newlevel = (noncheaplevel <= save ? noncheaplevel - 1 : save);

		if(ispruneok && newlevel != gca_first){
			needshortprune = true;
		}
		return newlevel;
	}
	
	/**
	 * Recover the partition nest at level 'level' and update various other
	 * parameters.
	 * 
	 * FUNCTIONS CALLED: NONE
	 */
	private void recover(int[] ptn, int level){
		for(int i = 0; i < n; ++i){
			if(ptn[i] > level){
				ptn[i] = NAUTY_INFINITY;
			}
		}

		if(level < noncheaplevel){
			noncheaplevel = level + 1;
		}
		if(level < eqlev_first){
			eqlev_first = level;
		}
		if(getcanon){
			if(level < gca_canon){
				gca_canon = level;
			}
			if(level <= eqlev_canon){
				eqlev_canon = level;
				comp_canon = 0;
			}
		}
	}
	
	@Deprecated
	public static int[] dynAllStat(){
		//effectively just a flag, but it keeps things more consistent with nauty
		return new int[0];
	}
	
	@Deprecated
	public static int[] dynAlloc1(int[] name, int sz){
		//JVM handles memory, so just make sure we are large enough
		if(name == null || sz > name.length){
			name = new int[sz];
		}
		
		return name;
	}
	
	//original macro depends on reference passing
	private static void multiply(StatsBlk stats, int index){
		if((stats.grpsize1 *= index) >= 1e10){
			stats.grpsize1 /= 1e10;
			stats.grpsize2 += 10;
		}
	}
}
