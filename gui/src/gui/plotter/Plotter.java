package gui.plotter;

import gui.Gui;
import gui.graph.Function2D;
import gui.graph.Graph2D;
import gui.graph.Sample2D;
import gui.math.Sum;
import gui.ui.CloseButton;
import gui.ui.Color;
import gui.ui.ColorRenderer;
import gui.ui.Graph2DPlotter;
import gui.ui.GuiParent;
import gui.ui.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class Plotter implements GuiParent<JFrame> {
    public static final char MNEMONIC='g';
    public static final String TITLE="Graph";
    
    private class PlotterListener implements Graph2DPlotter.Listener {
        @Override
        public void graph(Graph2D graph) throws Throwable {
            autoViewButton.setEnabled(!graph.isViewAuto());
        }
        
        @Override
        public void scalePixels(double xCenterPixels, double xScale,
                double yCenterPixels, double yScale) throws Throwable {
            plotter.setGraph(plotter.getGraph().scalePixels(
                    xCenterPixels, xScale, yCenterPixels, yScale));
        }
        
        @Override
        public void translatePixels(double xPixels, double yPixels)
                throws Throwable {
            plotter.setGraph(plotter.getGraph()
                    .translatePixels(xPixels, yPixels));
        }
    }
	
	private class TableModelImpl implements TableModel {
		@Override
		public void addTableModelListener(TableModelListener listener) {
			tableModelListeners.add(listener);
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return Color.class;
				case 1:
					return String.class;
				default:
					throw new IllegalArgumentException();
			}
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return "Color";
				case 1:
					return "Name";
				default:
					throw new IllegalArgumentException();
			}
		}
		
		@Override
		public int getRowCount() {
			return samples.size();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
            Sample sample=samples.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return sample.color();
                case 1:
                    return sample.label();
                default:
                    throw new IllegalArgumentException();
            }
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public void removeTableModelListener(TableModelListener listener) {
			tableModelListeners.remove(listener);
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}
	}
    
	private final JButton autoViewButton;
    private final JFrame frame;
    final Gui gui;
    private final Graph2DPlotter plotter;
	private final Random random=new Random();
	private final List<Sample> samples=new ArrayList<>();
	private final JPanel southEastPanel;
	private final JTable table;
	private final TableModel tableModel=new TableModelImpl();
	private final Collection<TableModelListener> tableModelListeners
			=new LinkedList<>();
    
    public Plotter(Gui gui) {
		this.gui=gui;
		
        frame=new JFrame(TITLE);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
		
		JSplitPane splitPane0=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane0.setResizeWeight(0.75);
		splitPane0.setContinuousLayout(true);
		frame.getContentPane().add(splitPane0, BorderLayout.CENTER);
        
        JPanel plotterPanel=new JPanel(new BorderLayout());
		splitPane0.add(plotterPanel, JSplitPane.LEFT);
		
		JPanel plotterHeader=new JPanel(new FlowLayout(FlowLayout.CENTER));
        plotterPanel.add(plotterHeader, BorderLayout.NORTH);
		
        autoViewButton=new JButton("Auto view");
        autoViewButton.addActionListener(this::autoViewButton);
        plotterHeader.add(autoViewButton);
        
        JButton zoomViewInButton=new JButton("\u002b");
        zoomViewInButton.addActionListener(this::zoomViewInButton);
        plotterHeader.add(zoomViewInButton);
        
        JButton zoomViewOutButton=new JButton("\u2212");
        zoomViewOutButton.addActionListener(this::zoomViewOutButton);
        plotterHeader.add(zoomViewOutButton);
        
        JButton translateViewBottomButton=new JButton("\u2193");
        translateViewBottomButton.addActionListener(
                this::translateViewBottomButton);
        plotterHeader.add(translateViewBottomButton);
        
        JButton translateViewLeftButton=new JButton("\u2190");
        translateViewLeftButton.addActionListener(
                this::translateViewLeftButton);
        plotterHeader.add(translateViewLeftButton);
        
        JButton translateViewRightButton=new JButton("\u2192");
        translateViewRightButton.addActionListener(
                this::translateViewRightButton);
        plotterHeader.add(translateViewRightButton);
        
        JButton translateViewTopButton=new JButton("\u2191");
        translateViewTopButton.addActionListener(
                this::translateViewTopButton);
        plotterHeader.add(translateViewTopButton);
        
        plotter=new Graph2DPlotter(gui.executor,
                (throwable)->{
                    SwingUtilities.invokeLater(
                            ()->SwingUtils.showError(frame, throwable));
                },
                Sum::priority);
        plotter.addListener(new PlotterListener());
        plotterPanel.add(plotter, BorderLayout.CENTER);
        
		JSplitPane splitPane1=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane1.setResizeWeight(0.01);
		splitPane0.add(splitPane1, JSplitPane.RIGHT);
		
        JPanel northEastPanel=new JPanel(new BorderLayout());
        splitPane1.add(northEastPanel, JSplitPane.TOP);
        
        JPanel controlPanel=new JPanel(new BorderLayout());
        northEastPanel.add(controlPanel, BorderLayout.NORTH);
        
        JPanel closePanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlPanel.add(closePanel, BorderLayout.EAST);
        
		closePanel.add(CloseButton.create(frame));
        
        JPanel controlButtonsPanel
                =new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlPanel.add(controlButtonsPanel, BorderLayout.CENTER);
        
        JButton addSampleButton=new JButton("Add sample");
        addSampleButton.addActionListener(actionListener(
                this::addSampleButton));
        controlButtonsPanel.add(addSampleButton);
		
        table=new JTable(tableModel);
		table.getSelectionModel().addListSelectionListener(
				this::tableSelection);
		table.getColumnModel().getColumn(0)
				.setCellRenderer(new ColorRenderer());
		int colorWidth=10*table.getRowHeight();
		table.getColumnModel().getColumn(0).setMaxWidth(colorWidth);
		table.getColumnModel().getColumn(0).setMinWidth(colorWidth);
		table.getColumnModel().getColumn(0).setWidth(colorWidth);
		table.setRowHeight(table.getRowHeight()+2*new FlowLayout().getHgap());
		table.selectAll();
		northEastPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		southEastPanel=new JPanel(new BorderLayout());
		splitPane1.setContinuousLayout(true);
		splitPane1.add(southEastPanel, JSplitPane.BOTTOM);
		
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.pack();
    }
	
	void addFunction(Function2D function) {
		plotter.setGraph(
				plotter.getGraph()
						.addFunction(function)
						.setViewAuto());
	}
	
	void addSample(Sample2D sample) {
		Sample sample2=new Sample(this, sample);
		samples.add(sample2);
		plotter.setGraph(
				plotter.getGraph()
						.addSample(sample)
						.setViewAuto());
		fireTableModelListeners();
		table.setRowSelectionInterval(samples.size()-1, samples.size()-1);
	}
    
    private void addSampleButton(ActionEvent event) throws Throwable {
        JPopupMenu menu=new JPopupMenu();
        
        JMenuItem cancelItem=new JMenuItem("Cancel");
		cancelItem.addActionListener((event2)->{});
		menu.add(cancelItem);
		
		menu.add(AggregatesAddSampleProcess.menu(this));
		
		JMenuItem measureSievesItem=new JMenuItem("Measure sieves");
		measureSievesItem.addActionListener(actionListener(
				(event2)->new MeasureSieve(this).show()));
		menu.add(measureSievesItem);
        
        Point mouse=MouseInfo.getPointerInfo().getLocation();
        JComponent source=(JComponent)event.getSource();
        Point component=source.getLocationOnScreen();
        menu.show((Component)event.getSource(),
                mouse.x-component.x,
                mouse.y-component.y);
    }
    
    private void autoViewButton(ActionEvent event) {
        plotter.setGraph(plotter.getGraph().setViewAuto());
    }
	
	@Override
	public JFrame component() {
		return frame;
	}
	
	private void fireTableModelListeners() {
		tableModelListeners.forEach((listener)->
				listener.tableChanged(new TableModelEvent(tableModel)));
	}
	
	void removeFunction(Function2D function) {
		plotter.setGraph(
				plotter.getGraph()
						.remove(function.id)
						.setViewAuto());
	}
	
	void removeSample(Sample sample) {
		samples.remove(sample);
		List<Graph2D> graph=Arrays.asList(plotter.getGraph());
		sample.graphIds((id)->graph.set(0, graph.get(0).remove(id)));
		plotter.setGraph(graph.get(0).setViewAuto());
		fireTableModelListeners();
        if (0<samples.size()) {
    		table.setRowSelectionInterval(samples.size()-1, samples.size()-1);
        }
	}
	
	void replaceFunction(Function2D function) {
		plotter.setGraph(plotter.getGraph().replace(function));
		table.repaint();
	}
	
	void replaceSample(Sample2D sample) {
		plotter.setGraph(plotter.getGraph().replace(sample));
		table.repaint();
	}
	
	public Color selectNewColor() {
		return Colors.selectNew(random,
				(consumer)->samples.forEach(
						(sample)->sample.usedColors(consumer)));
	}
    
    public static void start(Gui gui) throws Throwable {
		Plotter plotter=new Plotter(gui);
		SwingUtils.show(plotter.frame);
    }
	
	private void tableSelection(ListSelectionEvent event) {
		if (null==southEastPanel) {
			return;
		}
		int selectedRow=table.getSelectedRow();
		while (0<southEastPanel.getComponentCount()) {
			southEastPanel.remove(southEastPanel.getComponentCount()-1);
		}
		if (0<=selectedRow) {
			southEastPanel.add(
                    samples.get(selectedRow).component(),
                    BorderLayout.CENTER);
		}
		southEastPanel.invalidate();
		southEastPanel.validate();
		southEastPanel.repaint();
	}
    
    private void translateViewBottomButton(ActionEvent event) {
        Graph2D graph=plotter.getGraph();
        plotter.setGraph(graph.translatePixels(
                0.0,
                -Graph2DPlotter.TRANSLATE*graph.componentHeight));
    }
    
    private void translateViewLeftButton(ActionEvent event) {
        Graph2D graph=plotter.getGraph();
        plotter.setGraph(graph.translatePixels(
                -Graph2DPlotter.TRANSLATE*graph.componentWidth,
                0.0));
    }
    
    private void translateViewRightButton(ActionEvent event) {
        Graph2D graph=plotter.getGraph();
        plotter.setGraph(graph.translatePixels(
                Graph2DPlotter.TRANSLATE*graph.componentWidth,
                0.0));
    }
    
    private void translateViewTopButton(ActionEvent event) {
        Graph2D graph=plotter.getGraph();
        plotter.setGraph(graph.translatePixels(
                0.0,
                Graph2DPlotter.TRANSLATE*graph.componentHeight));
    }
    
    private void zoomViewInButton(ActionEvent event) {
        plotter.setGraph(plotter.getGraph()
                .scale(1.0/Graph2DPlotter.SCALE, 1.0/Graph2DPlotter.SCALE));
    }
    
    private void zoomViewOutButton(ActionEvent event) {
        plotter.setGraph(plotter.getGraph()
                .scale(Graph2DPlotter.SCALE, Graph2DPlotter.SCALE));
    }
}
