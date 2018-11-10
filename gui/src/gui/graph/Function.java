package gui.graph;

import gui.math.RealFunction;
import gui.ui.Color;

public class Function {
	public final Color color;
	public final RealFunction function;
	public final String label;
	public final Color toolTipColor;
	
	public Function(Color color, RealFunction function, String label,
			Color toolTipColor) {
		this.color=color;
		this.function=function;
		this.label=(null==label)?(function.toString()):label;
		this.toolTipColor=toolTipColor;
	}
	
	public Function(Color color, RealFunction function, Color toolTipColor) {
		this(color, function, null, toolTipColor);
	}
	
	public Function setColors(Color color, Color toolTipColor) {
		return new Function(color, function, label, toolTipColor);
	}
}
