package gui.graph;

import gui.ui.Color;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class Sample2DTest {
	@Test
	public void test() throws Throwable {
		Sample sample=Sample.builder()
				.add(1, 8)
				.add(3, 6)
				.add(5, 4)
				.add(7, 2)
				.create("label", Color.YELLOW, PlotType.LINE, Color.YELLOW,
						Color.YELLOW);
		for (int ii=0; 8>=ii; ++ii) {
			int index0=sample.headToIndex(false, ii);
			int index1=sample.headToIndex(true, ii);
			assertTrue((0<=index0) && (index0<=sample.size()));
			assertTrue((0<=index1) && (index1<=sample.size()));
			assertEquals(index0, sample.tailFromIndex(true, ii));
			assertEquals(index1, sample.tailFromIndex(false, ii));
			for (int jj=0; sample.size()>jj; ++jj) {
				assertEquals(ii>sample.xx(jj), index0>jj);
				assertEquals(ii>=sample.xx(jj), index1>jj);
			}
		}
		for (int from=0; sample.size()>=from; ++from) {
			for (int to=0; sample.size()>=to; ++to) {
				for (int ii=0; 8>=ii; ++ii) {
					int index0=sample.headToIndex(from, false, to, ii);
					int index1=sample.headToIndex(from, true, to, ii);
					assertTrue(from<=index0);
					assertTrue(from<=index1);
					if (from<=to) {
						assertTrue(index0<=to);
						assertTrue(index1<=to);
					}
					assertEquals(index0,
							sample.tailFromIndex(from, true, to, ii));
					assertEquals(index1,
							sample.tailFromIndex(from, false, to, ii));
					for (int jj=from; to>jj; ++jj) {
						assertEquals(ii>sample.xx(jj), index0>jj);
						assertEquals(ii>=sample.xx(jj), index1>jj);
					}
				}
			}
		}
	}
}
