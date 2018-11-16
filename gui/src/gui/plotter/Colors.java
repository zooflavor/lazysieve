package gui.plotter;

import gui.ui.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

public class Colors {
	public static final Color BACKGROUND=Color.LIGHT_GRAY;
	public static final List<Color> GRAPHS;
	public static final Color INTERPOLATION=Color.GRAY;
	public static final Color RULER=Color.WHITE;
	public static final Color TOOLTIP_BACKGROUND=Color.LIGHT_GRAY;
	public static final Color TOOLTIP_TEXT=Color.DARK_GRAY;
	
	static {
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
	
	private Colors() {
	}
	
	static List<Color> selectNew(Random random, int size,
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
}
