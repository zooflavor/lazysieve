package gui.math;

import gui.ui.progress.Progress;

@FunctionalInterface
public interface Solver {
	static Solver gaussianElimination(boolean completePivoting) {
		return (aa, bb, progress, sum)->Matrix.gaussianElimination(
				aa, bb, completePivoting, progress);
	}
	
	static Solver preferred() {
		return gaussianElimination(true);
	}
	
	static Solver qrDecomposition() {
		return (aa, bb, progress, sum)->{
			QRDecomposition qr=Matrix.householderDecomposition(
					aa, progress.subProgress(0.0, null, 0.4), sum);
			bb=Matrix.multiply(Matrix.transpose(qr.qq),
					bb, progress.subProgress(0.4, null, 0.8), sum);
			double cols=bb[0].length;
			for (int row=bb.length-1; 0<=row; --row) {
				double dd=qr.rr[row][row];
				for (int row2=0; row>row2; ++row2) {
					double ee=qr.rr[row2][row];
					for (int col=0; cols>col; ++col) {
						bb[row2][col]-=ee*bb[row][col]/dd;
					}
				}
				for (int col=0; cols>col; ++col) {
					bb[row][col]/=dd;
				}
			}
			return bb;
		};
	}
	
	double[][] solve(double[][] aa, double[][] bb, Progress progress, Sum sum)
			throws Throwable;
}
