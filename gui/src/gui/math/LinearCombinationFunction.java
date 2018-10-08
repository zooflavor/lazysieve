package gui.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class LinearCombinationFunction implements Function<Double, Double> {
	public final List<Double> coefficients;
	public final List<Function<Double, Double>> functions;
	public final String name;
	public final Supplier<Sum> sumFactory;
	
	public LinearCombinationFunction(List<Double> coefficients,
			List<Function<Double, Double>> functions, String name,
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
	public Double apply(Double xx) {
		Sum sum=sumFactory.get();
		for (int ii=coefficients.size()-1; 0<=ii; --ii) {
			Double yy=functions.get(ii).apply(xx);
			if (null==yy) {
				return null;
			}
			sum.add(coefficients.get(ii)*yy);
		}
		return sum.sum();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
