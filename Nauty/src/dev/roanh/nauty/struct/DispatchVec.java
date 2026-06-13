package dev.roanh.nauty.struct;

import dev.roanh.nauty.NauSparse;

/**
 * We only supported sparse nauty so this dispatch vector is essentially hard wired.
 * @author Roan
 */
public abstract sealed interface DispatchVec permits NauSparse{
	
	/**
	 * Test for automorphism
	 */
	public abstract boolean isautom();
	
	/*	DispatchVec dispatch_sparse =
	  {
	  isautom_sg,
	  testcanlab_sg,
	  updatecan_sg,
	  refine_sg, //refine  --equal
	  refine_sg, //refine1
	  cheapautom_sg,
	   targetcell_sg,
	   nausparse_freedyn, -> irrelevant GC
	   nausparse_check, -> probably could ignore
	   init_sg,
	   NULL
	   };
	*/
	
	
	    boolean (*isautom)        /* test for automorphism */
	            (graph*,int*,boolean,int,int);
	    int     (*testcanlab)     /* test for better labelling */
	            (graph*,graph*,int*,int*,int,int);
	    void    (*updatecan)      /* update canonical object */
	            (graph*,graph*,int*,int,int,int);
	    void    (*refine)         /* refine partition */
	            (graph*,int*,int*,int,int*,int*,set*,int*,int,int);
	    void    (*refine1)        /* refine partition, MAXM==1 */
	            (graph*,int*,int*,int,int*,int*,set*,int*,int,int);
	    boolean (*cheapautom)     /* test for easy automorphism */
	            (int*,int,boolean,int);
	    int     (*targetcell)     /* decide which cell to split */
	            (graph*,int*,int*,int,int,boolean,int,int,int);
	    void    (*freedyn)(void); /* free dynamic memory */
	    void    (*check)          /* check compilation parameters */
	            (int,int,int,int);
	    void    (*init)(graph*,graph**,graph*,graph**,int*,int*,set*,
	                   struct optionstruct*,int*,int,int);
	    //null for sparse nauty dispatch
	    void    (*cleanup)(graph*,graph**,graph*,graph**,int*,int*,
	                      struct optionstruct*,statsblk*,int,int);
	    
	    //note: optionstruct == optionblk
}
