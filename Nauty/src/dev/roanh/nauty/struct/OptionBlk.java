package dev.roanh.nauty.struct;

public class OptionBlk{

	//TODO user level options will NOT be supported
	
	
//	My call:
//		static DEFAULTOPTIONS_SPARSEDIGRAPH(options);
//		statsblk stats;
//		options.getcanon = TRUE;
//		options.defaultptn = FALSE;
//
//		#define DEFAULTOPTIONS_SPARSEDIGRAPH(options) optionblk options = \
//		 {0,TRUE,FALSE,FALSE,TRUE,FALSE,CONSOLWIDTH, \
//		  NULL,NULL,NULL,NULL,NULL,NULL,adjacencies_sg,100,0,999,0,&dispatch_sparse,FALSE,NULL}
	
		//TODO somehow get all this stuff to be final...
	
	//repeat: 0(true),TRUE,FALSE,FALSE,TRUE(false),FALSE,CONSOLWIDTH
	
//	    public final boolean getcanon = true;             /* make canong and canonlab? */
//	    public final boolean digraph = true;          /* multiple edges or loops? */
//	    public final boolean writeautoms = false;      /* write automorphisms? */
//	    public final boolean writemarkers = false;     /* write stats on pts fixed, etc.? */
	    public final boolean defaultptn = false;       /* set lab,ptn,active for single cell? */
//	    public final boolean cartesian = false;        /* use cartesian rep for writing automs? */
	    @Deprecated
	    public int linelength;           /* max chars/line (excl. '\n') for output */
	    
	    //repeat: NULL,NULL,NULL,NULL,
	    //these fill all not be supported
	    
//	    FILE *outfile;            /* file for output, if any */
//	    void (*userrefproc)       /* replacement for usual refine procedure */
//	         (graph*,int*,int*,int,int*,int*,set*,int*,int,int);
//	    void (*userautomproc)     /* procedure called for each automorphism */
//	         (int,int*,int*,int,int,int);
//	    void (*userlevelproc)     /* procedure called for each level */
//	         (int*,int*,int,int*,statsblk*,int,int,int,int,int,int);
	    
	    //repeat: NULL,NULL,adjacencies_sg,
	    
//	    void (*usernodeproc)      /* procedure called for each node */
//	         (graph*,int*,int*,int,int,int,int,int,int);
//	    int  (*usercanonproc)     /* procedure called for better labellings */
//	         (graph*,int*,graph*,unsigned long,int,int,int);
//	    void (*invarproc)         /* procedure to compute vertex-invariant */
//	         (graph*,int*,int*,int,int,int,int*,int,boolean,int,int);
	    
	    //repeat: 100,0,999,0,&dispatch_sparse,FALSE,NULL}
	    
	    public final int tc_level = 100;             /* max level for smart target cell choosing */
	    public final int mininvarlevel = 0;        /* min level for invariant computation */
	    public final int maxinvarlevel = 999;        /* max level for invariant computation */
	    public final int invararg = 0;             /* value passed to (*invarproc)() */
	   // obsolete dispatchvec *dispatch;    /* vector of object-specific routines */
	    public final boolean schreier = false;         /* use random schreier method */
	    //void *extra_options;      /* arbitrary extra options */
}
