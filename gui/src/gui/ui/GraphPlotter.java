package gui.ui;

import gui.graph.CheckAndPost;
import gui.graph.Function;
import gui.graph.Graph;
import gui.graph.GraphRenderer;
import gui.graph.RenderedGraph;
import gui.graph.RenderedInterval;
import gui.graph.RenderedSample;
import gui.graph.RendererDeathException;
import gui.graph.Ruler;
import gui.graph.Sample;
import gui.util.Consumer;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

public class GraphPlotter extends JComponent {
	public static final float LINE_WIDTH=1.0f;
	public static final double SAMPLE_POINT_SIZE=2.0;
    public static final double SCALE=1.1;
    public static final double TRANSLATE=0.125;
	
	private static final long serialVersionUID=0l;
	
	private class CheckAndPostImpl implements CheckAndPost {
		@Override
		public void check(Graph graph) throws RendererDeathException {
			synchronized (lock) {
				if (graph!=GraphPlotter.this.graph) {
					throw new RendererDeathException();
				}
			}
		}
		
		@Override
		public void checkAndPost(RenderedGraph graph)
				throws RendererDeathException {
			synchronized (lock) {
				if (graph.graph!=GraphPlotter.this.graph) {
					throw new RendererDeathException();
				}
				GraphPlotter.this.renderedGraph=graph;
			}
			SwingUtilities.invokeLater(GraphPlotter.this::repaint);
		}
	}
	
	public static interface Listener {
		void graph(Graph graph) throws Throwable;
		void scalePixels(double xCenterPixels, double xScale,
                double yCenterPixels, double yScale) throws Throwable;
		void translatePixels(double xPixels, double yPixels) throws Throwable;
	}
	
