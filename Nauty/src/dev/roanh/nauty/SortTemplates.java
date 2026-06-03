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
				
				sortSwap1(x, xb, xc);
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
						sortSwap1(x, xa, xb);
						++xa;
					}
					++xb;
				}

				while(xc >= xb && y[x[xc]] >= v){
					if(y[x[xc]] == v){
						sortSwap1(x, xc, xd);
						--xd;
					}
					--xc;
				}

				if(xb > xc){
					break;
				}

				sortSwap1(x, xb, xc);
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
				sortSwap1(x, xl, xh);
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
				sortSwap1(x, xl, xh);
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

	private static int sortMedianOf3(int a, int b, int c){
		return (a <= b) ? ((b <= c) ? b : ((c <= a) ? a : c)) : ((a <= c) ? a : ((c <= b) ? b : c));
	}

	private static void sortSwap1(int[] x, int i, int j){
		int tmp = x[i];
		x[i] = x[j];
		x[j] = tmp;
	}

	private static record StackEntry(int addr, int len){
	}
}
