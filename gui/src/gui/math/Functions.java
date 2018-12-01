package gui.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class Functions {
	public static final Comparator<RealFunction> COMPARATOR
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
	public static final RealFunction LNX_LNLNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return 1.0<fromX;
				}

				@Override
				public String toString() {
					return "ln(x)*ln(ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if (1.0>=xx) {
						return Double.NaN;
					}
					double lnx=Math.log(xx);
					return lnx*Math.log(lnx);
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
					return (1.0<fromX)
                            && ((Math.E<fromX)
                                    || (Math.E>toX));
				}
				
				@Override
				public String toString() {
					return "1/ln(ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if ((1.0>=xx)
							|| (Math.E==xx)) {
						return Double.NaN;
					}
					return 1.0/Math.log(Math.log(xx));
				}
			};
	public static final RealFunction ONE_PER_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
                    return (0.0<fromX)
                            && ((1.0<fromX)
                                    || (1.0>toX));
				}
				
				@Override
				public String toString() {
					return "1/ln(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					return 1.0/Math.log(xx);
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
					return (1.0<fromX)
                            && ((Math.E<fromX)
                                    || (Math.E>toX));
				}
				
				@Override
				public String toString() {
					return "x/ln(ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if ((1.0>=xx)
							|| (Math.E==xx)) {
						return Double.NaN;
					}
					return xx/Math.log(Math.log(xx));
				}
			};
	public static final RealFunction X_PER_LNLNX_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return (1.0<fromX)
                            && ((Math.E<fromX)
                                    || (Math.E>toX));
				}
				
				@Override
				public String toString() {
					return "x/(ln(ln(x))*ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if ((1.0>=xx)
							|| (Math.E==xx)) {
						return Double.NaN;
					}
					double lnx=Math.log(xx);
					return xx/(Math.log(lnx)*lnx);
				}
			};
	public static final RealFunction X_PER_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
                    return (0.0<fromX)
                            && ((1.0<fromX)
                                    || (1.0>toX));
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
	public static final RealFunction X_PER_LN2X
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
                    return (0.0<fromX)
                            && ((1.0<fromX)
                                    || (1.0>toX));
				}
				
				@Override
				public String toString() {
					return "x/ln^2(x)";
				}
				
				@Override
				public double valueAt(double xx) {
					if (0.0>=xx) {
						return Double.NaN;
					}
					double lnx=Math.log(xx);
					return xx/(lnx*lnx);
				}
			};
	public static final RealFunction X2_PER_LNLNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
					return (1.0<fromX)
                            && ((Math.E<fromX)
                                    || (Math.E>toX));
				}
				
				@Override
				public String toString() {
					return "x^2/ln(ln(x))";
				}
				
				@Override
				public double valueAt(double xx) {
					if ((1.0>=xx)
							|| (Math.E==xx)) {
						return Double.NaN;
					}
					return xx*xx/Math.log(Math.log(xx));
				}
			};
	public static final RealFunction X2_PER_LNX
			=new RealFunction() {
				@Override
				public boolean isDefined(double fromX, double toX) {
                    return (0.0<fromX)
                            && ((1.0<fromX)
                                    || (1.0>toX));
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
		new Functions();
		List<RealFunction> functions=new ArrayList<>();
		functions.add(Functions.ONE);
		functions.add(Functions.X);
		functions.add(Functions.X2);
		functions.add(Functions.X3);
		functions.add(Functions.X4);
		functions.add(Functions.X5);
		functions.add(Functions.X6);
		functions.add(Functions.ONE_PER_X);
		functions.add(Functions.ONE_PER_X2);
		functions.add(Functions.SQRT_X);
		functions.add(Functions.LN2X);
		functions.add(Functions.LNX);
		functions.add(Functions.X_LN2X);
		functions.add(Functions.X_LNX);
		functions.add(Functions.LNLNX);
		functions.add(Functions.LNX_LNLNX);
		functions.add(Functions.X_LNLNX);
		functions.add(Functions.X_LNX_LNLNX);
		functions.add(Functions.X2_PER_LNX);
		functions.add(Functions.X_PER_LN2X);
		functions.add(Functions.ONE_PER_LNX);
		functions.add(Functions.X_PER_LNX);
		functions.add(Functions.ONE_PER_LNLNX);
		functions.add(Functions.X2_PER_LNLNX);
		functions.add(Functions.X_PER_LNLNX);
		functions.add(Functions.X_PER_LNLNX_LNX);
		functions.sort(COMPARATOR);
		FUNCTIONS=Collections.unmodifiableList(new ArrayList<>(functions));
	}
	
	private Functions() {
	}
}
