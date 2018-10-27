package gui.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RenderedSample {
	public final List<RenderedInterval> intervals;
	
	public RenderedSample(Collection<RenderedInterval> intervals) {
		List<RenderedInterval> intervals2=new ArrayList<>(intervals);
		intervals2.sort((interval0, interval1)->
				Double.compare(interval0.xx(0), interval1.xx(0)));
		this.intervals=Collections.unmodifiableList(intervals2);
	}
}
