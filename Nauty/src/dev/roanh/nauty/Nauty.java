package dev.roanh.nauty;

import dev.roanh.nauty.ds.NSet;
import dev.roanh.nauty.ptr.IntPtr;
import dev.roanh.nauty.struct.OptionBlk;
import dev.roanh.nauty.struct.SparseGraph;
import dev.roanh.nauty.struct.StatsBlk;
import dev.roanh.nauty.struct.TCNode;

public class Nauty{
	/**
	 * Max graph size is 2 billion.
	 */
	public static final int NAUTY_INFINITY = 2000000002;
	/**
	 * Doesn't really make sense in Java, can probably remove this completely.
	 */
	@Deprecated
	public static final int WORDSIZE = 32;//derive m from n where ever still needed using SETWORDSNEEDED
	
	//TODO how to handle errors... probably just throw tbh?
	public static final int NAUTY_ABORTED = -11;
	public static final int NAUTY_KILLED = -12;
	
	//TODO probably going to have instances of nauty
	
	/**
	 * We only support sparse nauty the dispatch vector and invar procedure are hard wired.
	 */
	private final NauSparse nauSparse = new NauSparse();
	private final NaUtil naUtil = new NaUtil();
	
	/* copies of some of the options: */
	boolean getcanon,digraph,writeautoms,domarkers,cartesian;
	int linelength,tc_level,mininvarlevel,maxinvarlevel,invararg;
	@Deprecated//these are all null
	Object usernodeproc, userautomproc, userlevelproc, usercanonproc;
	@Deprecated
	Object invarproc;//TODO type -> use nausparse.adjacencies_sg
	//TODO outfile ignored
	@Deprecated
	final boolean doschreier = false;
	
	/* local versions of some of the arguments: */
	@Deprecated
	int m;
	int n;
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
	
	private NSet defltwork = null;
	private NSet fixedpts = null;
	private NSet active = null;
	int[] workperm = dynAllStat();
	int[] firstlab = dynAllStat();
	int[] canonlab = dynAllStat();
	int[] firsttc = dynAllStat();
	int[] firstcode = dynAllStat();
	int[] canoncode = dynAllStat();
	
	/* In the dynamically allocated case (MAXN=0), each level of recursion
	   needs one set (tcell) to represent the target cell.  This is
	   implemented by using a linked list of tcnode anchored at the root
	   of the search tree.  Each node points to its child (if any) and to
	   the dynamically allocated tcell.  Apart from the first node of
	   the list, each node always has a tcell good for m up to alloc_m.
	   tcnodes and tcells are kept between calls to nauty, except that
	   they are freed and reallocated if m gets bigger than alloc_m.  */
	TCNode tcnode0 = new TCNode();
	@Deprecated
	int alloc_m = 0;
	int alloc_n = 0;
	
