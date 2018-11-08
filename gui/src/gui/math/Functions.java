package gui.math;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Functions {
	static Comparator<RealFunction> COMPARATOR
			=(f0, f1)->f0.toString().compareTo(f1.toString());
	public static final List<RealFunction> FUNCTIONS;
	public static final RealFunction LNLNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 1.0<fromX;
				}

				@Override
				public String toString() {
					return "ln(ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if (1.0>=xx) {
						return Double.NaN;
					}
					return Math.log(Math.log(xx));
				}
			};
	public static final RealFunction LNLNX_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 1.0<fromX;
				}

				@Override
				public String toString() {
					return "ln(ln(x))*ln(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (1.0>=xx) {
						return Double.NaN;
					}
					double lnx=Math.log(xx);
					return Math.log(lnx)*lnx;
				}
			};
	public static final RealFunction LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<fromX;
				}

				@Override
				public String toString() {
					return "ln(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					return Math.log(xx);
				}
			};
	public static final RealFunction LN2X
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<fromX;
				}

				@Override
				public String toString() {
					return "ln^2(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					double lnx=Math.log(xx);
					return lnx*lnx;
				}
			};
	public static final RealFunction ONE
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return true;
				}
				
				@Override
				public String toString() {
					return "1";
				}
				
				@Override
				public double valueAt(double xx) {
					return 1.0;
				}
			};
	public static final RealFunction ONE_PER_LNLNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<fromX;
				}
				
				@Override
				public String toString() {
					return "1/ln(ln(e+x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					return 1.0/Math.log(Math.log(Math.E+xx));
				}
			};
	public static final RealFunction ONE_PER_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<fromX;
				}
				
				@Override
				public String toString() {
					return "1/ln(1+x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					return 1.0/Math.log(1.0+xx);
				}
			};
	public static final RealFunction ONE_PER_X
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return (0.0<fromX)
							|| (0.0>toX);
				}
				
				@Override
				public String toString() {
					return "1/x";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0==xx) {
						return Double.NaN;
					}
					return 1.0/xx;
				}
			};
	public static final RealFunction ONE_PER_X2
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return (0.0<fromX)
							|| (0.0>toX);
				}
				
				@Override
				public String toString() {
					return "1/x^2";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0==xx) {
						return Double.NaN;
					}
					return 1.0/(xx*xx);
				}
			};
	public static final RealFunction SQRT_X
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<=toX;
				}
				
				@Override
				public String toString() {
					return "sqrt(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>xx) {
						return Double.NaN;
					}
					return Math.sqrt(xx);
				}
			};
	public static final RealFunction X
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return true;
				}
				
				@Override
				public String toString() {
					return "x";
				}
				
				@Override
				public double valueAt(double xx) {
					return xx;
				}
			};
	public static final RealFunction X2
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return true;
				}
				
				@Override
				public String toString() {
					return "x^2";
				}
				
				@Override
				public double valueAt(double xx) {
					return xx*xx;
				}
			};
	public static final RealFunction X3
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return true;
				}
				
				@Override
				public String toString() {
					return "x^3";
				}
				
				@Override
				public double valueAt(double xx) {
					return xx*xx*xx;
				}
			};
	public static final RealFunction X4
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return true;
				}
				
				@Override
				public String toString() {
					return "x^4";
				}
				
				@Override
				public double valueAt(double xx) {
					double x2=xx*xx;
					return x2*x2;
				}
			};
	public static final RealFunction X5
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return true;
				}
				
				@Override
				public String toString() {
					return "x^5";
				}
				
				@Override
				public double valueAt(double xx) {
					double x2=xx*xx;
					return x2*x2*xx;
				}
			};
	public static final RealFunction X6
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return true;
				}
				
				@Override
				public String toString() {
					return "x^6";
				}
				
				@Override
				public double valueAt(double xx) {
					double x2=xx*xx;
					return x2*x2*x2;
				}
			};
	public static final RealFunction X_LNLNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 1.0<fromX;
				}
				
				@Override
				public String toString() {
					return "x*ln(ln(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (1.0>=xx) {
						return Double.NaN;
					}
					return xx*Math.log(Math.log(xx));
				}
			};
	public static final RealFunction X_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<fromX;
				}
				
				@Override
				public String toString() {
					return "x*ln(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					return xx*Math.log(xx);
				}
			};
	public static final RealFunction X_LN2X
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<fromX;
				}
				
				@Override
				public String toString() {
					return "x*ln^2(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					double lnx=Math.log(xx);
					return xx*lnx*lnx;
				}
			};
	public static final RealFunction X_LNX_LNLNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 1.0<fromX;
				}
				
				@Override
				public String toString() {
					return "x*ln(x)*ln(ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if (1.0>=xx) {
						return Double.NaN;
					}
					double lnx=Math.log(xx);
					return xx*lnx*Math.log(lnx);
				}
			};
	public static final RealFunction X_PER_LNLNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 1.0<fromX;
				}
				
				@Override
				public String toString() {
					return "x/ln(ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if (1.0>=xx) {
						return Double.NaN;
					}
					return xx/Math.log(Math.log(xx));
				}
			};
	public static final RealFunction X2_PER_LNLNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 1.0<fromX;
				}
				
				@Override
				public String toString() {
					return "x^2/ln(ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if (1.0>=xx) {
						return Double.NaN;
					}
					return xx*xx/Math.log(Math.log(xx));
				}
			};
	public static final RealFunction X_PER_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<fromX;
				}
				
				@Override
				public String toString() {
					return "x/ln(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					return xx/Math.log(xx);
				}
			};
	public static final RealFunction X2_PER_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 0.0<fromX;
				}
				
				@Override
				public String toString() {
					return "x^2/ln(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					return xx*xx/Math.log(xx);
				}
			};
	
	static {
		List<RealFunction> functions0=new ArrayList<>();
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
		List<RealFunction> functions1=new ArrayList<>();
		try {
			int modifiers=Modifier.FINAL|Modifier.PUBLIC|Modifier.STATIC;
			for (Field field: Functions.class.getDeclaredFields()) {
				if ((field.getModifiers()==modifiers)
						&& RealFunction.class.equals(field.getType())) {
					RealFunction function=(RealFunction)field.get(null);
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
