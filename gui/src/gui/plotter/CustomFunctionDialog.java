package gui.plotter;

import gui.math.CustomFunction;
import gui.math.RealFunction;
import gui.ui.GuiWindow;
import gui.ui.MessageException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class CustomFunctionDialog extends GuiWindow<JDialog> {
    public static final char MNEMONIC='s';
    public static final String TITLE="Script függvény";
    
    private final JDialog dialog;
    private final JComboBox<String> engineName;
    private final JTextField name;
    private final FunctionSelector functionSelector;
    private final JTextArea script;
    
    public CustomFunctionDialog(FunctionSelector functionSelector) {
        super(functionSelector.session);
        this.functionSelector=functionSelector;
        
        dialog=new JDialog(functionSelector.window(), TITLE);
        dialog.getContentPane().setLayout(new BorderLayout());
        
        JPanel northPanel=new JPanel(new FlowLayout());
        dialog.getContentPane().add(northPanel, BorderLayout.NORTH);
        
        JButton okButton=new JButton("Ok");
        northPanel.add(okButton);
        okButton.addActionListener(actionListener(this::ok));
        
        JButton cancelButton=new JButton("Mégsem");
        northPanel.add(cancelButton);
        cancelButton.addActionListener(actionListener(this::cancel));
        
        JPanel centerPanel=new JPanel();
        SpringLayout layout=new SpringLayout();
        centerPanel.setLayout(layout);
        dialog.getContentPane().add(centerPanel, BorderLayout.CENTER);
        
        JLabel engineNameLabel=new JLabel("Scriptnyelv:");
        centerPanel.add(engineNameLabel);
		DefaultComboBoxModel<String> engineNamesModel
				=new DefaultComboBoxModel<>();
        for (ScriptEngineFactory factory:
                new ScriptEngineManager().getEngineFactories()) {
			List<String> names=factory.getNames();
			System.out.println(names);
			if (names.contains("JavaScript")) {
				engineNamesModel.addElement("JavaScript");
			}
			else if (!names.isEmpty()) {
				engineNamesModel.addElement(names.get(0));
			}
        }
		if (0>=engineNamesModel.getSize()) {
			throw new MessageException("Nincs scriptnyelv telepítve.");
		}
        engineName=new JComboBox<>(engineNamesModel);
        centerPanel.add(engineName);
        
        JLabel nameLabel=new JLabel("Függvénynév:");
        centerPanel.add(nameLabel);
        name=new JTextField(30);
        centerPanel.add(name);
        
        JLabel scriptLabel=new JLabel("Script:");
        centerPanel.add(scriptLabel);
        script=new JTextArea(10, 60);
        script.setBorder(name.getBorder());
		JScrollPane scriptScroll=new JScrollPane(script);
        centerPanel.add(scriptScroll);
        
        layout.putConstraint(SpringLayout.NORTH, engineName, 5, SpringLayout.NORTH, centerPanel);
        layout.putConstraint(SpringLayout.NORTH, name, 5, SpringLayout.SOUTH, engineName);
        layout.putConstraint(SpringLayout.NORTH, scriptScroll, 5, SpringLayout.SOUTH, name);
        layout.putConstraint(SpringLayout.SOUTH, centerPanel, 5, SpringLayout.SOUTH, scriptScroll);
        layout.putConstraint(SpringLayout.EAST, centerPanel, 5, SpringLayout.EAST, scriptScroll);
        layout.putConstraint(SpringLayout.WEST, engineName, 0, SpringLayout.WEST, scriptScroll);
        layout.putConstraint(SpringLayout.WEST, scriptScroll, 0, SpringLayout.WEST, name);
        layout.putConstraint(SpringLayout.EAST, engineNameLabel, -5, SpringLayout.WEST, engineName);
        layout.putConstraint(SpringLayout.WEST, name, 5, SpringLayout.EAST, nameLabel);
        layout.putConstraint(SpringLayout.EAST, scriptLabel, -5, SpringLayout.WEST, scriptScroll);
        layout.putConstraint(SpringLayout.BASELINE, engineNameLabel, 0, SpringLayout.BASELINE, engineName);
        layout.putConstraint(SpringLayout.BASELINE, nameLabel, 0, SpringLayout.BASELINE, name);
        layout.putConstraint(SpringLayout.BASELINE, scriptLabel, 0, SpringLayout.BASELINE, scriptScroll);
        layout.putConstraint(SpringLayout.WEST, nameLabel, 5, SpringLayout.WEST, centerPanel);
		
		dialog.pack();
    }
    
    private void cancel(ActionEvent event) throws Throwable {
        dialog.dispose();
    }
    
    private void ok(ActionEvent event) throws Throwable {
        String engineName2=(String)engineName.getSelectedItem();
        String name2=name.getText();
        String script2=script.getText();
        if (null==engineName2) {
			throw new MessageException("A scriptnyelv nincs megadva.");
        }
        if (name2.isEmpty()) {
			throw new MessageException("A függvénynév nincs megadva.");
        }
        if (script2.isEmpty()) {
			throw new MessageException("A script üres.");
        }
		RealFunction function
				=CustomFunction.create(engineName2, name2, script2);
		try {
			function.valueAt(0.0);
			function.valueAt(1.0);
			function.valueAt(10.0);
		}
		catch (ArithmeticException ex) {
		}
		functionSelector.addCustomFunction(function);
		dialog.dispose();
    }
	
    public static void start(FunctionSelector functionSelector)
            throws Throwable {
		new CustomFunctionDialog(functionSelector)
                .show();
	}
    
    @Override
    public JDialog window() {
        return dialog;
    }
}
