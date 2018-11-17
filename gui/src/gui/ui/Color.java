package gui.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Color implements Comparable<Color> {
	public static final Color BLACK=Color.create("fekete", 0xff000000);
	public static final Color BLUE=Color.create("kék", 0xff0000aa);
	public static final Color BRIGHT_BLUE=Color.create("világos kék", 0xff5555ff);
	public static final Color BRIGHT_CYAN=Color.create("világos cián", 0xff55ffff);
	public static final Color BRIGHT_GREEN=Color.create("világos zöld", 0xff55ff55);
	public static final Color BRIGHT_MAGENTA=Color.create("világos bíbor", 0xffff55ff);
	public static final Color BRIGHT_RED=Color.create("világos vörös", 0xffff5555);
	public static final Color BRIGHT_YELLOW=Color.create("világos sárga", 0xffffff55);
	public static final Color BROWN=Color.create("barna", 0xffaa5500);
	public static final List<Color> COLORS;
	public static final Color CYAN=Color.create("cián", 0xff00aaaa);
	public static final Color DARK_GRAY=Color.create("sötét szürke", 0xff555555);
	public static final Color GRAY=Color.create("szürke", 0xff7f7f7f);
	public static final Color GREEN=Color.create("zöld", 0xff00aa00);
	public static final Color LIGHT_GRAY=Color.create("világos szürke", 0xffaaaaaa);
	public static final Color MAGENTA=Color.create("bíbor", 0xffaa00aa);
	public static final String NAMELESS="#";
	public static final Color RED=Color.create("vörös", 0xffaa0000);
	public static final Color TRANSPARENT=Color.create("átlátszó", 0x00000000);
	public static final Color WHITE=Color.create("fehér", 0xffffffff);
	
	static {
		try {
			List<Color> colors=new ArrayList<>();
			int modifiers=Modifier.FINAL|Modifier.PUBLIC|Modifier.STATIC;
			for (Field field: Color.class.getDeclaredFields()) {
				if ((field.getModifiers()==modifiers)
						&& Color.class.equals(field.getType())) {
					Color color=(Color)field.get(null);
					colors.add(color);
				}
			}
			colors.sort(null);
			COLORS=Collections.unmodifiableList(new ArrayList<>(colors));
		}
		catch (IllegalAccessException
				|IllegalArgumentException
				|SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public final int alpha;
	public final java.awt.Color awt;
	public final int blue;
	public final int green;
	public final String name;
	public final int red;
	
	public Color(int alpha, int blue, int green, String name, int red) {
		this.alpha=alpha;
		this.blue=blue;
		this.green=green;
		this.name=(null==name)
				?String.format("%1$s%5$02x%4$02x%3$02x%2$02x",
						NAMELESS, alpha, blue, green, red)
				:name;
		this.red=red;
		awt=new java.awt.Color(red, green, blue, alpha);
	}
	
	public Color alpha(int alpha) {
		return new Color(alpha, blue, green, null, red);
	}
	
	public java.awt.Color awt() {
		return awt;
	}
	
	public static Color create(String name, int argb) {
		return new Color(
				(argb>>>24)&0xff,
				argb&0xff,
				(argb>>>8)&0xff,
				name,
				(argb>>>16)&0xff);
	}
	
	public String html() {
		return String.format("#%1$02x%2$02x%3$02x", red, green, blue);
	}
	
	@Override
	public int compareTo(Color color) {
		if (equals(color)) {
			return 0;
		}
		boolean nn=name.startsWith(NAMELESS);
		if (nn!=color.name.startsWith(NAMELESS)) {
			return nn?1:-1;
		}
		return name.compareTo(color.name);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ((null==obj)
				|| (!getClass().equals(obj.getClass()))) {
			return false;
		}
		Color color=(Color)obj;
		return (blue==color.blue)
				&& (green==color.green)
				&& (red==color.red);
	}
	
	@Override
	public int hashCode() {
		return blue+13*green+31*red;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
