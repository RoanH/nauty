/*
 * Copyright 2026 Roan Hofland (roan@roanh.dev) (this Java port)
 * Copyright 2018 Brendan McKay (the original C source adapted here)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.roanh.nauty;

public class SortTemplates{
	/**
	 * Least number of elements for using quicksort partitioning,
	 * otherwise insertion sort is used (default "11")
	 */
	private static final int SORT_MINPARTITION = 11;
	private static final int SORT_MINMEDIAN9 = 320;

	public static void sortints(int[] x, int n){
		int i, j;
		int a, d, ba, dc, s, nn;
		int tmp1, v, v1, v2, v3;
		int x0, xa, xb, xc, xd, xh, xl;
		StackEntry[] stack = new StackEntry[40];
		int top;

		top = 0;
		if(n > 1){
			stack[top] = new StackEntry(0, n);
			++top;
		}

		while(top > 0){
			--top;
			x0 = stack[top].addr;
			nn = stack[top].len;

			if(nn < SORT_MINPARTITION){
				for(i = 1; i < nn; ++i){
					tmp1 = x[x0 + i];
					for(j = i; x[x0 + j - 1] > tmp1;){
						x[x0 + j] = x[x0 + j - 1];
						if(--j == 0){
							break;
						}
					}
					x[x0 + j] = tmp1;
				}
				continue;
			}

			if(nn < SORT_MINMEDIAN9){
				v = sortMedianOf3(x[x0], x[x0 + nn / 2], x[x0 + nn - 1]);
			}else{
				v1 = sortMedianOf3(x[x0], x[x0 + 1], x[x0 + 2]);
				v2 = sortMedianOf3(x[x0 + nn / 2 - 1], x[x0 + nn / 2], x[x0 + nn / 2 + 1]);
				v3 = sortMedianOf3(x[x0 + nn - 3], x[x0 + nn - 2], x[x0 + nn - 1]);
				v = sortMedianOf3(v1, v2, v3);
			}

			xa = x0;
			xb = x0;
			xc = x0 + (nn - 1);
			xd = x0 + (nn - 1);
			while(true){
				while(xb <= xc && x[xb] <= v){
					if(x[xb] == v){
						x[xb] = x[xa];
						x[xa] = v;
						++xa;
					}
					++xb;
				}
				
				while(xc >= xb && x[xc] >= v){
					if(x[xc] == v){
						x[xc] = x[xd];
						x[xd] = v;
						--xd;
					}
					--xc;
				}
				
				if(xb > xc){
					break;
				}
				
				sortSwap(x, xb, xc);
				++xb;
				--xc;
			}

			a = xa - x0;
			ba = xb - xa;
			if(ba > a){
				s = a;
			}else{
				s = ba;
			}
			
			for(xl = x0, xh = xb - s; s > 0; --s){
				x[xl] = x[xh];
				x[xh] = v;
				++xl;
				++xh;
			}
			
			d = xd - x0;
			dc = xd - xc;
			if(dc > nn - 1 - d){
				s = nn - 1 - d;
			}else{
				s = dc;
			}
			
			for(xl = xb, xh = x0 + (nn - s); s > 0; --s){
				x[xh] = x[xl];
				x[xl] = v;
				++xl;
				++xh;
			}

			if(ba > dc){
				if(ba > 1){
					stack[top] = new StackEntry(x0, ba);
					++top;
				}
				
				if(dc > 1){
					stack[top] = new StackEntry(x0 + (nn - dc), dc);
					++top;
				}
			}else{
				if(dc > 1){
					stack[top] = new StackEntry(x0 + (nn - dc), dc);
					++top;
				}
				
				if(ba > 1){
					stack[top] = new StackEntry(x0, ba);
					++top;
				}
			}
		}
	}

	public static void sortindirect(int[] x, int xOffset, int[] y, int n){
		int i, j;
		int a, d, ba, dc, s, nn;
		int tmp2, v, v1, v2, v3;
		int tmp1;
		int x0, xa, xb, xc, xd, xh, xl;

		StackEntry[] stack = new StackEntry[40];
		int top;

		top = 0;
		if(n > 1){
			stack[top] = new StackEntry(xOffset, n);
			++top;
		}

		while(top > 0){
			--top;
			x0 = stack[top].addr;
			nn = stack[top].len;

			if(nn < SORT_MINPARTITION){
				for(i = 1; i < nn; ++i){
					tmp1 = x[x0 + i];
					tmp2 = y[tmp1];
					for(j = i; y[x[x0 + j - 1]] > tmp2;){
						x[x0 + j] = x[x0 + j - 1];
						if(--j == 0){
							break;
						}
					}
					x[x0 + j] = tmp1;
				}
				continue;
			}

			if(nn < SORT_MINMEDIAN9){
				v = sortMedianOf3(y[x[x0]], y[x[x0 + nn / 2]], y[x[x0 + nn - 1]]);
			}else{
				v1 = sortMedianOf3(y[x[x0]], y[x[x0 + 1]], y[x[x0 + 2]]);
				v2 = sortMedianOf3(y[x[x0 + nn / 2 - 1]], y[x[x0 + nn / 2]], y[x[x0 + nn / 2 + 1]]);
				v3 = sortMedianOf3(y[x[x0 + nn - 3]], y[x[x0 + nn - 2]], y[x[x0 + nn - 1]]);
				v = sortMedianOf3(v1, v2, v3);
			}

			xa = x0;
			xb = x0;
			xc = x0 + (nn - 1);
			xd = x0 + (nn - 1);
			while(true){
				while(xb <= xc && y[x[xb]] <= v){
					if(y[x[xb]] == v){
						sortSwap(x, xa, xb);
						++xa;
					}
					++xb;
				}

				while(xc >= xb && y[x[xc]] >= v){
					if(y[x[xc]] == v){
						sortSwap(x, xc, xd);
						--xd;
					}
					--xc;
				}

				if(xb > xc){
					break;
				}

				sortSwap(x, xb, xc);
				++xb;
				--xc;
			}

			a = xa - x0;
			ba = xb - xa;
			if(ba > a){
				s = a;
			}else{
				s = ba;
			}

			for(xl = x0, xh = xb - s; s > 0; --s){
				sortSwap(x, xl, xh);
				++xl;
				++xh;
			}

			d = xd - x0;
			dc = xd - xc;
			if(dc > nn - 1 - d){
				s = nn - 1 - d;
			}else{
				s = dc;
			}

			for(xl = xb, xh = x0 + (nn - s); s > 0; --s){
				sortSwap(x, xl, xh);
				++xl;
				++xh;
			}

			if(ba > dc){
				if(ba > 1){
					stack[top] = new StackEntry(x0, ba);
					++top;
				}

				if(dc > 1){
					stack[top] = new StackEntry(x0 + (nn - dc), dc);
					++top;
				}
			}else{
				if(dc > 1){
					stack[top] = new StackEntry(x0 + (nn - dc), dc);
					++top;
				}

				if(ba > 1){
					stack[top] = new StackEntry(x0, ba);
					++top;
				}
			}
		}
	}
	
	public static void sortparallel(int[] x, int xOffset, int[] y, int yOffset, int n){
		int i, j;
		int a, d, ba, dc, s, nn;
		int tmp2, y0, ya, yb, yc, yd, yl, yh;
		int tmp1, v, v1, v2, v3;
		int x0, xa, xb, xc, xd, xh, xl;

		StackEntry[] stack = new StackEntry[40];
		int top;

		top = 0;
		if(n > 1){
			stack[top] = new StackEntry(xOffset, n);
			++top;
		}

		while(top > 0){
			--top;
			x0 = stack[top].addr;
			y0 = yOffset + (x0 - xOffset);
			nn = stack[top].len;

			if(nn < SORT_MINPARTITION){
				for(i = 1; i < nn; ++i){
					tmp1 = x[x0 + i];
					tmp2 = y[y0 + i];
					for(j = i; x[x0 + j - 1] > tmp1;){
						x[x0 + j] = x[x0 + j - 1];
						y[y0 + j] = y[y0 + j - 1];
						if(--j == 0){
							break;
						}
					}
					x[x0 + j] = tmp1;
					y[y0 + j] = tmp2;
				}
				continue;
			}

			if(nn < SORT_MINMEDIAN9){
				v = sortMedianOf3(x[x0], x[x0 + nn / 2], x[x0 + nn - 1]);
			}else{
				v1 = sortMedianOf3(x[x0], x[x0 + 1], x[x0 + 2]);
				v2 = sortMedianOf3(x[x0 + nn / 2 - 1], x[x0 + nn / 2], x[x0 + nn / 2 + 1]);
				v3 = sortMedianOf3(x[x0 + nn - 3], x[x0 + nn - 2], x[x0 + nn - 1]);
				v = sortMedianOf3(v1, v2, v3);
			}

			xa = xb = x0;
			xc = xd = x0 + (nn - 1);
			ya = yb = y0;
			yc = yd = y0 + (nn - 1);
			while(true){
				while(xb <= xc && xb <= v){
					if(xb == v){
						xb = xa;
						xa = v;
						++xa;
						sortSwap(y, ya, yb);
						++ya;
					}
					++xb;
					++yb;
				}
				while(xc >= xb && xc >= v){
					if(xc == v){
						xc = xd;
						xd = v;
						--xd;
						sortSwap(y, yc, yd);
						--yd;
					}
					--xc;
					--yc;
				}
				if(xb > xc){
					break;
				}
				sortSwap(x, xb, xc);
				sortSwap(y, yb, yc);
				++xb;
				++yb;
				--xc;
				--yc;
			}

			a = xa - x0;
			ba = xb - xa;
			if(ba > a){
				s = a;
			}else{
				s = ba;
			}
			for(xl = x0, xh = xb - s, yl = y0, yh = yb - s; s > 0; --s){
				xl = xh;
				xh = v;
				++xl;
				++xh;
				sortSwap(y, yl, yh);
				++yl;
				++yh;
			}
			d = xd - x0;
			dc = xd - xc;
			if(dc > nn - 1 - d){
				s = nn - 1 - d;
			}else{
				s = dc;
			}
			for(xl = xb, xh = x0 + (nn - s), yl = yb, yh = y0 + (nn - s); s > 0; --s){
				xh = xl;
				xl = v;
				++xl;
				++xh;
				sortSwap(y, yl, yh);
				++yl;
				++yh;
			}

			if(ba > dc){
				if(ba > 1){
					stack[top] = new StackEntry(x0, ba);
					++top;
				}
				if(dc > 1){
					stack[top] = new StackEntry(x0 + (nn - dc), dc);
					++top;
				}
			}else{
				if(dc > 1){
					stack[top] = new StackEntry(x0 + (nn - dc), dc);
					++top;
				}
				if(ba > 1){
					stack[top] = new StackEntry(x0, ba);
					++top;
				}
			}
		}
	}

	private static int sortMedianOf3(int a, int b, int c){
		return (a <= b) ? ((b <= c) ? b : ((c <= a) ? a : c)) : ((a <= c) ? a : ((c <= b) ? b : c));
	}

	private static void sortSwap(int[] arr, int x, int y){
		int tmp = arr[x];
		arr[x] = arr[y];
		arr[y] = tmp;
	}

	//TODO consider turning this into two local int[] arrays for better CPU cache locality
	private static record StackEntry(int addr, int len){
	}
}
