package gui.math;

import gui.ui.progress.Progress;
import org.junit.Assert;
import static org.junit.Assert.assertNotSame;
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
					Progress.NULL,
					true);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			Matrix.gaussianElimination(
					new double[][]{{1, 0}, {0, 1}},
					new double[][]{{2}},
					Progress.NULL,
					true);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			Matrix.gaussianElimination(
					new double[][]{{0}},
					new double[][]{{0}},
					Progress.NULL,
					true);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			Matrix.gaussianElimination(
					new double[][]{{0}},
					new double[][]{{3}},
					Progress.NULL,
					true);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			Matrix.gaussianElimination(
					new double[][]{{0, 0}, {0, 0}},
					new double[][]{{2}, {3}},
					Progress.NULL,
					true);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		assertEquals(
				new double[][]{{2}, {3}},
				Matrix.gaussianElimination(
						new double[][]{{1, 0}, {0, 1}},
						new double[][]{{2}, {3}},
						Progress.NULL,
						true));
		assertEquals(
				new double[][]{{3}, {2}},
				Matrix.gaussianElimination(
						new double[][]{{0, 1}, {1, 0}},
						new double[][]{{2}, {3}},
						Progress.NULL,
						true));
		assertEquals(
				new double[][]{{2}, {3}},
				Matrix.gaussianElimination(
						new double[][]{{1, 0}, {0, 2}},
						new double[][]{{2}, {6}},
						Progress.NULL,
						true));
		assertEquals(
				new double[][]{{2}, {3}},
				Matrix.gaussianElimination(
						new double[][]{{1, 0}, {0, 2}},
						new double[][]{{2}, {6}},
						Progress.NULL,
						false));
		assertEquals(
				new double[][]{{2}, {3}},
				Matrix.gaussianElimination(
						new double[][]{{2, 1}, {1, 1}},
						new double[][]{{7}, {5}},
						Progress.NULL,
						true));
	}
	
	@Test
	public void testIdentity() throws Throwable {
		assertEquals(
				new double[][]{
					{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0}},
				Matrix.identity(3));
	}
	
	@Test
	public void testInvert() throws Throwable {
		assertEquals(
				new double[][]{{-0.125, 0.375}, {0.375, -0.125}},
				Matrix.invert(
					new double[][]{{1, 3}, {3, 1}},
					Progress.NULL,
					true));
	}
	
	@Test
	public void testMultiply() throws Throwable {
		try {
			Matrix.multiply(
					Matrix.identity(1),
					Matrix.identity(2),
					Sum::priority);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		assertEquals(
				new double[][]{{10, 13}, {22, 29}, {34, 45}},
				Matrix.multiply(
					new double[][]{{1, 2}, {3, 4}, {5, 6}},
					new double[][]{{2, 3}, {4, 5}},
					Sum::priority));
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
