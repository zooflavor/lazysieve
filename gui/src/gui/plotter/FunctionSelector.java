package gui.plotter;

import gui.math.Functions;
import gui.math.RealFunction;
import gui.ui.GuiWindow;
import gui.ui.MessageException;
import gui.ui.SwingUtils;
import gui.util.Consumer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class FunctionSelector extends GuiWindow<JDialog> {
    private class CustomFunctionsModel implements ListModel<RealFunction> {
        @Override
        public void addListDataListener(ListDataListener listener) {
            customFunctionsListener.add(listener);
        }
        
        @Override
        public RealFunction getElementAt(int index) {
            return customFunctions.get(index);
        }
        
        @Override
        public int getSize() {
            return customFunctions.size();
        }
        
        @Override
        public void removeListDataListener(ListDataListener listener) {
            customFunctionsListener.remove(listener);
        }
    }
    
	private final List<JCheckBox> checkBoxes=new ArrayList<>();
    private final List<RealFunction> customFunctions=new ArrayList<>();
    private final JList<RealFunction> customFunctionsList;
    private final List<ListDataListener> customFunctionsListener
            =new ArrayList<>();
	private final JDialog dialog;
	private final Consumer<List<RealFunction>> handler;
	private final List<RealFunction> functions=new ArrayList<>();
	
	public FunctionSelector(Consumer<List<RealFunction>> handler,
			Plotter plotter) {
		super(plotter.session);
		this.handler=handler;
		
		dialog=new JDialog(plotter.window(), "Alapfüggvények");
		dialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel northPanel=new JPanel(new BorderLayout());
		dialog.getContentPane().add(northPanel, BorderLayout.NORTH);
		
		JPanel northNorthPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		northPanel.add(northNorthPanel, BorderLayout.NORTH);
		
		JButton okButton=new JButton("Ok");
		okButton.setMnemonic('o');
		okButton.addActionListener(actionListener(this::okButton));
		northNorthPanel.add(okButton);
		
		JButton cancelButton=new JButton("Mégsem");
		cancelButton.setMnemonic('m');
		cancelButton.addActionListener(actionListener(this::cancelButton));
		northNorthPanel.add(cancelButton);
		
		JPanel northSouthPanel=new JPanel();
		northPanel.add(northSouthPanel, BorderLayout.SOUTH);
		
		List<RealFunction> rest=new ArrayList<>(Functions.FUNCTIONS);
		List<List<RealFunction>> groups=new ArrayList<>();
		
		List<RealFunction> group=new ArrayList<>();
		group.add(remove(rest, Functions.ONE));
		group.add(remove(rest, Functions.X));
		group.add(remove(rest, Functions.X2));
		group.add(remove(rest, Functions.X3));
		group.add(remove(rest, Functions.X4));
		group.add(remove(rest, Functions.X5));
		group.add(remove(rest, Functions.X6));
		groups.add(group);
		
		group=new ArrayList<>();
		group.add(remove(rest, Functions.LNLNX));
		group.add(remove(rest, Functions.LNX));
		group.add(remove(rest, Functions.LNX_LNLNX));
		group.add(remove(rest, Functions.LN2X));
		group.add(remove(rest, Functions.X_LNLNX));
		group.add(remove(rest, Functions.X_LNX));
		group.add(remove(rest, Functions.X_LNX_LNLNX));
		group.add(remove(rest, Functions.X_LN2X));
		groups.add(group);
		
		group=new ArrayList<>();
		group.add(remove(rest, Functions.ONE_PER_LNLNX));
		group.add(remove(rest, Functions.ONE_PER_LNX));
		group.add(remove(rest, Functions.ONE_PER_X));
		group.add(remove(rest, Functions.ONE_PER_X2));
		groups.add(group);
		
		if (!rest.isEmpty()) {
			groups.add(rest);
		}
		
		int columns=Math.max(1, groups.size());
		int rows=1;
		for (List<?> group2: groups) {
			rows=Math.max(rows, group2.size());
		}
		northSouthPanel.setLayout(new GridLayout(rows, columns));
		
		for (int rr=0; rows>rr; ++rr) {
			for (int cc=0; columns>cc; ++cc) {
				JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
				if (groups.size()>cc) {
					List<RealFunction> group2=groups.get(cc);
					if (group2.size()>rr) {
						RealFunction function=group2.get(rr);
						JCheckBox checkBox=new JCheckBox(function.toString());
						checkBoxes.add(checkBox);
						functions.add(function);
						panel.add(checkBox);
					}
				}
				northSouthPanel.add(panel);
			}
		}
        
        JPanel centerPanel=new JPanel(new BorderLayout());
        centerPanel.setLayout(new BorderLayout());
        dialog.getContentPane().add(centerPanel, BorderLayout.CENTER);
        
        JPanel centerNorthPanel=new JPanel(new FlowLayout());
        centerPanel.add(centerNorthPanel, BorderLayout.NORTH);
        
        JButton addCustomFunction=new JButton("Script fv. hozzáadása");
        addCustomFunction.addActionListener(
                actionListener(this::addCustomFunction));
        centerNorthPanel.add(addCustomFunction);
        
        JButton removeCustomFunction=new JButton("Script fv. eltávolítása");
        removeCustomFunction.addActionListener(
                actionListener(this::removeCustomFunction));
        centerNorthPanel.add(removeCustomFunction);
        
        customFunctionsList=new JList<>(new CustomFunctionsModel());
        customFunctionsList.setVisibleRowCount(8);
        centerPanel.add(new JScrollPane(customFunctionsList),
                BorderLayout.CENTER);
		
		dialog.pack();
	}
    
    private void addCustomFunction(ActionEvent event) throws Throwable {
        CustomFunctionDialog.start(this);
    }
    
    public void addCustomFunction(RealFunction function) throws Throwable {
        customFunctions.add(function);
        fireCustomFunctionsListeners();
    }
	
	private void cancelButton(ActionEvent event) throws Throwable {
		dialog.dispose();
	}
	
	private void fireCustomFunctionsListeners() {
		customFunctionsListener.forEach((listener)->
				listener.contentsChanged(new ListDataEvent(
                        customFunctionsList,
                        ListDataEvent.CONTENTS_CHANGED,
                        0,
                        customFunctions.size())));
	}
	
	private void okButton(ActionEvent event) throws Throwable {
		dialog.dispose();
		List<RealFunction> functions2=new ArrayList<>();
		for (int ii=0; checkBoxes.size()>ii; ++ii) {
			if (checkBoxes.get(ii).isSelected()) {
				functions2.add(functions.get(ii));
			}
		}
		functions2.addAll(customFunctions);
		if (functions2.isEmpty()) {
			throw new MessageException("Egy függvény sincs kiválasztva.");
		}
		handler.consume(functions2);
	}
	
	private static <T> T remove(Iterable<T> list, T value) {
		for (Iterator<T> iterator=list.iterator(); iterator.hasNext();) {
			if (Objects.equals(value, iterator.next())) {
				iterator.remove();
				return value;
			}
		}
		throw new NoSuchElementException(Objects.toString(value));
	}
    
    private void removeCustomFunction(ActionEvent event) throws Throwable {
        int selected=customFunctionsList.getSelectedIndex();
        if ((0<=selected)
                && (customFunctions.size()>selected)) {
            customFunctions.remove(selected);
            fireCustomFunctionsListeners();
        }
    }
	
    public void start() throws Throwable {
		SwingUtils.show(dialog);
	}
	
	@Override
	public JDialog window() {
		return dialog;
	}
}
