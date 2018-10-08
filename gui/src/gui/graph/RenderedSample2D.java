package gui.graph;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class RenderedSample2D {
	public static class Box {
		public final double max;
		public final double mean;
		public final double min;
		
		public Box(double max, double mean, double min) {
			this.max=max;
			this.mean=mean;
			this.min=min;
		}
	}
	
	public final NavigableMap<Double, Box> sample;
	
	public RenderedSample2D(Map<Double, Box> sample) {
		this.sample
				=Collections.unmodifiableNavigableMap(new TreeMap<>(sample));
	}
}
