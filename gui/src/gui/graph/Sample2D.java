package gui.graph;

import gui.ui.Color;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Sample2D {
	public final Object id;
	public final String label;
	public final Color lineColor;
	public final Color pointColor;
	public final NavigableMap<Double, Double> sample;
	public final double sampleMaxX;
	public final double sampleMaxY;
	public final double sampleMinX;
	public final double sampleMinY;
	public final Color toolTipColor;

	private Sample2D(Object id, String label, Color lineColor,
			Color pointColor, NavigableMap<Double, Double> sample,
			double sampleMaxX, double sampleMaxY, double sampleMinX,
			double sampleMinY, Color toolTipColor) {
		if (sample.isEmpty()) {
			throw new IllegalArgumentException("empty sample");
		}
		this.id=id;
		this.label=label;
		this.lineColor=lineColor;
		this.pointColor=pointColor;
		this.sample=sample;
		this.sampleMaxX=sampleMaxX;
		this.sampleMaxY=sampleMaxY;
		this.sampleMinX=sampleMinX;
		this.sampleMinY=sampleMinY;
		this.toolTipColor=toolTipColor;
	}
	
	public Sample2D(Object id, String label, Color lineColor, Color pointColor,
			Map<Double, Double> sample, Color toolTipColor) {
		if (sample.isEmpty()) {
			throw new IllegalArgumentException("empty sample");
		}
		this.id=id;
		this.label=label;
		this.lineColor=lineColor;
		this.pointColor=pointColor;
		this.sample
				=Collections.unmodifiableNavigableMap(new TreeMap<>(sample));
		this.toolTipColor=toolTipColor;
		sampleMaxX=this.sample.lastKey();
		sampleMinX=this.sample.firstKey();
		double maxY=-Double.MAX_VALUE;
		double minY=Double.MAX_VALUE;
		for (double yy: this.sample.values()) {
			maxY=Math.max(maxY, yy);
			minY=Math.min(minY, yy);
		}
		sampleMaxY=maxY;
		sampleMinY=minY;
	}
	
	public Sample2D setColors(Color lineColor, Color pointColor,
			Color toolTipColor) {
		return new Sample2D(id, label, lineColor, pointColor, sample,
				sampleMaxX, sampleMaxY, sampleMinX, sampleMinY, toolTipColor);
	}
}
