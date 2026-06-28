package dev.roanh.nauty.ds;

import java.util.Arrays;

/**
 * Java arrays start zero'ed, so no need to force a reset,
 * all of the performance tricks used in C here are redundant
 * on the JVM except for reducing the clear frequency which we
 * can even do much less often since we gain nothing from short CPU wise.
 * @author Roan
 */
@SuppressWarnings("javadoc")
public class MarkVal{
	private int[] marks;
	private int markVal = 1;
	
	public void mark(int i){
		marks[i] = markVal;
	}
	
	public void unmark(int i){
		marks[i] = 0;
	}
	
	public boolean isMarked(int i){
		return marks[i] == markVal;
	}
	
	public boolean isNotMarked(int i){
		return marks[i] != markVal;
	}

	public void reset(){
		if(++markVal == Integer.MAX_VALUE){
			Arrays.fill(marks, 0);
			markVal = 1;
		}
	}

	/**
	 * preparemarks1(N) and preparemarks2(N)
	 * make vmarks array large enough to mark 0..N-1 and such that
	 * the next RESETMARKS command will work correctly
	 */
	public void prepare(int nn){
		if(marks == null || nn > marks.length){
			marks = new int[nn];
			markVal = 1;
		}
	}
}
