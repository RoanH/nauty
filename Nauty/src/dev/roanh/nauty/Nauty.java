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
	
	
	
	//TODO probably going to have instances of nauty
	
	
	/**
	 * We only support sparse nauty the dispatch vector is hard wired.
	 */
	private final NauSparse dispatch = new NauSparse();
	
	
	//TODO I don't like that these exist...
//	private int m;
//	private int n;
//	private SparseGraph g;
//	private SparseGraph canong;
	
	@Deprecated
	final boolean doschreier = false;
	
	void
	nauty(SparseGraph g_arg, int[] lab, int[] ptn, @Deprecated /*passed as null from sparse nauty*/NSet active_arg,
	      int[] orbits_arg, OptionBlk options, StatsBlk stats_arg,
	      NSet ws_arg, int worksize, int m_arg, int n_arg, SparseGraph canong_arg)
	{
	    int i;
	    int numcells;
	    int retval;
	    IntPtr initstatus = new IntPtr();
	    TCNode tcp;
	    TCNode tcq;

	    /* determine dispatch vector */
	    //dispatch vector will be 'hardcoded' to sparse nauty

	    /* check for excessive sizes: */

	    if (m_arg > NAUTY_INFINITY/WORDSIZE+1)
	    {
	        throw new IllegalArgumentException("nauty: need m <= %d, but m=%d".formatted(NAUTY_INFINITY/WORDSIZE+1,m_arg));
	    }
	    if (n_arg > NAUTY_INFINITY-2 || n_arg > WORDSIZE * m_arg)
	    {
	        throw new IllegalArgumentException("nauty: need n <= min(%d,%d*m), but n=%d".formatted(NAUTY_INFINITY-2,WORDSIZE,n_arg));
	    }
	
	    if (n_arg == 0)   /* Special code for zero-sized graph */
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
	        dispatch.init_sg(g_arg,canong_arg,
                options,initstatus);
	        if (initstatus.check()){ stats_arg.errstatus = initstatus.val;}

	        return;
	    }

	    /* take copies of some args, and options: */
	    m = m_arg;
	    n = n_arg;

	    DYNALLOC1(set,defltwork,defltwork_sz,2*m,"nauty");
	    DYNALLOC1(set,fixedpts,fixedpts_sz,m,"nauty");
	    DYNALLOC1(set,active,active_sz,m,"nauty");
	    DYNALLOC1(int,workperm,workperm_sz,n,"nauty");
	    DYNALLOC1(int,firstlab,firstlab_sz,n,"nauty");
	    DYNALLOC1(int,canonlab,canonlab_sz,n,"nauty");
	    DYNALLOC1(short,firstcode,firstcode_sz,n+2,"nauty");
	    DYNALLOC1(short,canoncode,canoncode_sz,n+2,"nauty");
	    DYNALLOC1(int,firsttc,firsttc_sz,n+2,"nauty");
	    if (m > alloc_m)
	    {
	        tcp = tcnode0.next;
	        while (tcp != NULL)
	        {
	            tcq = tcp->next;
	            FREES(tcp->tcellptr);
	            FREES(tcp);
	            tcp = tcq;
	        }
	        alloc_m = m;
	        tcnode0.next = NULL;
	    }

	       /* OLD g = g_arg; */
	    orbits = orbits_arg;
	    stats = stats_arg;

	    getcanon = options->getcanon;
	    digraph = options->digraph;
	    writeautoms = options->writeautoms;
	    domarkers = options->writemarkers;
	    cartesian = options->cartesian;
	    linelength = options->linelength;
	    if (digraph){ tc_level = 0;}
	    else         {tc_level = options.tc_level;}
	    outfile = (options.outfile == NULL ? stdout : options->outfile);
	    usernodeproc = options->usernodeproc;
	    userautomproc = options->userautomproc;
	    userlevelproc = options->userlevelproc;
	    usercanonproc = options->usercanonproc;

	    invarproc = options->invarproc;
	    if (options.mininvarlevel < 0 && options->getcanon)
	        mininvarlevel = -options->mininvarlevel;
	    else
	        mininvarlevel = options->mininvarlevel;
	    if (options->maxinvarlevel < 0 && options->getcanon)
	        maxinvarlevel = -options->maxinvarlevel;
	    else
	        maxinvarlevel = options->maxinvarlevel;
	    invararg = options->invararg;

	    if (getcanon)
	        if (canong_arg == NULL)
	        {
	            stats_arg->errstatus = CANONGNIL;
	            fprintf(ERRFILE,
	                  "nauty: canong=NULL but options.getcanon=TRUE\n\n");
	            return;
	        }

	    /* initialize everything: */

	    if (options->defaultptn)
	    {
	        for (i = 0; i < n; ++i){   /* give all verts same colour */
	            lab[i] = i;
	            ptn[i] = NAUTY_INFINITY;
	        }
	        ptn[n-1] = 0;
	        EMPTYSET(active,m);
	        ADDELEMENT(active,0);
	        numcells = 1;
	    }
	    else
	    {
	        ptn[n-1] = 0;
	        numcells = 0;
	        for (i = 0; i < n; ++i){
	            if (ptn[i] != 0) {ptn[i] = NAUTY_INFINITY;}
	            else    {         ++numcells;}
	        }
	        if (active_arg == NULL)
	        {
	            EMPTYSET(active,m);
	            for (i = 0; i < n; ++i)
	            {
	                ADDELEMENT(active,i);
	                while (ptn[i]) {++i;}
	            }
	        }
	        else{
	            for (i = 0; i < M; ++i) {active[i] = active_arg[i];}
	        }
	    }

	    g = canong = NULL;
	    initstatus.val = 0;
	    
	    dispatch.init_sg(g_arg,canong_arg,
	            options,initstatus);
	    if (initstatus.check())
	    {
	        stats.errstatus = initstatus.val;
	        return;
	    }

	    g = g_arg;
	    canong = canong_arg;

	    for (i = 0; i < n; ++i) orbits[i] = i;
	    stats.grpsize1 = 1.0;
	    stats.grpsize2 = 0;
	    stats.numgenerators = 0;
	    stats.numnodes = 0;
	    stats.numbadleaves = 0;
	    stats.tctotal = 0;
	    stats.canupdates = 0;
	    stats.numorbits = n;
	    EMPTYSET(fixedpts,m);
	    noncheaplevel = 1;
	    eqlev_canon = -1;       /* needed even if !getcanon */

	    if (worksize >= 2 * m)
	        workspace = ws_arg;
	    else
	    {
	        workspace = defltwork;
	        worksize = 2 * m;
	    }
	    worktop = workspace + (worksize - worksize % (2 * m));
	    fmptr = workspace;

	    /* here goes: */
	    stats->errstatus = 0;
	    needshortprune = FALSE;
	    invarsuclevel = NAUTY_INFINITY;
	    invapplics = invsuccesses = 0;

	    retval = firstpathnode0(lab,ptn,1,numcells,&tcnode0);

	    if (retval == NAUTY_ABORTED)
	        stats->errstatus = NAUABORTED;
	    else if (retval == NAUTY_KILLED)
	        stats->errstatus = NAUKILLED;
	    else
	    {
	        if (getcanon)
	        {
	            (*dispatch.updatecan)(g,canong,canonlab,samerows,M,n);
	            for (i = 0; i < n; ++i){ lab[i] = canonlab[i];}
	        }
	        stats.invarsuclevel =
	             (invarsuclevel == NAUTY_INFINITY ? 0 : invarsuclevel);
	        stats.invapplics = invapplics;
	        stats.invsuccesses = invsuccesses;
	    }

	    if (n >= 320)
	    {
	        nautil_freedyn();
	        OPTCALL(dispatch.freedyn)();
	        nauty_freedyn();
	    }
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