	private class MouseListenerImpl
			implements MouseListener, MouseMotionListener, MouseWheelListener {
		private boolean pressed;
		private int translateX;
		private int translateY;
		
		@Override
		public void mouseClicked(MouseEvent event) {
		}
		
		@Override
		public void mouseDragged(MouseEvent event) {
			if (pressed) {
				int dx=translateX-event.getX();
				int dy=event.getY()-translateY;
				if ((0!=dx)
						|| (0!=dy)) {
					fireListeners((listener)->
                            listener.translatePixels(dx, dy));
					translateX=event.getX();
					translateY=event.getY();
				}
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent event) {
			pressed=false;
			setToolTipText("");
		}
		
		@Override
		public void mouseExited(MouseEvent event) {
			pressed=false;
			setToolTipText(null);
		}
		
		@Override
		public void mouseMoved(MouseEvent event) {
		}
		
		@Override
		public void mousePressed(MouseEvent event) {
			if (MouseEvent.BUTTON1==event.getButton()) {
				pressed=true;
				translateX=event.getX();
				translateY=event.getY();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent event) {
			if (pressed
					&& (MouseEvent.BUTTON1==event.getButton())) {
				try {
					mouseDragged(event);
				}
				finally {
					pressed=false;
				}
			}
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent event) {
			double scale=Math.pow(SCALE, event.getWheelRotation());
			if (1.0!=scale) {
				double xx=event.getX();
				double yy=getHeight()-event.getY()-1;
				fireListeners((listener)->
                        listener.scalePixels(xx, scale, yy, scale));
			}
		}
	}
	
	private class ToolTip {
		private Graph graph;
		private Point location;
		private int mouseX;
		private int mouseY;
		private String text;
		
		public Point location(MouseEvent event) {
			synchronized (lock) {
				reset(event);
				return location;
			}
		}
		
		private void reset(MouseEvent event) {
			synchronized (lock) {
				if ((this.graph==GraphPlotter.this.graph)
						&& (mouseX==event.getX())
						&& (mouseY==event.getY())) {
					return;
				}
				this.graph=GraphPlotter.this.graph;
				mouseX=event.getX();
				mouseY=event.getY();
				if ((null==graph.toolTipBackgroundColor)
						|| (null==graph.toolTipTextColor)) {
					location=null;
					text=null;
					return;
				}
				location=new Point(mouseX, mouseY);
				class Row {
					public final Color color;
					public final String label;
					public final double xx;
					public final double yy;
					
					public Row(Color color, String label, double xx,
							double yy) {
						this.color=color;
						this.label=label;
						this.xx=xx;
						this.yy=yy;
					}
				}
				double graphX=graph.pixelToGraphX(mouseX);
				double graphY=graph.pixelToGraphY(mouseY);
				List<Row> rows=new ArrayList<>();
				rows.add(new Row(graph.toolTipBackgroundColor,
						"kurzor", graphX, graphY));
				for (Sample sample: graph.samples) {
					int ceilingIndex=sample.ceilingIndex(
							Math.round(Math.max(0.0, Math.ceil(graphX))));
					int floorIndex=sample.floorIndex(
							Math.round(Math.max(0.0, Math.floor(graphX))));
					int sampleIndex;
					if (0>ceilingIndex) {
						if (0>floorIndex) {
							continue;
						}
						else {
							sampleIndex=floorIndex;
						}
					}
					else {
						if (0>floorIndex) {
							sampleIndex=ceilingIndex;
						}
						else {
							long ceilingX=sample.xx(ceilingIndex);
							long floorX=sample.xx(floorIndex);
							if (Math.abs(ceilingX-graphX)
									<=Math.abs(floorX-graphX)) {
								sampleIndex=ceilingIndex;
							}
							else {
								sampleIndex=floorIndex;
							}
						}
					}
					rows.add(new Row(sample.pointColor, sample.label,
							sample.xx(sampleIndex), sample.yy(sampleIndex)));
				}
				for (Function function: graph.functions) {
					double yy=function.function.valueAt(graphX);
					if (Double.isFinite(yy)) {
						rows.add(new Row(function.color, function.label,
								graphX, yy));
					}
				}
				if (rows.isEmpty()) {
					text=null;
				}
				else {
					StringBuilder sb=new StringBuilder();
					sb.append("<html><body><table style=\"background-color: ");
					sb.append(graph.toolTipBackgroundColor.html());
					sb.append(";\">");
					sb.append(
							"<tr><th></th><th></th><th><span style=\"color: ");
					sb.append(graph.toolTipTextColor.html());
					sb.append(";\">x</span></th><th><span style=\"color: ");
					sb.append(graph.toolTipTextColor.html());
					sb.append(";\">y</span></th></tr>");
					for (Row row: rows) {
						sb.append("<tr><td><span style=\"background: ");
						sb.append(row.color.html());
						sb.append(";\">&nbsp;&nbsp;&nbsp;&nbsp;</span></td>");
						sb.append("<th align=\"left\"><span style=\"color: ");
						sb.append(graph.toolTipTextColor);
						sb.append(";\">");
						sb.append(row.label);
						sb.append("</span></th><td align=\"right\">");
						sb.append("<span style=\"color: ");
						sb.append(graph.toolTipTextColor.html());
						sb.append(";\">");
						sb.append(String.format("%1$,f", row.xx));
						sb.append("</span></td><td align=\"right\">");
						sb.append("<span style=\"color: ");
						sb.append(graph.toolTipTextColor.html());
						sb.append(";\">");
						sb.append(String.format("%1$,f", row.yy));
						sb.append("</span></td></tr>");
					}
					sb.append("</table></body></html>");
					text=sb.toString();
				}
			}
		}
		
		public String text(MouseEvent event) {
			synchronized (lock) {
				reset(event);
				return text;
			}
		}
	}
	
	private final Executor executor;
	private final Consumer<Throwable> logger;
	private Graph graph=Graph.EMPTY;
	private final List<Listener> listeners=new ArrayList<>();
	private final Object lock=new Object();
	private RenderedGraph renderedGraph;
	private final ToolTip toolTip=new ToolTip();
	
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public GraphPlotter(Executor executor, Consumer<Throwable> logger) {
        this.executor=executor;
		this.logger=logger;
		MouseListenerImpl mouseListener=new MouseListenerImpl();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);
		setToolTipText("");
	}
	
	public void addListener(Listener listener) {
		synchronized (lock) {
			listeners.add(listener);
		}
	}
	
	@Override
	public JToolTip createToolTip() {
		JToolTip toolTip2=super.createToolTip();
		Color backgroundColor;
		synchronized (lock) {
			backgroundColor=graph.toolTipBackgroundColor;
		}
		if (null!=backgroundColor) {
			toolTip2.setBackground(backgroundColor.awt());
		}
		return toolTip2;
	}
	
	private void fireListeners(Consumer<Listener> handler) {
		List<Listener> listeners2;
		synchronized (lock) {
			listeners2=new ArrayList<>(listeners);
		}
		listeners2.forEach((listener)->{
			try {
				handler.consume(listener);
			}
			catch (Throwable throwable) {
				try {
					logger.consume(throwable);
				}
				catch (Throwable throwable2) {
				}
			}
		});
	}
	
	public Graph getGraph() {
		synchronized (lock) {
			return graph;
		}
	}
	
	@Override
	public Point getToolTipLocation(MouseEvent event) {
		return toolTip.location(event);
	}
	
	@Override
	public String getToolTipText(MouseEvent event) {
		return toolTip.text(event);
	}
	
	private static void paint(Graph graph, RenderedGraph renderedGraph,
			Graphics2D graphics) {
		boolean completed=false;
		if (null!=renderedGraph) {
			completed=renderedGraph.completed;
			graphics.setColor(graph.rulerColor.awt());
			graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
					(int)Math.round(graph.labelSize)));
			for (int ii=0; renderedGraph.rulers.size()>ii; ++ii) {
				paintRuler(graph, graphics, renderedGraph.rulers.get(ii));
			}
			for (int ii=0; renderedGraph.functions.size()>ii; ++ii) {
				Function function=graph.functions.get(ii);
				paintLine(graphics, function.color, null,
						renderedGraph.functions.get(ii));
			}
			for (int ii=0; renderedGraph.samples.size()>ii; ++ii) {
				Sample sample=graph.samples.get(ii);
				switch (sample.plotType) {
					case BARS:
						paintBars(graphics, sample.pointColor,
								renderedGraph.samples.get(ii));
						break;
					case LINE:
						paintLine(graphics, sample.lineColor,
								sample.pointColor,
								renderedGraph.samples.get(ii));
						break;
					default:
						throw new IllegalStateException(
								sample.plotType.toString());
				}
			}
		}
		if (!completed) {
			paintIncomplete(graph.incompleteColor, graphics,
					graph.componentHeight, graph.componentWidth);
		}
	}
	
	private static void paintBars(Graphics2D graphics, Color pointColor,
			RenderedSample sample) {
		graphics.setColor(pointColor.awt());
		graphics.setStroke(new BasicStroke(LINE_WIDTH));
		for (int jj=0; sample.intervals.size()>jj; ++jj) {
			RenderedInterval interval=sample.intervals.get(jj);
			for (int ii=0; interval.size()>ii; ++ii) {
				double maxValue=interval.maxValue(ii);
				double minValue=interval.minValue(ii);
				double xx=interval.xx(ii);
				if (maxValue==minValue) {
					graphics.fill(new Ellipse2D.Double(
							xx-0.5*SAMPLE_POINT_SIZE,
							maxValue-0.5*SAMPLE_POINT_SIZE,
							SAMPLE_POINT_SIZE,
							SAMPLE_POINT_SIZE));
				}
				else {
					graphics.draw(new Line2D.Double(
							xx, maxValue, xx, minValue));
				}
			}
		}
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graph graph2;
		Graph graph3;
		RenderedGraph renderedGraph2;
		int height=getHeight();
		int width=getWidth();
		synchronized (lock) {
			graph2=this.graph;
			renderedGraph2=this.renderedGraph;
			graph3=graph2.setComponentSize(height, width);
			if (graph3!=graph2) {
				setGraph(graph3);
			}
		}
		Graphics2D graphics2d=(Graphics2D)(graphics.create());
		try {
			graphics2d.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2d.setRenderingHint(
					RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_PURE);
			graphics2d.setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics2d.setColor(graph2.backgroundColor.awt());
			graphics2d.fillRect(0, 0, width, height);
			if (graph3!=graph2) {
				paintIncomplete(
						graph2.incompleteColor, graphics2d, height, width);
			}
			else {
				paint(graph2,
						((null!=renderedGraph2)
										&& (renderedGraph2.graph==graph2))
								?renderedGraph2
								:null,
						graphics2d);
			}
		}
		finally {
			graphics2d.dispose();
		}
		SwingUtilities.invokeLater(()->{
			synchronized (lock) {
				if ((graph2==this.graph)
						&& (renderedGraph2==this.renderedGraph)) {
					return;
				}
			}
			repaint();
		});
	}
	
	private static void paintIncomplete(Color color, Graphics2D graphics,
			int height, int width) {
		int radius=Math.min(height, width)/4;
		graphics.setColor(color.awt());
		graphics.setStroke(new BasicStroke(radius/4));
		graphics.drawOval(
				(width-radius)/2,
				(height-radius)/2,
				radius,
				radius);
	}
	
	private static void paintLine(Graphics2D graphics, Color lineColor,
			Color pointColor, RenderedSample sample) {
		graphics.setColor(lineColor.awt());
		graphics.setStroke(new BasicStroke(LINE_WIDTH));
		int maxSamples=1;
		for (int jj=0; sample.intervals.size()>jj; ++jj) {
			maxSamples=Math.max(maxSamples, 4*sample.intervals.size());
		}
		Path2D.Double path=new Path2D.Double(Path2D.WIND_NON_ZERO, maxSamples);
		for (int jj=0; sample.intervals.size()>jj; ++jj) {
			RenderedInterval interval=sample.intervals.get(jj);
			path.reset();
			boolean first=true;
			for (int ii=0; interval.size()>ii; ++ii) {
				double leftValue=interval.leftValue(ii);
				double maxValue=interval.maxValue(ii);
				double minValue=interval.minValue(ii);
				double rightValue=interval.rightValue(ii);
				double xx=interval.xx(ii);
				if (null==pointColor) {
					double yy=0.25*(leftValue+maxValue+minValue+rightValue);
					if (first) {
						first=false;
						path.moveTo(xx, yy);
					}
					else {
						path.lineTo(xx, yy);
					}
				}
				else {
					if (first) {
						first=false;
						path.moveTo(xx, leftValue);
					}
					else {
						path.lineTo(xx, leftValue);
					}
					path.lineTo(xx, minValue);
					path.lineTo(xx, maxValue);
					path.lineTo(xx, rightValue);
				}
			}
			graphics.draw(path);
		}
		if (null!=pointColor) {
			graphics.setColor(pointColor.awt());
			for (int jj=0; sample.intervals.size()>jj; ++jj) {
				RenderedInterval interval=sample.intervals.get(jj);
				for (int ii=0; interval.size()>ii; ++ii) {
					double maxValue=interval.maxValue(ii);
					double minValue=interval.minValue(ii);
					double xx=interval.xx(ii);
					if (maxValue==minValue) {
						graphics.fill(new Ellipse2D.Double(
								xx-0.5*SAMPLE_POINT_SIZE,
								maxValue-0.5*SAMPLE_POINT_SIZE,
								SAMPLE_POINT_SIZE,
								SAMPLE_POINT_SIZE));
					}
					else {
						graphics.draw(new Line2D.Double(
								xx, maxValue, xx, minValue));
					}
				}
			}
		}
	}
	
	private static void paintRuler(Graph graph, Graphics2D graphics,
			Ruler ruler) {
		graphics.setStroke(new BasicStroke(
				(float)(ruler.thinness.lineWidthFactor*LINE_WIDTH)));
		if (ruler.vertical) {
			graphics.draw(new Line2D.Double(ruler.pixelLevel, -1.0,
					ruler.pixelLevel, graph.componentHeight+1.0));
		}
		else {
			graphics.draw(new Line2D.Double(-1.0, ruler.pixelLevel,
					graph.componentWidth+1.0, ruler.pixelLevel));
		}
		if (null!=ruler.label) {
			Rectangle2D bounds=graphics.getFontMetrics()
					.getStringBounds(ruler.label, graphics);
			if (ruler.vertical) {
				graphics.drawString(ruler.label,
						(float)(ruler.pixelLevel+1.0),
						(float)(-bounds.getY()));
				graphics.drawString(ruler.label,
						(float)(ruler.pixelLevel+1.0),
						(float)(graph.componentHeight));
			}
			else {
				graphics.drawString(ruler.label,
						(float)(0.0),
						(float)(ruler.pixelLevel-2.0));
				graphics.drawString(ruler.label,
						(float)(graph.componentWidth-bounds.getWidth()),
						(float)(ruler.pixelLevel-2.0));
			}
		}
	}
	
	public void removeListener(Listener listener) {
		synchronized (lock) {
			listeners.remove(listener);
		}
	}
	
	public void setGraph(Graph newGraph) {
		Objects.requireNonNull(graph, "graph");
        synchronized (lock) {
            if (this.graph==newGraph) {
                return;
            }
            this.graph=newGraph;
            this.renderedGraph=null;
        }
		SwingUtilities.invokeLater(()->{
			Graph newGraph2
                    =newGraph.setComponentSize(getHeight(), getWidth());
			synchronized (lock) {
				if (this.graph!=newGraph) {
					return;
				}
				this.graph=newGraph2;
				renderedGraph=null;
			}
			repaint();
            executor.execute(
					new GraphRenderer(new CheckAndPostImpl(), newGraph2));
			fireListeners((listener)->listener.graph(newGraph2));
		});
	}
}
