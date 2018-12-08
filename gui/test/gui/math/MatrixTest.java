package gui.math;

import gui.ui.progress.Progress;
import org.junit.Assert;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class MatrixTest {
	private static void assertEquals(double expected, double actual) {
		Assert.assertEquals((Object)(Double)expected, (Object)(Double)actual);
	}
	
	private static void assertEquals(double[] expected, double[] actual) {
		assertEquals(expected.length, actual.length);
		for (int ii=0; expected.length>ii; ++ii) {
			MatrixTest.assertEquals(expected[ii], actual[ii]);
		}
	}
	
	private static void assertEquals(double[][] expected, double[][] actual) {
		assertEquals(expected.length, actual.length);
		for (int ii=0; expected.length>ii; ++ii) {
			MatrixTest.assertEquals(expected[ii], actual[ii]);
		}
	}
	
	@Test
	public void testAddMulRow() throws Throwable {
		double[][] matrix=new double[][]{{1, 2}, {3, 4}, {5, 6}};
		Matrix.addMulRow(matrix, 1, 2, 6, 3);
		assertEquals(
				new double[][]{{1, 2}, {3, 4}, {11, 14}},
				matrix);
	}
	
	@Test
	public void testCopy() throws Throwable {
		double[][] matrix={{1, 2},{3, 4},{5, 6}};
		double[][] copy=Matrix.copy(matrix);
		assertEquals(matrix, copy);
		assertNotSame(matrix, copy);
		for (int rr=0; matrix.length>rr; ++rr) {
			assertNotSame(matrix[rr], copy[rr]);
		}
	}
	
	@Test
	public void testCreate() throws Throwable {
		assertEquals(
				new double[][]{{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}},
				Matrix.create(3, 2));
	}
	
	@Test
	public void testGaussianElimination() throws Throwable {
		try {
			Matrix.gaussianElimination(
					new double[][]{{1, 0}},
					new double[][]{{1}},
					true,
					Progress.NULL);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			Matrix.gaussianElimination(
					new double[][]{{1, 0}, {0, 1}},
					new double[][]{{2}},
					true,
					Progress.NULL);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			Matrix.gaussianElimination(
					new double[][]{{0}},
					new double[][]{{0}},
					true,
					Progress.NULL);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			Matrix.gaussianElimination(
					new double[][]{{0}},
					new double[][]{{3}},
					true,
					Progress.NULL);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			Matrix.gaussianElimination(
					new double[][]{{0, 0}, {0, 0}},
					new double[][]{{2}, {3}},
					true,
					Progress.NULL);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		assertEquals(
				new double[][]{{2}, {3}},
				Matrix.gaussianElimination(
						new double[][]{{1, 0}, {0, 1}},
						new double[][]{{2}, {3}},
						true,
						Progress.NULL));
		assertEquals(
				new double[][]{{3}, {2}},
				Matrix.gaussianElimination(
						new double[][]{{0, 1}, {1, 0}},
						new double[][]{{2}, {3}},
						true,
						Progress.NULL));
		assertEquals(
				new double[][]{{2}, {3}},
				Matrix.gaussianElimination(
						new double[][]{{1, 0}, {0, 2}},
						new double[][]{{2}, {6}},
						true,
						Progress.NULL));
		assertEquals(
				new double[][]{{2}, {3}},
				Matrix.gaussianElimination(
						new double[][]{{1, 0}, {0, 2}},
						new double[][]{{2}, {6}},
						false,
						Progress.NULL));
		assertEquals(
				new double[][]{{2}, {3}},
				Matrix.gaussianElimination(
						new double[][]{{2, 1}, {1, 1}},
						new double[][]{{7}, {5}},
						true,
						Progress.NULL));
	}
	
	@Test
	public void testHouseholderDecomposition() throws Throwable {
		try {
			Matrix.householderDecomposition(new double[][]{},
					Progress.NULL,
					Sum.preferred());
			fail();
		}
		catch (IllegalArgumentException ex) {
		}
		try {
			Matrix.householderDecomposition(new double[][]{{}},
					Progress.NULL,
					Sum.preferred());
			fail();
		}
		catch (IllegalArgumentException ex) {
		}
		try {
			Matrix.householderDecomposition(new double[][]{{0, 0}, {0, 0}},
					Progress.NULL,
					Sum.preferred());
			fail();
		}
		catch (ArithmeticException ex) {
		}
		for (double[][] aa: new double[][][]{
					{{2, 3}, {3, 5}},
					{{2, 3, 4}, {4, 2, 3}, {3, 4, 2}},
					{{-2, 3, -4}, {4, -2, 3}, {-3, 4, 2}},
					{{1, 0, 0}, {0, 1, 0}, {0, 1, 0}},
					{{2, 3, 4, 5}, {4, 2, 3, 5}, {3, 5, 4, 2}, {2, 4, 5, 3}},
					{{6, 2, 3, 4, 5}, {4, 6, 2, 3, 5}, {3, 5, 4, 6, 2}, {2, 4, 6, 5, 3}, {5, 6, 4, 3, 2}}}) {
			QRDecomposition qr=Matrix.householderDecomposition(
					aa, Progress.NULL, Sum.preferred());
			assertTrue(0.0>aa[0][0]*qr.rr[0][0]);
			double[][] a2=Matrix.multiply(qr.qq, qr.rr,
					Progress.NULL, Sum.preferred());
			double[][] i2=Matrix.multiply(qr.qq, Matrix.transpose(qr.qq),
					Progress.NULL, Sum.preferred());
			for (int rr=0; aa.length>rr; ++rr) {
				for (int cc=0; aa.length>cc; ++cc) {
					Assert.assertEquals(aa[rr][cc], a2[rr][cc], 0.01);
					Assert.assertEquals((rr==cc)?1.0:0.0, i2[rr][cc], 0.01);
					if (rr>cc) {
						assertEquals(0.0, qr.rr[rr][cc]);
					}
				}
			}
		}
	}
	
	@Test
	public void testMultiply() throws Throwable {
		try {
			Matrix.multiply(
					Matrix.create(1, 1),
					Matrix.create(2, 2),
					Progress.NULL, Sum.preferred());
			fail();
		}
		catch (ArithmeticException ex) {
		}
		assertEquals(
				new double[][]{{24.0, 29.0, 34.0}, {42.0, 51.0, 60.0}},
				Matrix.multiply(
						new double[][]{{2.0, 3.0}, {4.0, 5.0}},
						new double[][]{{3.0, 4.0, 5.0}, {6.0, 7.0, 8.0}},
						Progress.NULL,
						Sum.preferred()));
	}
	
	@Test
	public void testSwapDouble() throws Throwable {
		double[] array={1.0, 2.0, 3.0};
		Matrix.swap(array, 1, 1);
		assertEquals(new double[]{1.0, 2.0, 3.0}, array);
		Matrix.swap(array, 1, 2);
		assertEquals(new double[]{1.0, 3.0, 2.0}, array);
	}
	
	@Test
	public void testSwapInteger() throws Throwable {
		int[] array={1, 2, 3};
		Matrix.swap(array, 1, 1);
		Assert.assertArrayEquals(new int[]{1, 2, 3}, array);
		Matrix.swap(array, 1, 2);
		Assert.assertArrayEquals(new int[]{1, 3, 2}, array);
	}
	
	@Test
	public void testSwapColumns() throws Throwable {
		double[][] matrix={{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
		Matrix.swapColumns(matrix, 1, 1);
		assertEquals(
				new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
				matrix);
		Matrix.swapColumns(matrix, 1, 0);
		assertEquals(
				new double[][]{{2.0, 1.0}, {4.0, 3.0}, {6.0, 5.0}},
				matrix);
	}
	
	@Test
	public void testSwapRows() throws Throwable {
		double[][] matrix={{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
		Matrix.swapRows(matrix, 1, 1);
		assertEquals(
				new double[][]{{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}},
				matrix);
		Matrix.swapRows(matrix, 1, 2);
		assertEquals(
				new double[][]{{1.0, 2.0}, {5.0, 6.0}, {3.0, 4.0}},
				matrix);
	}
	
	@Test
	public void testTranspose() throws Throwable {
		assertEquals(
				new double[][]{{1, 3, 5},{2, 4, 6}},
				Matrix.transpose(new double[][]{{1, 2},{3, 4},{5, 6}}));
	}
}