	NSet workspace;//first and just-after-last addresses of work area to hold automorphism data, space is a first address pointer in C
//	set *worktop;
//	set *fmptr;                   /* pointer into workspace */
	
//	void nauty(SparseGraph g_arg, int[] lab, int[] ptn, NSet active_arg, int[] orbits_arg, OptionBlk options, StatsBlk stats_arg, NSet ws_arg, int worksize, int m_arg, int n_arg, SparseGraph canong_arg){
	void nauty(SparseGraph g_arg, int[] lab, int[] ptn, int[] orbits_arg, OptionBlk options, StatsBlk stats_arg, NSet ws_arg, int worksize, int m_arg, int n_arg, SparseGraph canong_arg){
		int i;
		IntPtr numcells;
		int retval;
		IntPtr initstatus = new IntPtr();
		TCNode tcp;
		TCNode tcq;

		/* determine dispatch vector */
		//dispatch vector will be 'hardcoded' to sparse nauty

		/* check for excessive sizes: */

		if(m_arg > NAUTY_INFINITY / WORDSIZE + 1){
			throw new IllegalArgumentException("nauty: need m <= %d, but m=%d".formatted(NAUTY_INFINITY / WORDSIZE + 1, m_arg));
		}
		if(n_arg > NAUTY_INFINITY - 2 || n_arg > WORDSIZE * m_arg){
			throw new IllegalArgumentException("nauty: need n <= min(%d,%d*m), but n=%d".formatted(NAUTY_INFINITY - 2, WORDSIZE, n_arg));
		}

		if(n_arg == 0) /* Special code for zero-sized graph */
		{
			stats_arg.grpsize1 = 1.0;
			stats_arg.grpsize2 = 0;
			stats_arg.numorbits = 0;
			stats_arg.numgenerators = 0;
			stats_arg.errstatus = 0;
			stats_arg.numnodes = 1;
			stats_arg.numbadleaves = 0;
			stats_arg.maxlevel = 1;
			stats_arg.tctotal = 0;
			stats_arg.canupdates = options.getcanon ? 1 : 0;
			stats_arg.invapplics = 0;
			stats_arg.invsuccesses = 0;
			stats_arg.invarsuclevel = 0;

			initstatus.val = 0;
			nauSparse.init_sg(g_arg, canong_arg, options, initstatus);
			if(initstatus.check()){
				stats_arg.errstatus = initstatus.val;
			}

			return;
		}

		/* take copies of some args, and options: */
		m = m_arg;
		n = n_arg;

		defltwork = new NSet(2 * n);
		fixedpts = new NSet(n);
		active = new NSet(n);
		workperm = dynAlloc1(workperm, n);
		firstlab = dynAlloc1(firstlab, n);
		canonlab = dynAlloc1(canonlab, n);
		firstcode = dynAlloc1(firstcode, n + 2);
		canoncode = dynAlloc1(canoncode, n + 2);
		firsttc = dynAlloc1(firsttc, n + 2);
		if(m > alloc_m){
			tcp = tcnode0.next;
			alloc_m = m;
			alloc_n = n;
			tcnode0.next = null;
		}

		/* OLD g = g_arg; */
		orbits = orbits_arg;
		stats = stats_arg;

		getcanon = options.getcanon;
		digraph = options.digraph;
		writeautoms = options.writeautoms;
		domarkers = options.writemarkers;
		cartesian = options.cartesian;
		linelength = options.linelength;
		if(digraph){
			tc_level = 0;
		}else{
			tc_level = options.tc_level;
		}

		if(options.mininvarlevel < 0 && options.getcanon){
			mininvarlevel = -options.mininvarlevel;
		}else{
			mininvarlevel = options.mininvarlevel;
		}
		if(options.maxinvarlevel < 0 && options.getcanon){
			maxinvarlevel = -options.maxinvarlevel;
		}else{
			maxinvarlevel = options.maxinvarlevel;
		}
		invararg = options.invararg;

		if(getcanon){
			if(canong_arg == null){
				throw new IllegalArgumentException("nauty: canong=NULL but options.getcanon=TRUE");
			}
		}

		/* initialize everything: */

		if(options.defaultptn){//TODO fixed to false
//	        for (i = 0; i < n; ++i){   /* give all verts same colour */
//	            lab[i] = i;
//	            ptn[i] = NAUTY_INFINITY;
//	        }
//	        ptn[n-1] = 0;
//	        EMPTYSET(active,m);
//	        ADDELEMENT(active,0);
//	        numcells = 1;
		}else{
			ptn[n - 1] = 0;
			numcells.val = 0;
			for(i = 0; i < n; ++i){
				if(ptn[i] != 0){
					ptn[i] = NAUTY_INFINITY;
				}else{
					++numcells.val;
				}
			}

			active.clear();
			for(i = 0; i < n; ++i){
				active.addElement(i);
				while(ptn[i] != 0){
					++i;
				}
			}
		}

		g = null;
		canong = null;
		initstatus.val = 0;

		nauSparse.init_sg(g_arg, canong_arg, options, initstatus);
		if(initstatus.check()){
			stats.errstatus = initstatus.val;
			return;
		}

		g = g_arg;
		canong = canong_arg;

		for(i = 0; i < n; ++i){
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

		if(worksize >= 2 * m){
			workspace = ws_arg;
		}else{
			workspace = defltwork;
			worksize = 2 * m;
		}
		worktop = workspace + (worksize - worksize % (2 * m));
		fmptr = workspace;

		/* here goes: */
		stats.errstatus = 0;
		needshortprune = false;
		invarsuclevel = NAUTY_INFINITY;
		invapplics = invsuccesses = 0;

		retval = firstpathnode0(lab, ptn, 1, numcells, tcnode0);

		if(retval == NAUTY_ABORTED){
			throw new IllegalStateException("Nauty aborted");
		}else if(retval == NAUTY_KILLED){
			throw new IllegalStateException("Nauty killed");
		}else{
			if(getcanon){
				nauSparse.updatecan_sg(g, canong, canonlab, samerows, n);
				for(i = 0; i < n; ++i){
					lab[i] = canonlab[i];
				}
			}
			stats.invarsuclevel = (invarsuclevel == NAUTY_INFINITY ? 0 : invarsuclevel);
			stats.invapplics = invapplics;
			stats.invsuccesses = invsuccesses;
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
	int
	firstpathnode0(int[] lab, int[] ptn, int level, IntPtr numcells,
	          TCNode tcnode_parent)
	{
	    int tv;
	    int tv1,index,rtnlevel,tcellsize,tc,childcount;
	    IntPtr qinvar = new IntPtr();
	    IntPtr refcode = new IntPtr();
	    NSet tcell;
	    TCNode tcnode_this;

	    tcnode_this = tcnode_parent.next;
	    if (tcnode_this == null)
	    {
	    	tcnode_this = new TCNode(alloc_n);
	        tcnode_parent.next = tcnode_this;
	        tcnode_this.next = null;
	    }
	    tcell = tcnode_this.tcellptr;

	    ++stats.numnodes;

	    /* refine partition : */
	    naUtil.doref(g,lab,ptn,level,numcells,qinvar,workperm,
	          active,refcode,nauSparse,
	          mininvarlevel,maxinvarlevel,/*invararg,digraph,M,*/n);
	    firstcode[level] = (short)refcode.val;
	    if (qinvar.val > 0)
	    {
	        ++invapplics;
	        if (qinvar.val == 2)
	        {
	            ++invsuccesses;
	            if (mininvarlevel < 0){ mininvarlevel = level;}
	            if (maxinvarlevel < 0){ maxinvarlevel = level;}
	            if (level < invarsuclevel) {invarsuclevel = level;}
	        }
	    }

	    tc = -1;
	    if (numcells.val != n)
	    {
	     /* locate new target cell, setting tc to its position in lab, tcell
	                      to its contents, and tcellsize to its size: */
	        maketargetcell(g,lab,ptn,level,tcell,&tcellsize,
	                        &tc,tc_level,digraph,-1,dispatch.targetcell,M,n);
	        stats.tctotal += tcellsize;
	    }
	    firsttc[level] = tc;

	    /* optionally call user-defined node examination procedure: */
	    //OPTCALL(usernodeproc)(g,lab,ptn,level,numcells,tc,(int)firstcode[level],M,n);

	    if (numcells.val == n){/* found first leaf? */
	        firstterminal(lab,level);
	        //user level and canon proc are both not supported
//	        OPTCALL(userlevelproc)(lab,ptn,level,orbits,stats,0,1,1,n,0,n);
//	        if (getcanon && usercanonproc != NULL)
//	        {
//	            (*dispatch.updatecan)(g,canong,canonlab,samerows,M,n);
//	            samerows = n;
//	            if ((*usercanonproc)(g,canonlab,canong,stats->canupdates,(int)canoncode[level],M,n))
//	                return NAUTY_ABORTED;
//	        }
	        return level-1;
	    }

	    if(Thread.interrupted()){
	    	throw new InterruptedException();
	    }

	    if (noncheaplevel >= level && !nauSparse.cheapautom_sg(ptn,level,digraph,n)){
	        noncheaplevel = level + 1;
	        }

	    /* use the elements of the target cell to produce the children: */
	    index = 0;
	    for (tv1 = tv = tcell.nextelement(-1); tv >= 0;
	                                    tv = tcell.nextelement(tv))
	    {
	        if (orbits[tv] == tv)   /* ie, not equiv to previous child */
	        {
	            breakout(lab,ptn,level+1,tc,tv,active,M);
	            fixedpts.addElement(tv);
	            cosetindex = tv;
	            if (tv == tv1)
	            {
	                rtnlevel = firstpathnode0(lab,ptn,level+1,numcells.incNew(),
	                                         tcnode_this);
	                childcount = 1;
	                gca_first = level;
	                stabvertex = tv1;
	            }
	            else
	            {
	                rtnlevel = othernode0(lab,ptn,level+1,numcells.incNew(),
	                                     tcnode_this);
	                ++childcount;
	            }
	            fixedpts.delElement(tv);
	            if (rtnlevel < level)
	                return rtnlevel;
	            if (needshortprune)
	            {
	                needshortprune = false;
	                shortprune(tcell,fmptr-M,M);
	            }
	            recover(ptn,level);
	        }
	        if (orbits[tv] == tv1)  /* ie, in same orbit as tv1 */
	            ++index;
	    }
	    MULTIPLY(stats->grpsize1,stats->grpsize2,index);

	    if (tcellsize == index && allsamelevel == level + 1)
	        --allsamelevel;

//	    if (domarkers){//fixed to false
//	        writemarker(level,tv1,index,tcellsize,stats->numorbits,numcells);}
//	    OPTCALL(userlevelproc)(lab,ptn,level,orbits,stats,tv1,index,tcellsize,numcells,childcount,n);
	    return level-1;
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
	static int
	othernode0(int[] lab, int[] ptn, int level, int numcells,
	      tcnode *tcnode_parent)
	{
	    int tv;
	    int tv1,refcode,rtnlevel,tcellsize,tc,qinvar;
	    short code;
	    set *tcell;
	    tcnode *tcnode_this;

	    tcnode_this = tcnode_parent->next;
	    if (tcnode_this == NULL)
	    {
	        if ((tcnode_this = (tcnode*)ALLOCS(1,sizeof(tcnode))) == NULL ||
	            (tcnode_this->tcellptr
	                     = (set*)ALLOCS(alloc_m,sizeof(set))) == NULL)
	            alloc_error("tcell");
	        tcnode_parent->next = tcnode_this;
	        tcnode_this->next = NULL;
	    }
	    tcell = tcnode_this->tcellptr;

	    if (nauty_kill_request) return NAUTY_KILLED;

	    ++stats->numnodes;

	    /* refine partition : */
	    doref(g,lab,ptn,level,&numcells,&qinvar,workperm,active,
	          &refcode,dispatch.refine,invarproc,mininvarlevel,maxinvarlevel,
	          invararg,digraph,M,n);
	    code = (short)refcode;
	    if (qinvar > 0)
	    {
	        ++invapplics;
	        if (qinvar == 2)
	        {
	            ++invsuccesses;
	            if (level < invarsuclevel) invarsuclevel = level;
	        }
	    }

	    if (eqlev_first == level - 1 && code == firstcode[level])
	        eqlev_first = level;
	    if (getcanon)
	    {
	        if (eqlev_canon == level - 1)
	        {
	            if (code < canoncode[level])
	                comp_canon = -1;
	            else if (code > canoncode[level])
	                comp_canon = 1;
	            else
	            {
	                comp_canon = 0;
	                eqlev_canon = level;
	            }
	        }
	        if (comp_canon > 0) canoncode[level] = code;
	    }

	    tc = -1;
	   /* If children will be required, find new target cell and set tc to its
	      position in lab, tcell to its contents, and tcellsize to its size: */

	    if (numcells < n && (eqlev_first == level ||
	                         (getcanon && comp_canon >= 0)))
	    {
	        if (!getcanon || comp_canon < 0)
	        {
	            maketargetcell(g,lab,ptn,level,tcell,&tcellsize,&tc,
	                  tc_level,digraph,firsttc[level],dispatch.targetcell,M,n);
	            if (tc != firsttc[level]) eqlev_first = level - 1;
	        }
	        else
	            maketargetcell(g,lab,ptn,level,tcell,&tcellsize,&tc,
	                  tc_level,digraph,-1,dispatch.targetcell,M,n);
	        stats->tctotal += tcellsize;
	    }

	    /* optionally call user-defined node examination procedure: */
	    //OPTCALL(usernodeproc)(g,lab,ptn,level,numcells,tc,(int)code,M,n);

	    /* call processnode to classify the type of this node: */

	    rtnlevel = processnode(lab,ptn,level,numcells);
	    if (rtnlevel < level)   /* keep returning if necessary */
	        return rtnlevel;
	    if (needshortprune)
	    {
	        needshortprune = FALSE;
	        shortprune(tcell,fmptr-M,M);
	    }

	    if (!(*dispatch.cheapautom)(ptn,level,digraph,n))
	        noncheaplevel = level + 1;

	    /* use the elements of the target cell to produce the children: */
	    for (tv1 = tv = nextelement(tcell,M,-1); tv >= 0;
	                                    tv = nextelement(tcell,M,tv))
	    {
	        breakout(lab,ptn,level+1,tc,tv,active,M);
	        ADDELEMENT(fixedpts,tv);
	        rtnlevel = othernode0(lab,ptn,level+1,numcells+1,tcnode_this);
	        DELELEMENT(fixedpts,tv);

	        if (rtnlevel < level) return rtnlevel;
	    /* use stored automorphism data to prune target cell: */
	        if (needshortprune)
	        {
	            needshortprune = FALSE;
	            shortprune(tcell,fmptr-M,M);
	        }
	        if (tv == tv1)
	        {
	            longprune(tcell,fixedpts,workspace,fmptr,M);
	            //if (doschreier) pruneset(fixedpts,gp,&gens,tcell,M,n);
	        }

	        recover(ptn,level);
	    }

	    return level-1;
	}
	
	
	
	
	
	
	
	public static int[] dynAllStat(){
		//effectively just a flag, but it keeps things more consistent with nauty
		return new int[0];
	}
	
	public static int[] dynAlloc1(int[] name, int sz){
		//JVM handles memory, so just make sure we are large enough
		if(sz > name.length){
			name = new int[sz];
		}
		
		return name;
	}
}
