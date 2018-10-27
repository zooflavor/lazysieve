package gui.plotter;

import gui.graph.Function;
import gui.graph.Sample;
import gui.math.LinearCombinationFunction;
import gui.math.RealFunction;
import gui.ui.Color;
import gui.ui.ColorRenderer;
import gui.ui.DoubleRenderer;
import gui.ui.SwingUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

final class SamplePanel {
	private static class Approximation {
		private final LinearCombinationFunction approximation;
		private final double error;
		private Function function;
		
		public Approximation(LinearCombinationFunction approximation,
				double error, Function function) {
			this.approximation=approximation;
			this.error=error;
			this.function=function;
		}
	}
	
	private class ApproximationsModel implements TableModel {
		@Override
		public void addTableModelListener(TableModelListener listener) {
			approximationsTableListeners.add(listener);
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return Color.class;
				case 1:
					return String.class;
				case 2:
					return Double.class;
				default:
					throw new IllegalArgumentException();
			}
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return "Color";
				case 1:
					return "Approximation";
				case 2:
					return "Sum(error^2)";
				default:
					throw new IllegalArgumentException();
			}
		}
		
		@Override
		public int getRowCount() {
			return approximations.size();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Approximation approximation=approximations.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return approximation.function.color;
				case 1:
					return approximation.function.label;
				case 2:
					return approximation.error;
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
			approximationsTableListeners.remove(listener);
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}
	}
	
	private class CoefficientsModel implements TableModel {
		@Override
		public void addTableModelListener(TableModelListener listener) {
			coefficientsTableListeners.add(listener);
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return String.class;
				case 1:
					return Double.class;
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
					return "Function";
				case 1:
					return "Coefficient";
				default:
					throw new IllegalArgumentException();
			}
		}
		
		@Override
		public int getRowCount() {
			int selected=approximationsTable.getSelectedRow();
			if (0<=selected) {
				LinearCombinationFunction function
						=approximations.get(selected).approximation;
				return approximations.get(selected).approximation
						.coefficients.size();
			}
			return 0;
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			int selected=approximationsTable.getSelectedRow();
			if (0<=selected) {
				LinearCombinationFunction function
						=approximations.get(selected).approximation;
				switch (columnIndex) {
					case 0:
						return function.functions.get(rowIndex).toString();
					case 1:
						return function.coefficients.get(rowIndex);
					default:
						throw new IllegalArgumentException();
				}
			}
			return 0;
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public void removeTableModelListener(TableModelListener listener) {
			coefficientsTableListeners.remove(listener);
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}
	}
	
	private static class ComboModel implements ComboBoxModel<Color> {
		private Color selected;
		
		public ComboModel(Color selected) {
			this.selected=selected;
		}
		
		@Override
		public void addListDataListener(ListDataListener listener) {
		}
		
		@Override
		public Color getElementAt(int index) {
			return Colors.GRAPHS.get(index);
		}
		
		@Override
		public Object getSelectedItem() {
			return selected;
		}
		
		@Override
		public int getSize() {
			return Colors.GRAPHS.size();
		}
		
		@Override
		public void removeListDataListener(ListDataListener listener) {
		}
		
		@Override
		public void setSelectedItem(Object anItem) {
			selected=(Color)anItem;
		}
	}
	
	private final JComboBox<Color> approximationColor;
	private final List<Approximation> approximations=new ArrayList<>();
	private final JTable approximationsTable;
	private final TableModel approximationsTableModel
			=new ApproximationsModel();
	private final Collection<TableModelListener> approximationsTableListeners
			=new LinkedList<>();
	private final JTable coefficientsTable;
	private final TableModel coefficientsTableModel
			=new CoefficientsModel();
	private final Collection<TableModelListener> coefficientsTableListeners
			=new LinkedList<>();
	private final JPanel panel;
	final Plotter plotter;
	private final JButton removeApproximationButton;
	private Sample sample;
	private final JComboBox<Color> sampleColor;
	
	public SamplePanel(Plotter plotter, Sample sample) {
		this.plotter=plotter;
		this.sample=sample;
		
		panel=new JPanel();
		panel.setLayout(new BorderLayout());
		
		JSplitPane splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		panel.add(splitPane, BorderLayout.CENTER);
		
		JPanel topPanel=new JPanel(new BorderLayout());
		splitPane.add(topPanel, JSplitPane.TOP);
		
		JPanel northPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		topPanel.add(northPanel, BorderLayout.NORTH);
		
		JButton saveSampleButton=new JButton("Save");
		saveSampleButton.addActionListener(this::saveSampleButton);
		northPanel.add(saveSampleButton);
		
		JButton removeSampleButton=new JButton("Remove");
		removeSampleButton.addActionListener(this::removeSampleButton);
		northPanel.add(removeSampleButton);
		
		sampleColor=new JComboBox<>(new ComboModel(color()));
		sampleColor.setRenderer(new ColorRenderer());
		sampleColor.addActionListener(this::sampleColorChanged);
		northPanel.add(sampleColor);
		
		JButton approximateButton=new JButton("Approximate");
		approximateButton.addActionListener(this::approximateButton);
		northPanel.add(approximateButton);
		
		approximationsTable=new JTable(approximationsTableModel);
		approximationsTable.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		approximationsTable.getSelectionModel().addListSelectionListener(
				this::approximationsTableSelection);
		approximationsTable.getColumnModel().getColumn(0)
				.setCellRenderer(new ColorRenderer());
		int colorWidth=10*approximationsTable.getRowHeight();
		approximationsTable.getColumnModel().getColumn(0)
				.setMaxWidth(colorWidth);
		approximationsTable.getColumnModel().getColumn(0)
				.setMinWidth(colorWidth);
		approximationsTable.getColumnModel().getColumn(0)
				.setWidth(colorWidth);
		approximationsTable.getColumnModel().getColumn(2)
				.setCellRenderer(new DoubleRenderer());
		approximationsTable.setRowHeight(
				approximationsTable.getRowHeight()
						+2*new FlowLayout().getHgap());
		topPanel.add(new JScrollPane(approximationsTable),
				BorderLayout.CENTER);
		
		JPanel bottomPanel=new JPanel(new BorderLayout());
		splitPane.add(bottomPanel, JSplitPane.BOTTOM);
		
		JPanel approximationHeaderPanel
				=new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPanel.add(approximationHeaderPanel, BorderLayout.NORTH);
		
		removeApproximationButton=new JButton("Remove");
		removeApproximationButton.addActionListener(
				this::removeApproximationButton);
		removeApproximationButton.setEnabled(false);
		approximationHeaderPanel.add(removeApproximationButton);
		
		approximationColor=new JComboBox<>(new ComboModel(null));
		approximationColor.setRenderer(new ColorRenderer());
		approximationColor.setEnabled(false);
		approximationColor.addActionListener(this::approximationColorChanged);
		approximationHeaderPanel.add(approximationColor);
		
		coefficientsTable=new JTable(coefficientsTableModel);
		coefficientsTable.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		coefficientsTable.getColumnModel().getColumn(1)
				.setCellRenderer(new DoubleRenderer());
		coefficientsTable.setRowHeight(
				coefficientsTable.getRowHeight()
						+2*new FlowLayout().getHgap());
		bottomPanel.add(new JScrollPane(coefficientsTable),
				BorderLayout.CENTER);
	}
	
	private void approximateButton(ActionEvent event) {
		try {
			new FunctionSelector(this::approximateFunctions, plotter)
					.start();
		}
		catch (Throwable throwable) {
			SwingUtils.showError(plotter.window(), throwable);
		}
	}
	
	private void approximateFunctions(List<RealFunction> functions)
			throws Throwable {
		if (functions.isEmpty()) {
			return;
		}
		new ApproximationProcess(functions, this)
				.start(plotter.session.executor);
	}
	
	void approximation(double error, LinearCombinationFunction function)
			throws Throwable {
		Color color=plotter.selectNewColor();
		StringBuilder label=new StringBuilder();
		for (RealFunction function2: function.functions) {
			if (0<label.length()) {
				label.append("+");
			}
			label.append(function2);
		}
		Approximation approximation=new Approximation(
				function,
				error,
				new Function(color, function, new Object(), label.toString(),
						color));
		approximations.add(approximation);
		plotter.addFunction(approximation.function);
		fireApproximationsTableListeners();
	}
	
	private void approximationColorChanged(ActionEvent event) {
		if (approximationColor.isEnabled()) {
			int selected=approximationsTable.getSelectedRow();
			if (0<=selected) {
				Approximation approximation=approximations.get(selected);
				Color color=(Color)approximationColor.getSelectedItem();
				approximation.function
						=approximation.function.setColors(color, color);
				plotter.replaceFunction(approximation.function);
			}
		}
	}
	
	private void approximationsTableSelection(ListSelectionEvent event) {
		int selected=approximationsTable.getSelectedRow();
		approximationColor.setEnabled(false);
		if (0>selected) {
			approximationColor.setSelectedItem(null);
			removeApproximationButton.setEnabled(false);
		}
		else {
			approximationColor.setSelectedItem(
					approximations.get(selected).function.color);
			approximationColor.setEnabled(true);
			removeApproximationButton.setEnabled(true);
		}
		fireCoeffecientsTableListeners();
	}
	
	public Color color() {
		return sample.pointColor;
	}
	
	public JComponent component() {
		return panel;
	}
	
	private void fireApproximationsTableListeners() {
		approximationsTableListeners.forEach((listener)->listener.tableChanged(
				new TableModelEvent(approximationsTableModel)));
	}
	
	private void fireCoeffecientsTableListeners() {
		coefficientsTableListeners.forEach((listener)->listener.tableChanged(
				new TableModelEvent(coefficientsTableModel)));
	}
	
	public void graphIds(Consumer<Object> consumer) {
		consumer.accept(sample.id);
		approximations.forEach(
				(approximation)->consumer.accept(approximation.function.id));
	}
	
	public String label() {
		return sample.label;
	}
	
	private void removeApproximationButton(ActionEvent event) {
		int selected=approximationsTable.getSelectedRow();
		if (0<=selected) {
			Approximation approximation=approximations.remove(selected);
			plotter.removeFunction(approximation.function);
			fireApproximationsTableListeners();
		}
	}
	
	private void removeSampleButton(ActionEvent event) {
		plotter.removeSample(this);
	}
	
	public Sample sample() {
		return sample;
	}
	
	private void sampleColorChanged(ActionEvent event) {
		Color color=(Color)sampleColor.getSelectedItem();
		sample=sample.setColors(Colors.INTERPOLATION, color, color);
		plotter.replaceSample(sample);
	}
	
	private void saveSampleButton(ActionEvent event) {
        SaveSampleProcess.start(plotter, sample);
	}
	
	public void usedColors(Consumer<Color> consumer) {
		consumer.accept(sample.pointColor);
		approximations.forEach((approximation)->
				consumer.accept(approximation.function.color));
	}
}
