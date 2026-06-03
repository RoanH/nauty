package dev.roanh.nauty.struct;

public class OptionBlk{

	
	
	
	    int getcanon;             /* make canong and canonlab? */
	    boolean digraph;          /* multiple edges or loops? */
	    boolean writeautoms;      /* write automorphisms? */
	    boolean writemarkers;     /* write stats on pts fixed, etc.? */
	    boolean defaultptn;       /* set lab,ptn,active for single cell? */
	    boolean cartesian;        /* use cartesian rep for writing automs? */
	    int linelength;           /* max chars/line (excl. '\n') for output */
	    FILE *outfile;            /* file for output, if any */
	    void (*userrefproc)       /* replacement for usual refine procedure */
	         (graph*,int*,int*,int,int*,int*,set*,int*,int,int);
	    void (*userautomproc)     /* procedure called for each automorphism */
	         (int,int*,int*,int,int,int);
	    void (*userlevelproc)     /* procedure called for each level */
	         (int*,int*,int,int*,statsblk*,int,int,int,int,int,int);
	    void (*usernodeproc)      /* procedure called for each node */
	         (graph*,int*,int*,int,int,int,int,int,int);
	    int  (*usercanonproc)     /* procedure called for better labellings */
	         (graph*,int*,graph*,unsigned long,int,int,int);
	    void (*invarproc)         /* procedure to compute vertex-invariant */
	         (graph*,int*,int*,int,int,int,int*,int,boolean,int,int);
	    int tc_level;             /* max level for smart target cell choosing */
	    int mininvarlevel;        /* min level for invariant computation */
	    int maxinvarlevel;        /* max level for invariant computation */
	    int invararg;             /* value passed to (*invarproc)() */
	    dispatchvec *dispatch;    /* vector of object-specific routines */
	    boolean schreier;         /* use random schreier method */
	    void *extra_options;      /* arbitrary extra options */
	
	
}
