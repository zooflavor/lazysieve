package gui.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

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

	public static final Color BACKGROUND=Color.LIGHT_GRAY;
	public static final List<Color> GRAPHS;
	public static final Color INTERPOLATION=Color.GRAY;
	public static final Color RULER=Color.WHITE;
	public static final Color TOOLTIP_BACKGROUND=Color.LIGHT_GRAY;
	public static final Color TOOLTIP_TEXT=Color.DARK_GRAY;
	
	static {
		try {
			Set<Color> colors=new TreeSet<>();
			int modifiers=Modifier.FINAL|Modifier.PUBLIC|Modifier.STATIC;
			for (Field field: Color.class.getDeclaredFields()) {
				if ((field.getModifiers()==modifiers)
						&& Color.class.equals(field.getType())) {
					Color color=(Color)field.get(null);
					colors.add(color);
				}
			}
			COLORS=Collections.unmodifiableList(new ArrayList<>(colors));
			
			Set<Color> graphs=new TreeSet<>(Color.COLORS);
			graphs.remove(Color.BLACK);
			graphs.remove(Color.DARK_GRAY);
			graphs.remove(Color.GRAY);
			graphs.remove(Color.LIGHT_GRAY);
			graphs.remove(Color.WHITE);
			graphs.remove(Color.TRANSPARENT);
			graphs.remove(BACKGROUND);
			graphs.remove(INTERPOLATION);
			graphs.remove(RULER);
			graphs.remove(TOOLTIP_BACKGROUND);
			graphs.remove(TOOLTIP_TEXT);
			GRAPHS=Collections.unmodifiableList(new ArrayList<>(graphs));
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
	
	public static List<Color> selectNew(Random random, int size,
			Consumer<Consumer<Color>> usedColors) {
		Map<Color, Integer> counts=new TreeMap<>();
		GRAPHS.forEach((color)->counts.put(color, 0));
		usedColors.accept((color)->{
			Integer count=counts.get(color);
			if (null!=count) {
				counts.put(color, count+1);
			}
		});
		List<Color> colors=new ArrayList<>(counts.size());
		List<Color> result=new ArrayList<>(size);
		while (result.size()<size) {
			colors.clear();
			int minCount=0;
			for (Map.Entry<Color, Integer> entry: counts.entrySet()) {
				Color color=entry.getKey();
				int count=entry.getValue();
				if (colors.isEmpty()
						|| (minCount>count)) {
					colors.clear();
					colors.add(color);
					minCount=count;
				}
				else if (minCount==count) {
					colors.add(color);
				}
			}
			if (colors.isEmpty()) {
				throw new IllegalStateException();
			}
			Color color=colors.get(random.nextInt(colors.size()));
			result.add(color);
			counts.put(color, counts.get(color)+1);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
