package dev.roanh.nauty.ptr;

public class IntPtr{
	//use a reference as pointer
	public int val;
	
	//C style truthy checks on integer values
	public boolean check(){
		return val != 0;
	}
}
