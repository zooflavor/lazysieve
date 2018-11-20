package gui.graph;

import gui.math.UnsignedLong;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GraphRenderer implements Runnable {
	private static final int PIXEL_SAMPLES=16;
	
	private boolean completed;
	private final CheckAndPost checkAndPost;
	private final Graph graph;
	private List<RenderedSample> functions;
	private List<Ruler> rulers;
	private List<RenderedSample> samples;
	
	public GraphRenderer(CheckAndPost checkAndPost, Graph graph) {
		this.checkAndPost=checkAndPost;
		this.graph=graph;
	}
	
	private void check() throws RendererDeathException {
		checkAndPost.check(graph);
	}
	
	private void checkAndPost() throws RendererDeathException {
		checkAndPost.checkAndPost(new RenderedGraph(
				completed, functions, graph, rulers, samples));
	}
	
	private RenderedSample renderFunction(Function function) throws Throwable {
		check();
		List<RenderedInterval> intervals=new ArrayList<>();
		RenderedInterval.Builder result
				=RenderedInterval.builder(Math.max(1, graph.componentWidth));
		FunctionSample sample=new FunctionSample(PIXEL_SAMPLES);
		double fromX=graph.pixelBorderX(0);
		for (int ii=0; graph.componentWidth>ii; ++ii) {
			check();
			double toX=graph.pixelBorderX(ii+1);
			if ((!function.function.isDefined(fromX, toX))
					&& (0<result.size())) {
				intervals.add(result.create());
				result=RenderedInterval.builder(
						Math.max(1, graph.componentWidth));
				continue;
			}
			sample.clear();
			for (int jj=0; PIXEL_SAMPLES>jj; ++jj) {
				double xx=graph.pixelBorderX(
						2*(ii*PIXEL_SAMPLES+jj)+1,
						2*PIXEL_SAMPLES*graph.componentWidth);
				double yy=function.function.valueAt(xx);
				if (Double.isFinite(yy)) {
					sample.add(xx, yy);
				}
			}
			renderSample(PlotType.LINE, result, sample,
					graph.graphToPixelX(fromX, toX));
			fromX=toX;
		}
		if (0<result.size()) {
			intervals.add(result.create());
		}
		return new RenderedSample(intervals);
	}
	
	private void renderRulers() {
		check();
		renderRulers(graph.componentWidth, graph::graphToPixelWidth,
				graph::graphToPixelX, rulers::add, true,
				graph.viewLeft, graph.viewRight);
		check();
		renderRulers(graph.componentHeight, graph::graphToPixelHeight,
				graph::graphToPixelY, rulers::add, false,
				graph.viewBottom, graph.viewTop);
	}
	
	private void renderRulers(double componentSize,
			java.util.function.Function<Double, Double> graphToPixelSize,
			java.util.function.Function<Double, Double> graphToPixelValue,
			Consumer<Ruler> rulers,
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
			check();
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
	
	private RenderedSample renderSample(Sample sample) throws Throwable {
		check();
		RenderedInterval.Builder result
				=RenderedInterval.builder(Math.max(1, graph.componentWidth));
		double pixelWidth=graph.viewWidth/graph.componentWidth;
		check();
		double leftBorder=graph.pixelBorderX(0);
		int leftIndex=sampleTailIndex(0, sample, sample.size(), leftBorder);
		if (0<leftIndex) {
			long leftX=sample.xx(leftIndex-1);
			double leftY=sample.yy(leftIndex-1);
			double leftXLimit=leftBorder-2.0*pixelWidth;
			double leftX2=leftX;
			if ((leftXLimit>leftX2)
					&& (samples.size()>leftIndex)) {
				long notSoLeftX=sample.xx(leftIndex);
				double notSoLeftY=sample.yy(leftIndex);
				leftY=((leftX-leftXLimit)*notSoLeftY
								+(leftXLimit-notSoLeftX)*leftY)
						/(leftX-notSoLeftX);
				leftX2=leftXLimit;
			}
			leftY=graph.graphToPixelY(leftY);
			result.add(graph.graphToPixelX(leftX2),
					leftY, leftY, leftY, leftY);
		}
		check();
		double rightBorder=graph.pixelBorderX(graph.componentWidth);
		int rightIndex=sampleTailIndex(leftIndex, sample, sample.size(),
				rightBorder);
		if (sample.size()>rightIndex) {
			long rightX=sample.xx(rightIndex);
			double rightY=sample.yy(rightIndex);
			double rightXLimit=rightBorder+2.0*pixelWidth;
			double rightX2=rightX;
			if ((rightXLimit<rightX2)
					&& (0<rightIndex)) {
				long notSoRightX=sample.xx(rightIndex-1);
				double notSoRightY=sample.yy(rightIndex-1);
				rightY=((rightX-rightXLimit)*notSoRightY
								+(rightXLimit-notSoRightX)*rightY)
						/(rightX-notSoRightX);
				rightX2=rightXLimit;
			}
			rightY=graph.graphToPixelY(rightY);
			result.add(graph.graphToPixelX(rightX2),
					rightY, rightY, rightY, rightY);
		}
		check();
		renderSample(
				leftIndex,
				0,
				graph.pixelBorderX(0),
				rightIndex,
				graph.componentWidth,
				graph.pixelBorderX(graph.componentWidth),
				result,
				sample);
		check();
		return new RenderedSample(Arrays.asList(result.create()));
	}
	
	private void renderSample(int leftIndex, int leftPixel,
			double leftPixelBorder, int rightIndex, int rightPixel,
			double rightPixelBorder, RenderedInterval.Builder result,
			Sample sample) throws Throwable {
		check();
		if (leftIndex>=rightIndex) {
			return;
		}
		if (leftPixel+1>=rightPixel) {
			renderSample(
					sample.plotType,
					result,
					sample.asIterableSample(leftIndex, rightIndex),
					graph.graphToPixelX(leftPixelBorder, rightPixelBorder));
			return;
		}
		int middlePixel=(leftPixel+rightPixel)/2;
		double middlePixelBorder=graph.pixelBorderX(middlePixel);
		int middleIndex=sampleTailIndex(
				leftIndex, sample, rightIndex, middlePixelBorder);
		renderSample(leftIndex, leftPixel, leftPixelBorder,
				middleIndex, middlePixel, middlePixelBorder,
				result, sample);
		renderSample(middleIndex, middlePixel, middlePixelBorder,
				rightIndex, rightPixel, rightPixelBorder,
				result, sample);
	}
	
	private <X extends Number, Y extends Number> void renderSample(
			PlotType plotType, RenderedInterval.Builder result,
			IterableSample sample, double xx) throws Throwable {
		check();
		if (sample.isEmpty()) {
			return;
		}
		class ConsumerImpl implements IterableSample.Consumer {
            double left;
            boolean hasLeft;
			double max=-Double.MAX_VALUE;
			double min=Double.MAX_VALUE;
            double right;
			
			@Override
			public void next(double key, double value) {
                if (!hasLeft) {
                    left=value;
                    hasLeft=true;
                }
                right=value;
				max=Math.max(max, value);
				min=Math.min(min, value);
			}
		}
		ConsumerImpl consumer=new ConsumerImpl();
		sample.forEach(consumer);
		switch (plotType) {
			case BARS:
				if (0.0>consumer.max) {
					consumer.max=0.0;
				}
				else if (0.0<consumer.min) {
					consumer.min=0.0;
				}
				break;
			case LINE:
				break;
			default:
				throw new IllegalStateException(plotType.toString());
		}
		result.add(xx,
				graph.graphToPixelY(consumer.left),
				graph.graphToPixelY(consumer.max),
				graph.graphToPixelY(consumer.min),
				graph.graphToPixelY(consumer.right));
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
			completed=true;
			checkAndPost();
		}
		catch (RendererDeathException ex) {
		}
		catch (Throwable throwable) {
			throwable.printStackTrace(System.err);
			System.err.flush();
		}
	}
	
	private int sampleTailIndex(int from, Sample sample, int to, double xx) {
		return sample.tailFromIndex(from, true, to,
				UnsignedLong.round(Math.ceil(Math.max(0.0, xx))));
	}
}
