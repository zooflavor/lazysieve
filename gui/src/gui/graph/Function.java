package gui.graph;

import gui.math.RealFunction;
import gui.ui.Color;
import java.util.Objects;

public class Function {
	public final Color color;
	public final RealFunction function;
	public final String label;
	
	public Function(Color color, RealFunction function, String label) {
		this.color=Objects.requireNonNull(color);
		this.function=Objects.requireNonNull(function);
		this.label=(null==label)?(function.toString()):label;
	}
	
	public Function(Color color, RealFunction function) {
		this(color, function, null);
	}
	
	public Function setColors(Color color) {
		return new Function(color, function, label);
	}
}
