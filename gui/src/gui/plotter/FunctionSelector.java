package gui.plotter;

import gui.math.Functions;
import gui.math.RealFunction;
import gui.ui.GuiWindow;
import gui.ui.SwingUtils;
import gui.util.Consumer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class FunctionSelector extends GuiWindow<JDialog> {
	private final List<JCheckBox> checkBoxes=new ArrayList<>();
	private final JDialog dialog;
	private final Consumer<List<RealFunction>> handler;
	private final List<RealFunction> functions=new ArrayList<>();
	
	public FunctionSelector(Consumer<List<RealFunction>> handler,
			Plotter plotter) {
		super(plotter.session);
		this.handler=handler;
		
		dialog=new JDialog(SwingUtils.window(plotter.window()),
				"Base functions");
		dialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new GridLayout(0, 4));
		dialog.getContentPane().add(centerPanel, BorderLayout.CENTER);
		
		for (RealFunction function: Functions.FUNCTIONS) {
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
	
	private void okButton(ActionEvent event) throws Throwable {
		dialog.dispose();
		List<RealFunction> functions2=new ArrayList<>();
		for (int ii=0; checkBoxes.size()>ii; ++ii) {
			if (checkBoxes.get(ii).isSelected()) {
				functions2.add(functions.get(ii));
			}
		}
		handler.consume(functions2);
	}
	
    public void start() throws Throwable {
		SwingUtils.show(dialog);
	}
	
	@Override
	public JDialog window() {
		return dialog;
	}
}
