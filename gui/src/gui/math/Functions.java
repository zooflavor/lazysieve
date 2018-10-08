package gui.math;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class Functions {
	public static final Comparator<Function<Double, Double>> COMPARATOR
			=(f0, f1)->f0.toString().compareTo(f1.toString());
	public static final List<Function<Double, Double>> FUNCTIONS;
	public static final Function<Double, Double> LNX
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (0.0>=xx) {
						return null;
					}
					return Math.log(xx);
				}
				
				@Override
				public String toString() {
					return "ln(x)";
				}
			};
	public static final Function<Double, Double> ONE
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					return 1.0;
				}
				
				@Override
				public String toString() {
					return "1";
				}
			};
	public static final Function<Double, Double> ONE_PER_X
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (0.0==xx) {
						return null;
					}
					return 1.0/xx;
				}
				
				@Override
				public String toString() {
					return "1/x";
				}
			};
	public static final Function<Double, Double> ONE_PER_X2
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (0.0==xx) {
						return null;
					}
					return 1.0/(xx*xx);
				}
				
				@Override
				public String toString() {
					return "1/x^2";
				}
			};
	public static final Function<Double, Double> SQRT_X
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (0.0>xx) {
						return null;
					}
					return Math.sqrt(xx);
				}
				
				@Override
				public String toString() {
					return "sqrt(x)";
				}
			};
	public static final Function<Double, Double> X
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					return xx;
				}
				
				@Override
				public String toString() {
					return "x";
				}
			};
	public static final Function<Double, Double> X2
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					return xx*xx;
				}
				
				@Override
				public String toString() {
					return "x^2";
				}
			};
	public static final Function<Double, Double> X3
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					return xx*xx*xx;
				}
				
				@Override
				public String toString() {
					return "x^3";
				}
			};
	public static final Function<Double, Double> X4
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					double x2=xx*xx;
					return x2*x2;
				}
				
				@Override
				public String toString() {
					return "x^4";
				}
			};
	public static final Function<Double, Double> X5
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					double x2=xx*xx;
					return x2*x2*xx;
				}
				
				@Override
				public String toString() {
					return "x^5";
				}
			};
	public static final Function<Double, Double> X6
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					double x2=xx*xx;
					return x2*x2*x2;
				}
				
				@Override
				public String toString() {
					return "x^6";
				}
			};
	public static final Function<Double, Double> X_LNLNX
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (1.0>=xx) {
						return null;
					}
					return xx*Math.log(Math.log(xx));
				}
				
				@Override
				public String toString() {
					return "x*ln(ln(x)";
				}
			};
	public static final Function<Double, Double> X_LNX
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (0.0>=xx) {
						return null;
					}
					return xx*Math.log(xx);
				}
				
				@Override
				public String toString() {
					return "x*ln(x)";
				}
			};
	public static final Function<Double, Double> X_LN2X
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (0.0>=xx) {
						return null;
					}
					double lnx=Math.log(xx);
					return xx*lnx*lnx;
				}
				
				@Override
				public String toString() {
					return "x*ln^2(x)";
				}
			};
	public static final Function<Double, Double> X_LNX_LNLNX
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (1.0>=xx) {
						return null;
					}
					double lnx=Math.log(xx);
					return xx*lnx*Math.log(lnx);
				}
				
				@Override
				public String toString() {
					return "x*ln(x)*ln(ln(x))";
				}
			};
	public static final Function<Double, Double> X_PER_LNLNX
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (1.0>=xx) {
						return null;
					}
					return xx/Math.log(Math.log(xx));
				}
				
				@Override
				public String toString() {
					return "x/ln(ln(x))";
				}
			};
	public static final Function<Double, Double> X_PER_LNX
			=new Function<Double, Double>() {
				@Override
				public Double apply(Double xx) {
					if (0.0>=xx) {
						return null;
					}
					return xx/Math.log(xx);
				}
				
				@Override
				public String toString() {
					return "x/ln(x)";
				}
			};
	
	static {
		List<Function<Double, Double>> functions0=new ArrayList<>();
		functions0.add(Functions.ONE);
		functions0.add(Functions.X);
		functions0.add(Functions.X2);
		functions0.add(Functions.X3);
		functions0.add(Functions.X4);
		functions0.add(Functions.X5);
		functions0.add(Functions.X6);
		functions0.add(Functions.ONE_PER_X);
		functions0.add(Functions.ONE_PER_X2);
		functions0.add(Functions.LNX);
		functions0.add(Functions.SQRT_X);
		List<Function<Double, Double>> functions1=new ArrayList<>();
		try {
			int modifiers=Modifier.FINAL|Modifier.PUBLIC|Modifier.STATIC;
			for (Field field: Functions.class.getDeclaredFields()) {
				if ((field.getModifiers()==modifiers)
						&& Function.class.equals(field.getType())) {
					@SuppressWarnings("unchecked")
					Function<Double, Double> function
							=(Function<Double, Double>)field.get(null);
					if (!functions0.contains(function)) {
						functions1.add(function);
					}
				}
			}
		}
		catch (IllegalAccessException
				|IllegalArgumentException
				|SecurityException ex) {
			throw new RuntimeException(ex);
		}
		functions1.sort(COMPARATOR);
		functions0.addAll(functions1);
		FUNCTIONS=Collections.unmodifiableList(new ArrayList<>(functions0));
	}
	
	private Functions() {
	}
}
