package gui.math;

import gui.ui.progress.Progress;
import java.util.Arrays;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class Matrix {
	static {
		new Matrix();
	}
	
	private Matrix() {
	}
	
	public static void addMulRow(double[][] matrix, int from, int to,
			double factor, double divisor) {
		double[] fromRow=matrix[from];
		double[] toRow=matrix[to];
		for (int cc=fromRow.length-1; 0<=cc; --cc) {
			toRow[cc]+=fromRow[cc]*factor/divisor;
		}
	}
	
	public static double[][] copy(double[][] matrix) {
		int columns=matrix[0].length;
		int rows=matrix.length;
		matrix=Arrays.copyOf(matrix, rows);
		for (int rr=rows-1; 0<=rr; --rr) {
			matrix[rr]=Arrays.copyOf(matrix[rr], columns);
		}
		return matrix;
	}
	
	public static double[][] create(int rows, int columns) {
		return new double[rows][columns];
	}
	
	public static double[][] gaussianElimination(double[][] aa, double[][] bb,
			boolean completePivoting, Progress progress) throws Throwable {
		int sizeA=aa.length;
		if (sizeA!=aa[0].length) {
			throw new ArithmeticException("aa.length!=aa[0].length");
		}
		if (sizeA!=bb.length) {
			throw new ArithmeticException("aa.length!=bb.length");
		}
		int sizeB=bb[0].length;
		aa=copy(aa);
		bb=copy(bb);
		int[] columnSwaps=new int[sizeA];
		for (int ii=0; sizeA>ii; ++ii) {
			columnSwaps[ii]=ii;
		}
		Progress subProgress=progress.subProgress(0.0, null, 0.5);
		for (int ii=0; sizeA-1>ii; ++ii) {
			subProgress.progress(1.0*ii/(sizeA-1));
			int maxColumnsEnd=completePivoting?sizeA:ii+1;
			int maxColumn=ii;
			int maxRow=ii;
			for (int rr=ii; sizeA>rr; ++rr) {
				for (int cc=ii; maxColumnsEnd>cc; ++cc) {
					if (Math.abs(aa[maxRow][maxColumn])<Math.abs(aa[rr][cc])) {
						maxColumn=cc;
						maxRow=rr;
					}
				}
			}
			if (maxColumn!=ii) {
				swap(columnSwaps, ii, maxColumn);
				swapColumns(aa, ii, maxColumn);
			}
			if (maxRow!=ii) {
				swapRows(aa, ii, maxRow);
				swapRows(bb, ii, maxRow);
			}
			if (0.0==aa[ii][ii]) {
				throw new ArithmeticException("0.0==aa[ii][ii]");
			}
			for (int rr=ii+1; sizeA>rr; ++rr) {
				if (0.0!=aa[rr][ii]) {
					double factor=-aa[rr][ii];
					double divisor=aa[ii][ii];
					addMulRow(aa, ii, rr, factor, divisor);
					addMulRow(bb, ii, rr, factor, divisor);
					aa[rr][ii]=0.0;
				}
			}
		}
		subProgress.finished();
		subProgress=progress.subProgress(0.5, null, 1.5);
		for (int ii=sizeA-1; 0<=ii; --ii) {
			subProgress.progress(1.0*(sizeA-1-ii)/(sizeA));
			if (0.0==aa[ii][ii]) {
				throw new ArithmeticException("0.0==aa[ii][ii]");
			}
			for (int rr=ii-1; 0<=rr; --rr) {
				if (0.0!=aa[rr][ii]) {
					double factor=-aa[rr][ii];
					double divisor=aa[ii][ii];
					addMulRow(aa, ii, rr, factor, divisor);
					addMulRow(bb, ii, rr, factor, divisor);
					aa[rr][ii]=0.0;
				}
			}
			double divisor=aa[ii][ii];
			for (int cc=sizeA-1; 0<=cc; --cc) {
				aa[ii][cc]/=divisor;
			}
			for (int cc=sizeB-1; 0<=cc; --cc) {
				bb[ii][cc]/=divisor;
			}
		}
		for (int ii=0; sizeA>ii; ++ii) {
			while (columnSwaps[ii]!=ii) {
				swapRows(bb, ii, columnSwaps[ii]);
				swap(columnSwaps, ii, columnSwaps[ii]);
			}
		}
		progress.finished();
		return bb;
	}
	
	public static QRDecomposition householderDecomposition(double[][] matrix,
			Progress progress, Sum sum) throws Throwable {
		int size=matrix.length;
		if (0>=size) {
			throw new IllegalArgumentException("0>="+size);
		}
		if (size!=matrix[0].length) {
			throw new IllegalArgumentException(size+"!="+matrix[0].length);
		}
		double[][] wr=copy(matrix);
		double[][] rr=create(size, size);
		double[][] qq=create(size, size);
		for (int ii=0; size>ii; ++ii) {
			qq[ii][ii]=1.0;
		}
		for (int kk=0; size-1>kk; ++kk) {
			Progress progress2=progress.subProgress(
					1.0*kk/size, null, 1.0*(kk+1)/size);
			progress2.progress(0.0);
			sum.clear();
			for (int ii=kk; size>ii; ++ii) {
				double dd=wr[ii][kk];
				sum.add(dd*dd);
			}
			double alpha=Math.sqrt(sum.sum());
			if (0.0<wr[kk][kk]) {
				alpha*=-1.0;
			}
			double[][] vv=create(size-kk, 1);
			for (int ii=kk; size>ii; ++ii) {
				vv[ii-kk][0]=wr[ii][kk];
			}
			vv[0][0]-=alpha;
			sum.clear();
			for (int ii=0; vv.length>ii; ++ii) {
				double dd=vv[ii][0];
				sum.add(dd*dd);
			}
			double vn2=sum.sum();
			if (0.0==vn2) {
				throw new ArithmeticException("0.0==||v||");
			}
			double[][] q2=create(size, size);
			for (int ii=0; size>ii; ++ii) {
				q2[ii][ii]=1.0;
			}
			double [][]vvt=multiply(vv, transpose(vv),
					progress2.subProgress(0.0, null, 0.4), sum);
			for (int col=kk; size>col; ++col) {
				for (int row=kk; size>row; ++row) {
					q2[row][col]-=2.0*vvt[row-kk][col-kk]/vn2;
				}
			}
			qq=multiply(qq, transpose(q2),
					progress2.subProgress(0.4, null, 0.7), sum);
			wr=multiply(q2, wr, progress2.subProgress(0.7, null, 1.0), sum);
			rr[kk][kk]=alpha;
			for (int ii=0; kk>ii; ++ii) {
				rr[ii][kk]=wr[ii][kk];
			}
		}
		for (int ii=0; size>ii; ++ii) {
			rr[ii][size-1]=wr[ii][size-1];
		}
		progress.finished();
		return new QRDecomposition(qq, rr);
	}
	
	public static double[][] multiply(double[][] matrix0, double[][] matrix1,
			Progress progress, Sum sum) throws Throwable {
		if (matrix0[0].length!=matrix1.length) {
			throw new ArithmeticException("matrix[0].length!=matrix1.length");
		}
		int rows=matrix0.length;
		int columns=matrix1[0].length;
		int common=matrix1.length;
		double[][] result=create(rows, columns);
		for (int rr=0; rows>rr; ++rr) {
			progress.progress(1.0*rr/rows);
			for (int cc=columns-1; 0<=cc; --cc) {
				sum.clear();
				for (int dd=common-1; 0<=dd; --dd) {
					sum.add(matrix0[rr][dd]*matrix1[dd][cc]);
				}
				result[rr][cc]=sum.sum();
			}
		}
		sum.clear();
		return result;
	}
	
	public static void swap(double[] array, int index0, int index1) {
		if (index0!=index1) {
			double tt=array[index0];
			array[index0]=array[index1];
			array[index1]=tt;
		}
	}
	
	public static void swap(int[] array, int index0, int index1) {
		if (index0!=index1) {
			int tt=array[index0];
			array[index0]=array[index1];
			array[index1]=tt;
		}
	}
	
	public static void swapColumns(double[][] matrix, int column0,
			int column1) {
		if (column0!=column1) {
			for (int rr=matrix.length-1; 0<=rr; --rr) {
				swap(matrix[rr], column0, column1);
			}
		}
	}
	
	public static void swapRows(double[][] matrix, int row0, int row1) {
		if (row0!=row1) {
			double[] tt=matrix[row0];
			matrix[row0]=matrix[row1];
			matrix[row1]=tt;
		}
	}
	
	public static double[][] transpose(double[][] matrix) {
		int rows=matrix.length;
		int columns=matrix[0].length;
		double[][] result=create(columns, rows);
		for (int rr=rows-1; 0<=rr; --rr) {
			for (int cc=columns-1; 0<=cc; --cc) {
				result[cc][rr]=matrix[rr][cc];
			}
		}
		return result;
	}
}
