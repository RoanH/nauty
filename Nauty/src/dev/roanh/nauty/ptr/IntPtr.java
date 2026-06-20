package dev.roanh.nauty.ptr;

public class IntPtr{
	//use a reference as pointer
	public int val;
	
	public IntPtr(){
		this(0);
	}
	
	public IntPtr(int val){
		this.val = val;
	}
	
	//C style truthy checks on integer values
	public boolean check(){
		return val != 0;
	}
	
	public IntPtr incNew(){
		return new IntPtr(val + 1);
	}
}
