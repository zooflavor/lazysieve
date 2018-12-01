package gui.util;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class BinarySearch {
	static {
		new BinarySearch();
	}
	
	private BinarySearch() {
	}
	
	public static int search(int from, Property property, int to) {
		int size=to-from;
		if (0>=size) {
			return from;
		}
		if (1==size) {
			return property.hasProperty(from)
					?from
					:to;
		}
		if (property.hasProperty(from)) {
			return from;
		}
		--to;
		if (!property.hasProperty(to)) {
			return to+1;
		}
		while (from+1<to) {
			int middle=(from+to)/2;
			if (property.hasProperty(middle)) {
				to=middle;
			}
			else {
				from=middle;
			}
		}
		return to;
	}
}
