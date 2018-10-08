package gui.graph;

import gui.math.Sum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Graph2DRenderer implements Runnable {
	private static final int PIXEL_SAMPLES=16;
	
	private final Consumer<RenderedGraph2D> checkAndPost;
	private final Graph2D graph;
	private List<RenderedSample2D> functions;
	private List<Ruler> rulers;
	private List<RenderedSample2D> samples;
	private final Sum sum;
	
	public Graph2DRenderer(Consumer<RenderedGraph2D> checkAndPost,
			Graph2D graph, Supplier<Sum> sumFactory) {
		this.checkAndPost=checkAndPost;
		this.graph=graph;
		sum=sumFactory.get();
	}
	
	private void checkAndPost() throws RendererDeathException {
		checkAndPost.accept(
				new RenderedGraph2D(functions, graph, rulers, samples));
	}
	
	private RenderedSample2D renderFunction(Function2D function) {
		Map<Double, RenderedSample2D.Box> result
				=new HashMap<>(Math.max(1, graph.componentWidth));
		Map<Double, Double> sample=new HashMap<>();
		for (int ii=0; graph.componentWidth>ii; ++ii) {
			sample.clear();
			for (int jj=0; PIXEL_SAMPLES>jj; ++jj) {
				double xx=graph.pixelBorderX(
						2*(ii*PIXEL_SAMPLES+jj)+1,
						2*PIXEL_SAMPLES*graph.componentWidth);
				Double yy=function.function.apply(xx);
				if (null!=yy) {
					sample.put(xx, yy);
				}
			}
			renderSample(result, sample,
					graph.graphToPixelX(
							graph.pixelBorderX(ii),
							graph.pixelBorderX(ii+1)));
		}
		return new RenderedSample2D(result);
	}
	
	private void renderRulers() {
		renderRulers(graph.componentWidth, graph::graphToPixelWidth,
				graph::graphToPixelX, rulers::add, true,
				graph.viewLeft, graph.viewRight);
		renderRulers(graph.componentHeight, graph::graphToPixelHeight,
				graph::graphToPixelY, rulers::add, false,
				graph.viewBottom, graph.viewTop);
	}
	
	private static void renderRulers(double componentSize,
			Function<Double, Double> graphToPixelSize,
			Function<Double, Double> graphToPixelValue, Consumer<Ruler> rulers,
			boolean vertical, double viewFrom, double viewTo) {
		if (0.0>=componentSize) {
			return;
		}
		double viewSize=viewTo-viewFrom;
		if (0.0>=viewSize) {
			return;
		}
		double majorSteps=Math.pow(10.0, Math.floor(Math.log10(viewSize)));
		double steps=Math.pow(10.0,
				Math.floor(Math.log10(viewSize/componentSize)));
		while (5.0>=graphToPixelSize.apply(steps)) {
			steps*=10.0;
		}
		if (steps>majorSteps) {
			return;
		}
		long thin=Math.round(majorSteps/steps);
		long thinner=Math.round(majorSteps/(10.0*steps));
		for (long ii=Math.round(Math.floor(viewFrom/steps)); ; ++ii) {
			double level=ii*steps;
			if (viewTo<level) {
				break;
			}
			Ruler.Thinness thinness;
			if (0==(ii%thin)) {
				thinness=Ruler.Thinness.THIN;
			}
			else if (0==(ii%thinner)) {
				thinness=Ruler.Thinness.THINNER;
			}
			else {
				thinness=Ruler.Thinness.THINNEST;
			}
			rulers.accept(new Ruler(graphToPixelValue.apply(level), thinness,
					vertical, level));
		}
	}
	
	private RenderedSample2D renderSample(Sample2D sample) {
		Map<Double, RenderedSample2D.Box> result
				=new HashMap<>(Math.max(1, graph.componentWidth));
		double pixelWidth=graph.viewWidth/graph.componentWidth;
		Double leftX=sample.sample.lowerKey(graph.pixelBorderX(0));
		if (null!=leftX) {
			renderSample(
					result,
					sample.sample.subMap(
							leftX-pixelWidth, false, leftX, true),
					graph.graphToPixelX(leftX-pixelWidth, leftX));
		}
		for (int ii=0; graph.componentWidth>ii; ++ii) {
			double left=graph.pixelBorderX(ii);
			double right=graph.pixelBorderX(ii+1);
			renderSample(
					result,
					sample.sample.subMap(left, true, right, false),
					graph.graphToPixelX(left, right));
		}
		Double rightX=sample.sample.ceilingKey(
				graph.pixelBorderX(graph.componentWidth));
		if (null!=rightX) {
			renderSample(
					result,
					sample.sample.subMap(
							rightX, true, rightX+pixelWidth, false),
					graph.graphToPixelX(rightX, rightX+pixelWidth));
		}
		return new RenderedSample2D(result);
	}
	
	private void renderSample(Map<Double, RenderedSample2D.Box> result,
			Map<Double, Double> sample, double xx) {
		if (sample.isEmpty()) {
			return;
		}
		double max=-Double.MAX_VALUE;
		double min=Double.MAX_VALUE;
		sum.clear();
		for (Map.Entry<Double, Double> entry: sample.entrySet()) {
			double yy=entry.getValue();
			max=Math.max(max, yy);
			min=Math.min(min, yy);
			sum.add(yy);
		}
		result.put(xx,
				new RenderedSample2D.Box(
						graph.graphToPixelY(max),
						graph.graphToPixelY(sum.sum()/sample.size()),
						graph.graphToPixelY(min)));
	}
	
	@Override
	public void run() {
		try {
			functions=new ArrayList<>(graph.functions.size());
			rulers=new ArrayList<>();
			samples=new ArrayList<>(graph.samples.size());
			checkAndPost();
			renderRulers();
			checkAndPost();
			for (int ii=0; graph.samples.size()>ii; ++ii) {
				checkAndPost();
				samples.add(renderSample(graph.samples.get(ii)));
			}
			for (int ii=0; graph.functions.size()>ii; ++ii) {
				checkAndPost();
				functions.add(renderFunction(graph.functions.get(ii)));
			}
			checkAndPost();
		}
		catch (RendererDeathException ex) {
		}
	}
}
