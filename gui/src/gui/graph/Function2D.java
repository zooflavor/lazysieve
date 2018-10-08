package gui.graph;

import gui.ui.Color;
import java.util.function.Function;

public class Function2D {
	public final Color color;
	public final Function<Double, Double> function;
	public final Object id;
	public final String label;
	public final Color toolTipColor;
	
	public Function2D(Color color, Function<Double, Double> function,
			Object id, String label, Color toolTipColor) {
		this.color=color;
		this.function=function;
		this.id=id;
		this.label=(null==label)?(function.toString()):label;
		this.toolTipColor=toolTipColor;
	}
	
	public Function2D(Color color, Function<Double, Double> function,
			Object id, Color toolTipColor) {
		this(color, function, id, null, toolTipColor);
	}
	
	public Function2D setColors(Color color, Color toolTipColor) {
		return new Function2D(color, function, id, label, toolTipColor);
	}
}
