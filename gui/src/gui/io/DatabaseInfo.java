package gui.io;

import gui.math.UnsignedLong;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

public class DatabaseInfo {
	public static class Output {
		public final String key;
		public final String value;
		
		public Output(String key, String value) {
			this.key=key;
			this.value=value;
		}
		
		public static Output create(String key, String value) {
			return new Output(key, value);
		}
		
		public static Output create(String key, long value) {
			return new Output(key, UnsignedLong.format(value));
		}
	}
	
	public static class TypeInfo {
		public final Long firstSegmentStart;
		public final Long lastSegmentStart;
		public final Long missingSegments;
		public final Long missingSegmentStart;
		public final long numberOfSegments;
		
		public TypeInfo(Long firstSegmentStart, Long lastSegmentStart,
				Long missingSegments, Long missingSegmentStart,
				long numberOfSegments) {
			this.firstSegmentStart=firstSegmentStart;
			this.lastSegmentStart=lastSegmentStart;
			this.missingSegments=missingSegments;
			this.missingSegmentStart=missingSegmentStart;
			this.numberOfSegments=numberOfSegments;
		}
	}
	
	public final TypeInfo aggregates;
	public final long numberOfNewSegments;
	public final TypeInfo segments;

	public DatabaseInfo(TypeInfo aggregates, long numberOfNewSegments,
			TypeInfo segments) {
		this.aggregates=aggregates;
		this.numberOfNewSegments=numberOfNewSegments;
		this.segments=segments;
	}

	private String crunch(Database database, long segments) {
		if (0l!=numberOfNewSegments) {
			return "./database reaggregate";
		}
		if ((null!=this.segments.missingSegmentStart)
				&& (0<Long.compareUnsigned(Segment.GENERATOR_START_NUMBER,
						this.segments.missingSegmentStart))) {
			return String.format(
					"../generator/init.bin %1$s",
					database.rootDirectory);
		}
		if ((null==aggregates.missingSegmentStart)
				|| (null==aggregates.missingSegments)
				|| (0l==aggregates.missingSegments)
				|| (0l==segments)) {
			return "exit 0";
		}
		return String.format(
				"../generator/generator.bin %1$s start 0x%2$x segments 0x%3$x",
				database.rootDirectory,
				aggregates.missingSegmentStart,
				UnsignedLong.min(segments,
						aggregates.missingSegments));
	}
	
	public List<DatabaseInfo.Output> output(Long crunchSegments,
			Database database, boolean prefixType) {
		List<Output> output=new ArrayList<>();
		output(output, prefixType, "segment files", segments);
		output.add(null);
		output(output, prefixType, "aggregates", aggregates);
		if (0l<numberOfNewSegments) {
			output.add(null);
			output.add(Output.create(
				"new segment files",
				numberOfNewSegments));
		}
		if (null!=crunchSegments) {
			output.add(null);
			output.add(Output.create(
				"crunch",
				crunch(database, crunchSegments)));
		}
		return output;
	}
	
	private static void output(List<Output> output, boolean prefixType,
			String type, DatabaseInfo.TypeInfo typeInfo) {
		if (!prefixType) {
			output.add(new Output(type, null));
		}
		String prefix=prefixType?(type+": "):"";
		output.add(Output.create(String.format(
				"%1$snumber of segments",
				prefix),
				typeInfo.numberOfSegments));
		if (null!=typeInfo.firstSegmentStart) {
			output.add(Output.create(String.format(
					"%1$sstart of the first segment",
					prefix),
					typeInfo.firstSegmentStart));
		}
		if (null!=typeInfo.lastSegmentStart) {
			output.add(Output.create(String.format(
					"%1$sstart of the last segment",
					prefix),
					typeInfo.lastSegmentStart));
		}
		if (null!=typeInfo.missingSegmentStart) {
			output.add(Output.create(String.format(
					"%1$sstart of the first missing segment",
					prefix),
					typeInfo.missingSegmentStart));
		}
		if (null!=typeInfo.missingSegments) {
			output.add(Output.create(String.format(
					"%1$snumber of missing segments",
					prefix),
					typeInfo.missingSegments));
		}
	}
	
	public static DatabaseInfo.TypeInfo typeInfo(NavigableSet<Long> segments) {
		if (segments.isEmpty()) {
			return new TypeInfo(null, null, null, 1l, 0l);
		}
		long missingSegmentStart=1l;
		while ((0<Long.compareUnsigned(
						Segment.END_NUMBER, missingSegmentStart))
				&& segments.contains(missingSegmentStart)) {
			missingSegmentStart+=Segment.NUMBERS;
		}
		if (Segment.END_NUMBER==missingSegmentStart) {
			return new TypeInfo(
					segments.first(),
					segments.last(),
					null,
					null,
					segments.size());
		}
		Long missingSegments;
		Long next=segments.higher(missingSegmentStart);
		if (null==next) {
			next=Segment.END_NUMBER;
		}
		missingSegments=(next-missingSegmentStart)/Segment.NUMBERS;
		return new TypeInfo(
				segments.first(),
				segments.last(),
				missingSegments,
				missingSegmentStart,
				segments.size());
	}
}
