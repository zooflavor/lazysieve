package gui.plotter;

import gui.math.Functions;
import gui.ui.EventHandler;
import gui.ui.GuiParent;
import gui.ui.SwingUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class FunctionSelector implements GuiParent<JDialog> {
	private final List<JCheckBox> checkBoxes=new ArrayList<>();
	private final JDialog dialog;
	private final EventHandler<List<Function<Double, Double>>> handler;
	private final List<Function<Double, Double>> functions=new ArrayList<>();
	
	public FunctionSelector(
			EventHandler<List<Function<Double, Double>>> handler,
			Plotter plotter) {
		this.handler=handler;
		
		dialog=new JDialog(SwingUtils.window(plotter.component()),
				"Base functions");
		dialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new GridLayout(0, 4));
		dialog.getContentPane().add(centerPanel, BorderLayout.CENTER);
		
		for (Function<Double, Double> function: Functions.FUNCTIONS) {
			JCheckBox checkBox=new JCheckBox(function.toString());
			checkBoxes.add(checkBox);
			functions.add(function);
			JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(checkBox);
			centerPanel.add(panel);
		}
		
		JPanel southPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		dialog.getContentPane().add(southPanel, BorderLayout.SOUTH);
		
		JButton okButton=new JButton("Ok");
		okButton.addActionListener(actionListener(this::okButton));
		southPanel.add(okButton);
		
		JButton cancelButton=new JButton("Cancel");
		cancelButton.addActionListener(actionListener(this::cancelButton));
		southPanel.add(cancelButton);
		
		dialog.pack();
	}
	
	private void cancelButton(ActionEvent event) throws Throwable {
		dialog.dispose();
	}
	
	@Override
	public JDialog component() {
		return dialog;
	}
	
	private void okButton(ActionEvent event) throws Throwable {
		dialog.dispose();
		List<Function<Double, Double>> functions2=new ArrayList<>();
		for (int ii=0; checkBoxes.size()>ii; ++ii) {
			if (checkBoxes.get(ii).isSelected()) {
				functions2.add(functions.get(ii));
			}
		}
		handler.handle(functions2);
	}
	
	@Override
	public void setAllEnabled(boolean enabled) {
		dialog.setEnabled(enabled);
	}
	
    public void start() throws Throwable {
		SwingUtils.show(dialog);
	}
}
