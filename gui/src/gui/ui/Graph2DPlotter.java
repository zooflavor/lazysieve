package gui.ui;

import gui.graph.Function2D;
import gui.graph.Graph2D;
import gui.graph.Graph2DRenderer;
import gui.graph.RenderedGraph2D;
import gui.graph.RenderedSample2D;
import gui.graph.RendererDeathException;
import gui.graph.Ruler;
import gui.graph.Sample2D;
import gui.math.Sum;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

public class Graph2DPlotter extends JComponent {
	public static final float LINE_WIDTH=1.0f;
	public static final double SAMPLE_POINT_SIZE=2.0;
    public static final double SCALE=1.1;
    public static final double TRANSLATE=0.125;
	
	private static final long serialVersionUID=0l;
	
	public static interface Listener {
		void graph(Graph2D graph) throws Throwable;
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
		private Graph2D graph;
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
				if ((this.graph==Graph2DPlotter.this.graph)
						&& (mouseX==event.getX())
						&& (mouseY==event.getY())) {
					return;
				}
				this.graph=Graph2DPlotter.this.graph;
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
						"cursor", graphX, graphY));
				for (Sample2D sample: graph.samples) {
					if (null==sample.toolTipColor) {
						continue;
					}
					Double ceilingX=sample.sample.ceilingKey(graphX);
					Double floorX=sample.sample.floorKey(graphX);
					Double sampleX;
					if (null==ceilingX) {
						if (null==floorX) {
							continue;
						}
						else {
							sampleX=floorX;
						}
					}
					else {
						if (null==floorX) {
							sampleX=ceilingX;
						}
						else {
							if (Math.abs(ceilingX-graphX)
									<=Math.abs(floorX-graphX)) {
								sampleX=ceilingX;
							}
							else {
								sampleX=floorX;
							}
						}
					}
					rows.add(new Row(sample.toolTipColor, sample.label,
							sampleX, sample.sample.get(sampleX)));
				}
				for (Function2D function: graph.functions) {
					if (null==function.toolTipColor) {
						continue;
					}
					Double yy=function.function.apply(graphX);
					if (null!=yy) {
						rows.add(new Row(function.toolTipColor, function.label,
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
	private Graph2D graph=Graph2D.EMPTY;
	private final List<Listener> listeners=new ArrayList<>();
	private final Object lock=new Object();
	private RenderedGraph2D renderedGraph;
	private final Supplier<Sum> sumFactory;
	private final ToolTip toolTip=new ToolTip();
	
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Graph2DPlotter(Executor executor, Consumer<Throwable> logger,
			Supplier<Sum> sumFactory) {
        this.executor=executor;
		this.logger=logger;
		this.sumFactory=sumFactory;
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
	
	private void fireListeners(EventHandler<Listener> handler) {
		List<Listener> listeners2;
		synchronized (lock) {
			listeners2=new ArrayList<>(listeners);
		}
		listeners2.forEach((listener)->{
			try {
				handler.handle(listener);
			}
			catch (Throwable throwable) {
				logger.accept(throwable);
			}
		});
	}
	
	public Graph2D getGraph() {
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
	
	private static void paint(Graph2D graph, RenderedGraph2D renderedGraph,
			Graphics2D graphics) {
		graphics.setColor(graph.backgroundColor.awt());
		graphics.fillRect(0, 0, graph.componentWidth, graph.componentHeight);
		if (null==renderedGraph) {
			return;
		}
		graphics.setColor(graph.rulerColor.awt());
		graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
				(int)Math.round(graph.labelSize)));
		for (int ii=0; renderedGraph.rulers.size()>ii; ++ii) {
			paintRuler(graph, graphics, renderedGraph.rulers.get(ii));
		}
		for (int ii=0; renderedGraph.samples.size()>ii; ++ii) {
			Sample2D sample=graph.samples.get(ii);
			RenderedSample2D renderedSample=renderedGraph.samples.get(ii);
			paint(graphics, sample.lineColor, sample.pointColor,
					renderedSample);
		}
		for (int ii=0; renderedGraph.functions.size()>ii; ++ii) {
			Function2D function=graph.functions.get(ii);
			RenderedSample2D renderedSample=renderedGraph.functions.get(ii);
			paint(graphics, function.color, null, renderedSample);
		}
	}
	
	private static void paint(Graphics2D graphics,
			Color lineColor, Color pointColor, RenderedSample2D sample) {
		graphics.setColor(lineColor.awt());
		graphics.setStroke(new BasicStroke(LINE_WIDTH));
		boolean first=true;
		Path2D.Double path=new Path2D.Double(
				Path2D.WIND_NON_ZERO, sample.sample.size());
		for (Map.Entry<Double, RenderedSample2D.Box> entry:
				sample.sample.entrySet()) {
			double xx=entry.getKey();
			RenderedSample2D.Box yy=entry.getValue();
			if (first) {
				first=false;
				path.moveTo(xx, yy.mean);
			}
			else {
				path.lineTo(xx, yy.mean);
			}
		}
		graphics.draw(path);
		if (null!=pointColor) {
			graphics.setColor(pointColor.awt());
			sample.sample.forEach((xx, yy)->{
				graphics.fill(new Ellipse2D.Double(
						xx-0.5*SAMPLE_POINT_SIZE,
						yy.mean-0.5*SAMPLE_POINT_SIZE,
						SAMPLE_POINT_SIZE,
						SAMPLE_POINT_SIZE));
				if (yy.max!=yy.min) {
					graphics.fill(new Ellipse2D.Double(
							xx-0.5*SAMPLE_POINT_SIZE,
							yy.max-0.5*SAMPLE_POINT_SIZE,
							SAMPLE_POINT_SIZE,
							SAMPLE_POINT_SIZE));
					graphics.fill(new Ellipse2D.Double(
							xx-0.5*SAMPLE_POINT_SIZE,
							yy.min-0.5*SAMPLE_POINT_SIZE,
							SAMPLE_POINT_SIZE,
							SAMPLE_POINT_SIZE));
					graphics.draw(new Line2D.Double(xx, yy.max, xx, yy.min));
				}
			});
		}
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graph2D graph2;
		RenderedGraph2D renderedGraph2;
		synchronized (lock) {
			graph2=this.graph;
			renderedGraph2=this.renderedGraph;
		}
		int height=getHeight();
		int width=getWidth();
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
			if ((graph2.componentHeight!=height)
					|| (graph2.componentWidth!=width)) {
				graphics2d.setColor(graph2.backgroundColor.awt());
				graphics2d.fillRect(0, 0, width, height);
				rerenderGraph();
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
	
	private static void paintRuler(Graph2D graph, Graphics2D graphics,
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
	
	private void rendererCheckAndPost(RenderedGraph2D renderedGraph2)
			throws RendererDeathException {
		synchronized (lock) {
			if (renderedGraph2.graph!=this.graph) {
				throw new RendererDeathException();
			}
			this.renderedGraph=renderedGraph2;
		}
		SwingUtilities.invokeLater(this::repaint);
	}
	
	public void rerenderGraph() {
		setGraph(getGraph());
	}
	
	public void setGraph(Graph2D newGraph) {
		Objects.requireNonNull(graph, "graph");
		SwingUtilities.invokeLater(()->{
			Graph2D graph2=newGraph.setComponentSize(getHeight(), getWidth());
			synchronized (lock) {
				if (this.graph==graph2) {
					return;
				}
				this.graph=graph2;
				renderedGraph=null;
			}
			repaint();
            executor.execute(new Graph2DRenderer(
                    this::rendererCheckAndPost, graph2, sumFactory));
			fireListeners((listener)->listener.graph(graph2));
		});
	}
}
