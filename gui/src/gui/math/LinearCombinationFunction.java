package gui.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class LinearCombinationFunction implements RealFunction {
	public final List<Double> coefficients;
	public final List<RealFunction> functions;
	public final String name;
	public final Supplier<Sum> sumFactory;
	
	public LinearCombinationFunction(List<Double> coefficients,
			List<RealFunction> functions, String name,
			Supplier<Sum> sumFactory) {
		if (coefficients.size()!=functions.size()) {
			throw new IllegalArgumentException();
		}
		this.coefficients
				=Collections.unmodifiableList(new ArrayList<>(coefficients));
		this.functions
				=Collections.unmodifiableList(new ArrayList<>(functions));
		this.sumFactory=sumFactory;
		if (null==name) {
			StringBuilder name2=new StringBuilder();
			for (int ii=0; coefficients.size()>ii; ++ii) {
				if (0<name2.length()) {
					name2.append("+");
				}
				name2.append(coefficients.get(ii));
				name2.append("*");
				name2.append(functions.get(ii));
			}
			if (0>=name2.length()) {
				name2.append(0);
			}
			this.name=name2.toString();
		}
		else {
			this.name=name;
		}
	}
	
	@Override
	public boolean isDefined(double fromX, double toX) {
		for (int ii=coefficients.size()-1; 0<=ii; --ii) {
			if (!functions.get(ii).isDefined(fromX, toX)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public double valueAt(double xx) {
		Sum sum=sumFactory.get();
		for (int ii=coefficients.size()-1; 0<=ii; --ii) {
			double yy=functions.get(ii).valueAt(xx);
			if (!Double.isFinite(yy)) {
				return Double.NaN;
			}
			sum.add(coefficients.get(ii)*yy);
		}
		return sum.sum();
	}
}
