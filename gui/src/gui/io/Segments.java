package gui.io;

import gui.ui.progress.Progress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Segments {
	public final long firstMissingStart;
	public final NavigableMap<Long, Segment.Info> segments;
	
	public Segments(Iterable<Segment.Info> segments) {
		NavigableMap<Long, Segment.Info> segments2
				=new TreeMap<>(Long::compareUnsigned);
		for (Segment.Info segment: segments) {
			Long start=segment.segmentStart;
			Segment.Info segment2=segments2.get(start);
			if ((null==segment2)
					|| (segment2.lastModification
							<segment.lastModification)) {
				segments2.put(start, segment);
			}
		}
		this.segments=Collections.unmodifiableNavigableMap(segments2);
		long firstMissingStart2=1l;
		for (; segments2.containsKey(firstMissingStart2);
				firstMissingStart2+=Segment.NUMBERS) {
		}
		firstMissingStart=firstMissingStart2;
	}
	
	public DatabaseInfo.TypeInfo info() {
		return DatabaseInfo.typeInfo(segments.navigableKeySet());
	}
	
	public List<Long> newSegments(
			NavigableMap<Long, Long> aggregateLastModifications,
			Progress progress) throws Throwable {
		progress.checkCancelled();
		List<Long> newSegments=new ArrayList<>();
		int ii=0;
		for (Map.Entry<Long, Segment.Info> entry: segments.entrySet()) {
			progress.progress("új szegmensfájlok", 1.0*ii/segments.size());
			++ii;
			Long start=entry.getKey();
			Segment.Info segment=entry.getValue();
			Long lastModification=aggregateLastModifications.get(start);
			if ((null==lastModification)
					|| (lastModification<segment.lastModification)) {
				newSegments.add(start);
			}
		}
		progress.finished();
		return newSegments;
	}
}
