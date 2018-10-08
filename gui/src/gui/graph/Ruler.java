package gui.graph;

public class Ruler {
	public static enum Thinness {
		THIN(true, 0.5), THINNER(false, 0.25), THINNEST(false, 0.125);
		
		public final boolean label;
		public final double lineWidthFactor;
		
		private Thinness(boolean label, double lineWidthFactor) {
			this.label=label;
			this.lineWidthFactor=lineWidthFactor;
		}
	}
	
	public final String label;
	public final double pixelLevel;
	public final Thinness thinness;
	public final boolean vertical;
	
	public Ruler(double pixelLevel, Thinness thinness, boolean vertical,
			double viewLevel) {
		this.pixelLevel=pixelLevel;
		this.thinness=thinness;
		this.vertical=vertical;
		label=thinness.label?label(viewLevel):null;
	}
	
	private static String label(double level) {
		if (0.0==level) {
			return "0";
		}
		StringBuilder sb=new StringBuilder();
		if (0.0>level) {
			sb.append("-");
			level=-level;
		}
		int ee=(int)Math.round(Math.floor(Math.log10(level)));
		double mm=level/Math.pow(10.0, ee);
		String m2=String.format("%1$.3f", mm);
		while (m2.endsWith("0")) {
			m2=m2.substring(0, m2.length()-1);
		}
		if (m2.endsWith(".")) {
			m2=m2.substring(0, m2.length()-1);
		}
		sb.append(m2);
		sb.append("e");
		if (0<=ee) {
			sb.append("+");
		}
		sb.append(ee);
		return sb.toString();
	}
}
