package dev.roanh.nauty.ds;

import java.util.Arrays;

/**
 * Queue used to hold automorphism data.
 * Former an area of 'set' in nauty managed by the workspace,
 * worktop and fmptr variables in nauty.c.
 * @author Roan
 */
public class Workspace{
	private final PruneRecord[] records;
	private final boolean[] workperm;
	private final int n;
	private int fmptr;
	
	public Workspace(int size, int n){
		this.n = n;
		records = new PruneRecord[size];
		workperm = new boolean[n];
		for(int i = 0; i < records.length; i++){
			records[i] = new PruneRecord(n);
		}
	}
	
	public void reset(){
		fmptr = 0;
	}
	
	public boolean canFit(int size, int n){
		return records.length >= size && this.n >= n;
	}
	
	/**
	 * shortprune(set1,set2,m) ANDs the contents of set set2 into set set1.
	 * 
	 * GLOBALS ACCESSED: NONE
	 */
	public void shortprune(NSet set1){
		//set2 is always the head of the queue in practise
		set1.intersect(records[fmptr - 1].mcr());
	}
	
	/**
	 * longprune(tcell,fix,bottom,top,m) removes zero or elements of the set
	 * tcell.  It is assumed that addresses bottom through top-1 contain
	 * contiguous pairs of sets (f1,m1),(f2,m2), ... .  tcell is intersected
	 * with each mi such that fi is a subset of fix.
	 * 
	 * GLOBALS ACCESSED: NONE
	 */
	public void longprune(NSet tcell, NSet fix){
		for(int i = 0; i < fmptr; i++){
			PruneRecord record = records[i];
			if(!fix.notSubSet(record.fix())){
				tcell.intersect(record.mcr());
			}
		}
	}
	
	/**
	 * fmperm(perm,fix,mcr,m,n) uses perm to construct fix and mcr.  fix
	 * contains those points are fixed by perm, while mcr contains the set of
	 * those points which are least in their orbits.
	 * 
	 * GLOBALS ACCESSED: bit<r>
	 */
	public void fmperm(int[] perm, int n){
		int i, k, l;

		Arrays.fill(workperm, 0, n + 1, false);
		PruneRecord record = nextRecord();
		record.clear();

		for(i = 0; i < n; ++i){
			if(perm[i] == i){
				record.addElement(i);
			}else if(!workperm[i]){
				l = i;
				do{
					k = l;
					l = perm[l];
					workperm[k] = true;
				}while(l != i);

				record.mcr().addElement(i);
			}
		}
	}
	
	/**
	 * fmptn(lab,ptn,level,fix,mcr,m,n) uses the partition at the specified
	 * level in the partition nest (lab,ptn) to make sets fix and mcr.  fix
	 * represents the points in trivial cells of the partition, while mcr
	 * represents those points which are least in their cells.
	 * 
	 * GLOBALS ACCESSED: bit<r>
	 */
	public void fmptn(int[] lab, int[] ptn, int level, int n){
		PruneRecord record = nextRecord();
		record.clear();

		for(int i = 0; i < n; ++i){
			if(ptn[i] <= level){
				record.addElement(lab[i]);
			}else{
				int lmin = lab[i];
				do{
					if(lab[++i] < lmin){
						lmin = lab[i];
					}
				}while(ptn[i] > level);
				record.mcr().addElement(lmin);
			}
		}
	}
	
	private PruneRecord nextRecord(){
		if(fmptr == records.length){
			return records[fmptr - 1];
		}else{
			return records[fmptr++];
		}
	}
	
	public static record PruneRecord(NSet fix, NSet mcr){
		
		public PruneRecord(int n){
			this(new NSet(n), new NSet(n));
		}
		
		public void clear(){
			fix.clear();
			mcr.clear();
		}
		
		public void addElement(int pos){
			fix.addElement(pos);
			mcr.addElement(pos);
		}
	}
}
