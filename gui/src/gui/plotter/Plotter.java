package gui.plotter;

import gui.Session;
import gui.graph.Function;
import gui.graph.Graph;
import gui.graph.Sample;
import gui.ui.CloseButton;
import gui.ui.Color;
import gui.ui.ColorRenderer;
import gui.ui.GraphPlotter;
import gui.ui.GuiWindow;
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

public class Plotter extends GuiWindow<JFrame> {
    public static final char MNEMONIC='g';
    public static final String TITLE="Graph";
    
    private class PlotterListener implements GraphPlotter.Listener {
        @Override
        public void graph(Graph graph) throws Throwable {
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
			return samplePanels.size();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
            SamplePanel sample=samplePanels.get(rowIndex);
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
    private final GraphPlotter plotter;
	private final Random random=new Random();
	private final List<SamplePanel> samplePanels=new ArrayList<>();
	private final JPanel southEastPanel;
	private final JTable table;
	private final TableModel tableModel=new TableModelImpl();
	private final Collection<TableModelListener> tableModelListeners
			=new LinkedList<>();
    
    public Plotter(Session session) {
		super(session);
		
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
        
        plotter=new GraphPlotter(session.executor,
                (throwable)->{
                    SwingUtilities.invokeLater(
                            ()->SwingUtils.showError(frame, throwable));
                });
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
	
	void addFunction(Function function) {
		plotter.setGraph(
				plotter.getGraph()
						.addFunction(function)
						.setViewAuto());
	}
	
	void addSample(Sample sample) {
		SamplePanel samplePanel=new SamplePanel(this, sample);
		samplePanels.add(samplePanel);
		plotter.setGraph(
				plotter.getGraph()
						.addSample(sample)
						.setViewAuto());
		fireTableModelListeners();
		table.setRowSelectionInterval(samplePanels.size()-1, samplePanels.size()-1);
	}
    
    private void addSampleButton(ActionEvent event) throws Throwable {
        JPopupMenu menu=new JPopupMenu();
        
        JMenuItem cancelItem=new JMenuItem("Cancel");
		cancelItem.addActionListener((event2)->{});
		menu.add(cancelItem);
		
		JMenuItem loadSampleItem=new JMenuItem("Load sample");
		loadSampleItem.addActionListener(actionListener(this::loadSample));
		menu.add(loadSampleItem);
		
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
	
	private void fireTableModelListeners() {
		tableModelListeners.forEach((listener)->
				listener.tableChanged(new TableModelEvent(tableModel)));
	}
	
	private void loadSample(ActionEvent event) throws Throwable {
		LoadSampleProcess.start(this);
	}
	
	void removeFunction(Function function) {
		plotter.setGraph(
				plotter.getGraph()
						.remove(function.id)
						.setViewAuto());
	}
	
	void removeSample(SamplePanel samplePanel) {
		samplePanels.remove(samplePanel);
		List<Graph> graph=Arrays.asList(plotter.getGraph());
		samplePanel.graphIds((id)->graph.set(0, graph.get(0).remove(id)));
		plotter.setGraph(graph.get(0).setViewAuto());
		fireTableModelListeners();
        if (0<samplePanels.size()) {
    		table.setRowSelectionInterval(samplePanels.size()-1, samplePanels.size()-1);
        }
	}
	
	void replaceFunction(Function function) {
		plotter.setGraph(plotter.getGraph().replace(function));
		table.repaint();
	}
	
	void replaceSample(Sample sample) {
		plotter.setGraph(plotter.getGraph().replace(sample));
		table.repaint();
	}
	
	public Color selectNewColor() {
		return Colors.selectNew(random,
				(consumer)->samplePanels.forEach(
						(sample)->sample.usedColors(consumer)));
	}
    
    public static void start(Session session) throws Throwable {
		new Plotter(session)
				.show();
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
                    samplePanels.get(selectedRow).component(),
                    BorderLayout.CENTER);
		}
		southEastPanel.invalidate();
		southEastPanel.validate();
		southEastPanel.repaint();
	}
    
    private void translateViewBottomButton(ActionEvent event) {
        Graph graph=plotter.getGraph();
        plotter.setGraph(graph.translatePixels(
                0.0,
                -GraphPlotter.TRANSLATE*graph.componentHeight));
    }
    
    private void translateViewLeftButton(ActionEvent event) {
        Graph graph=plotter.getGraph();
        plotter.setGraph(graph.translatePixels(
                -GraphPlotter.TRANSLATE*graph.componentWidth,
                0.0));
    }
    
    private void translateViewRightButton(ActionEvent event) {
        Graph graph=plotter.getGraph();
        plotter.setGraph(graph.translatePixels(
                GraphPlotter.TRANSLATE*graph.componentWidth,
                0.0));
    }
    
    private void translateViewTopButton(ActionEvent event) {
        Graph graph=plotter.getGraph();
        plotter.setGraph(graph.translatePixels(
                0.0,
                GraphPlotter.TRANSLATE*graph.componentHeight));
    }
	
	@Override
	public JFrame window() {
		return frame;
	}
    
    private void zoomViewInButton(ActionEvent event) {
        plotter.setGraph(plotter.getGraph()
                .scale(1.0/GraphPlotter.SCALE, 1.0/GraphPlotter.SCALE));
    }
    
    private void zoomViewOutButton(ActionEvent event) {
        plotter.setGraph(plotter.getGraph()
                .scale(GraphPlotter.SCALE, GraphPlotter.SCALE));
    }
}
