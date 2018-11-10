package gui.graph;

import gui.plotter.Colors;
import gui.ui.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Graph {
	public static final Graph EMPTY=new Graph(
			Colors.BACKGROUND,
			-1,
			-1,
			new ArrayList<>(0),
			Colors.RULER.alpha(63),
			8.0,
			false,
			false,
			Colors.RULER,
			new ArrayList<>(0),
			Colors.TOOLTIP_BACKGROUND,
			Colors.TOOLTIP_TEXT,
			0.0,
			0.0,
			1.0,
			1.0);
	
	public final Color backgroundColor;
	public final int componentHeight;
	public final int componentWidth;
	public final List<Function> functions;
	public final Color incompleteColor;
	public final double labelSize;
	public final boolean logarithmicX;
	public final boolean logarithmicY;
	public final Color rulerColor;
	public final List<Sample> samples;
	public final Long samplesMaxX;
	public final Double samplesMaxY;
	public final Long samplesMinX;
	public final Double samplesMinY;
	public final Color toolTipBackgroundColor;
	public final Color toolTipTextColor;
	public final double viewBottom;
	public final double viewHeight;
	public final double viewLeft;
	public final double viewRight;
	public final double viewTop;
	public final double viewWidth;
	
	public Graph(Color backgroundColor, int componentHeight,
			int componentWidth, List<Function> functions,
			Color incompleteColor, double labelSize, boolean logarithmicX,
			boolean logarithmicY, Color rulerColor, List<Sample> samples,
			Color toolTipBackgroundColor, Color toolTipTextColor,
			double viewBottom, double viewLeft, double viewRight,
			double viewTop) {
		this.backgroundColor=backgroundColor;
		this.componentHeight=componentHeight;
		this.componentWidth=componentWidth;
		this.functions
				=Collections.unmodifiableList(new ArrayList<>(functions));
		this.incompleteColor=incompleteColor;
		this.labelSize=labelSize;
		this.logarithmicX=logarithmicX;
		this.logarithmicY=logarithmicY;
		this.rulerColor=rulerColor;
		this.samples=Collections.unmodifiableList(new ArrayList<>(samples));
		this.toolTipBackgroundColor=toolTipBackgroundColor;
		this.toolTipTextColor=toolTipTextColor;
		this.viewBottom=viewBottom;
		this.viewLeft=viewLeft;
		this.viewRight=viewRight;
		this.viewTop=viewTop;
		Long maxX=null;
		Double maxY=null;
		Long minX=null;
		Double minY=null;
		for (Sample sample: this.samples) {
			if ((null==maxX)
					|| (maxX<sample.sampleMaxX)) {
				maxX=sample.sampleMaxX;
			}
			if ((null==maxY)
					|| (maxY<sample.sampleMaxY)) {
				maxY=sample.sampleMaxY;
			}
			if ((null==minX)
					|| (minX>sample.sampleMinX)) {
				minX=sample.sampleMinX;
			}
			if ((null==minY)
					|| (minY>sample.sampleMinY)) {
				minY=sample.sampleMinY;
			}
		}
		samplesMaxX=maxX;
		samplesMaxY=maxY;
		samplesMinX=minX;
		samplesMinY=minY;
		viewHeight=viewTop-viewBottom;
		viewWidth=viewRight-viewLeft;
	}
	
	public Graph addFunction(Function function) {
		Objects.requireNonNull(function, "function");
		List<Function> newFunctions=new ArrayList<>(functions.size()+1);
		newFunctions.addAll(functions);
		newFunctions.add(function);
		return new Graph(backgroundColor, componentHeight, componentWidth,
				newFunctions, incompleteColor, labelSize, logarithmicX,
				logarithmicY, rulerColor, samples, toolTipBackgroundColor,
				toolTipTextColor, viewBottom, viewLeft, viewRight, viewTop);
	}
	
	public Graph addSample(Sample sample) {
		Objects.requireNonNull(sample, "sample");
		List<Sample> newSamples=new ArrayList<>(samples.size()+1);
		newSamples.addAll(samples);
		newSamples.add(sample);
		return new Graph(backgroundColor, componentHeight, componentWidth,
				functions, incompleteColor, labelSize, logarithmicX,
				logarithmicY, rulerColor, newSamples, toolTipBackgroundColor,
				toolTipTextColor, viewBottom, viewLeft, viewRight, viewTop);
	}
	
	public double graphToPixelHeight(double height) {
		return height*componentHeight/viewHeight;
	}
	
	public double graphToPixelWidth(double width) {
		return width*componentWidth/viewWidth;
	}
	
	public double graphToPixelX(double xx) {
		return (xx-viewLeft)*componentWidth/viewWidth;
	}
	
	public double graphToPixelX(double left, double right) {
		return ((left+right-2.0*viewLeft))*componentWidth/(2.0*viewWidth);
	}
	
	public double graphToPixelY(double yy) {
		return componentHeight+(viewBottom-yy)*componentHeight/viewHeight;
	}
    
    public boolean isViewAuto() {
        Graph graph=setViewAuto();
        return (viewBottom==graph.viewBottom)
                && (viewLeft==graph.viewLeft)
                && (viewRight==graph.viewRight)
                && (viewTop==graph.viewTop);
    }
	
	public Graph logarithmicX(boolean logarithmicX) {
		return new Graph(backgroundColor, componentHeight, componentWidth,
				functions, incompleteColor, labelSize, logarithmicX,
				logarithmicY, rulerColor, samples, toolTipBackgroundColor,
				toolTipTextColor, viewBottom, viewLeft, viewRight, viewTop);
	}
	
	public Graph logarithmicY(boolean logarithmicY) {
		return new Graph(backgroundColor, componentHeight, componentWidth,
				functions, incompleteColor, labelSize, logarithmicX,
				logarithmicY, rulerColor, samples, toolTipBackgroundColor,
				toolTipTextColor, viewBottom, viewLeft, viewRight, viewTop);
	}
	
	public double pixelBorderX(int xx) {
		return pixelBorderX(xx, componentWidth);
	}
	
	public double pixelBorderX(int xx, int pixels) {
		return ((pixels-xx)*viewLeft+xx*viewRight)/pixels;
	}
	
	public double pixelToGraphX(double xx) {
		return viewLeft+xx*viewWidth/componentWidth;
	}
	
	public double pixelToGraphY(double yy) {
		return viewTop-yy*viewHeight/componentHeight;
	}
	
	public Graph remove(Object object) {
		List<Function> functions2=new ArrayList<>(functions.size());
		for (Function function2: functions) {
			if (function2!=object) {
				functions2.add(function2);
			}
		}
		List<Sample> samples2=new ArrayList<>(samples.size());
		for (Sample sample2: samples) {
			if (sample2!=object) {
				samples2.add(sample2);
			}
		}
		return new Graph(backgroundColor, componentHeight,
				componentWidth, functions2, incompleteColor, labelSize,
				logarithmicX, logarithmicY, rulerColor, samples2,
				toolTipBackgroundColor, toolTipTextColor, viewBottom, viewLeft,
				viewRight, viewTop);
	}
	
	public Graph replace(Function function) {
		List<Function> functions2=new ArrayList<>(functions.size()+1);
		for (Function function2: functions) {
			if (function2!=function) {
				functions2.add(function2);
			}
		}
		functions2.add(function);
		return new Graph(backgroundColor, componentHeight, componentWidth,
				functions2, incompleteColor, labelSize, logarithmicX,
				logarithmicY, rulerColor, samples, toolTipBackgroundColor,
				toolTipTextColor, viewBottom, viewLeft, viewRight, viewTop);
	}
	
	public Graph replace(Sample sample) {
		List<Sample> samples2=new ArrayList<>(samples.size()+1);
		for (Sample sample2: samples) {
			if (sample2!=sample) {
				samples2.add(sample2);
			}
		}
		samples2.add(sample);
		return new Graph(backgroundColor, componentHeight, componentWidth,
				functions, incompleteColor, labelSize, logarithmicX,
				logarithmicY, rulerColor, samples2, toolTipBackgroundColor,
				toolTipTextColor, viewBottom, viewLeft, viewRight, viewTop);
	}
	
	public Graph scale(double xScale, double yScale) {
		return scalePixels(
                0.5*componentWidth, xScale, 0.5*componentHeight, yScale);
	}
	
	public Graph scalePixels(double xCenterPixels, double xScale,
			double yCenterPixels, double yScale) {
		double cx=viewLeft+xCenterPixels*viewWidth/componentWidth;
		double cy=viewBottom+yCenterPixels*viewHeight/componentHeight;
		return setView(
				cy+(viewBottom-cy)*yScale,
				cx+(viewLeft-cx)*xScale,
				cx+(viewRight-cx)*xScale,
				cy+(viewTop-cy)*yScale);
	}
	
	public Graph setComponentSize(int height, int width) {
		if ((height==componentHeight)
				&& (width==componentWidth)) {
			return this;
		}
		return new Graph(backgroundColor, height, width, functions,
				incompleteColor, labelSize, logarithmicX, logarithmicY,
				rulerColor, samples, toolTipBackgroundColor, toolTipTextColor,
				viewBottom, viewLeft, viewRight, viewTop);
	}
	
	public Graph setView(double viewBottom, double viewLeft,
			double viewRight, double viewTop) {
		return new Graph(backgroundColor, componentHeight,
				componentWidth, functions, incompleteColor, labelSize,
				logarithmicX, logarithmicY, rulerColor, samples,
				toolTipBackgroundColor, toolTipTextColor, viewBottom, viewLeft,
				viewRight, viewTop);
	}
	
	public Graph setViewAuto() {
		if ((null==samplesMaxX)
				|| (null==samplesMaxY)
				|| (null==samplesMinX)
				|| (null==samplesMinY)
				|| (samplesMaxX<samplesMinX)
				|| (samplesMaxY<samplesMinY)) {
			return setView(0.0, 0.0, 1.0, 1.0);
		}
		final double margin=0.05;
		double dx=samplesMaxX-samplesMinX;
		double left;
		double right;
		if (0.0>=dx) {
			left=samplesMaxX-1.0;
			right=samplesMaxX+1.0;
		}
		else {
			left=samplesMinX-margin*dx;
			right=samplesMaxX+margin*dx;
		}
		double dy=samplesMaxY-samplesMinY;
		double bottom;
		double top;
		if (0.0>=dy) {
			bottom=samplesMaxY-1.0;
			top=samplesMaxY+1.0;
		}
		else {
			bottom=samplesMinY-margin*dy;
			top=samplesMaxY+margin*dy;
		}
		return setView(bottom, left, right, top);
	}
	
	public Graph translatePixels(double xPixels, double yPixels) {
		double dx=xPixels*viewWidth/componentWidth;
		double dy=yPixels*viewHeight/componentHeight;
		return setView(viewBottom+dy, viewLeft+dx, viewRight+dx, viewTop+dy);
	}
}
