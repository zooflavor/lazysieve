package gui.ui;

import gui.math.UnsignedLong;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class UnsignedLongSpinner extends JSpinner {
	private static final long serialVersionUID=0l;
	
	private class EditorListener implements ActionListener, FocusListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			validateEditor();
		}
		
		@Override
		public void focusGained(FocusEvent event) {
		}
		
		@Override
		public void focusLost(FocusEvent event) {
			validateEditor();
		}
	}
	
	public static interface Listener {
		void changed(long value);
	}
	
	private class Model implements SpinnerModel {
		@Override
		public void addChangeListener(ChangeListener listener) {
			changeListeners.add(listener);
		}
		
		@Override
		public Object getNextValue() {
			validateEditor();
			return (0>=Long.compareUnsigned(max, value))?null:(value+step);
		}
		
		@Override
		public Object getPreviousValue() {
			validateEditor();
			return (0<=Long.compareUnsigned(min, value))?null:(value-step);
		}
		
		@Override
		public Object getValue() {
			return value;
		}
		
		@Override
		public void removeChangeListener(ChangeListener listener) {
			changeListeners.remove(listener);
		}
		
		@Override
		public void setValue(Object value) {
			if (!(value instanceof Long)) {
				return;
			}
			long value2=(Long)value;
			if ((0l!=Long.remainderUnsigned(value2-min, step))
					|| (0>Long.compareUnsigned(max, value2))
					|| (0<Long.compareUnsigned(min, value2))) {
				return;
			}
			UnsignedLongSpinner.this.value=value2;
			fireListeners();
		}
	}
	
	private final Collection<ChangeListener> changeListeners
			=new LinkedList<>();
	private final JTextField editor;
	private final Collection<UnsignedLongSpinner.Listener> listeners
			=new LinkedList<>();
	private long max;
	private long min;
	private long step;
	private long value;
	
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public UnsignedLongSpinner(long max, long min, long number, long step) {
		this.max=max;
		this.min=min;
		this.step=step;
		setModel(new Model());
		
		editor=new JTextField();
		editor.setFont(new Font(
				Font.MONOSPACED, Font.PLAIN, editor.getFont().getSize()));
		editor.setColumns(27);
		editor.setHorizontalAlignment(JTextField.RIGHT);
		EditorListener editorListener=new EditorListener();
		editor.addActionListener(editorListener);
		editor.addFocusListener(editorListener);
		setEditor(editor);
		
		setNumber(number);
	}
	
	public void addListener(UnsignedLongSpinner.Listener listener) {
		listeners.add(listener);
	}
	
	private void fireListeners() {
		editor.setText(UnsignedLong.format(value));
		for (ChangeListener listener: changeListeners) {
			listener.stateChanged(new ChangeEvent(this));
		}
		for (UnsignedLongSpinner.Listener listener: listeners) {
			listener.changed(value);
		}
	}
	
	public long getNumber() {
		return value;
	}
	
	public void removeListener(UnsignedLongSpinner.Listener listener) {
		listeners.remove(listener);
	}
	
	public void reset(long max, long min, long number, long step) {
		this.max=max;
		this.min=min;
		this.step=step;
		setNumber(number);
	}
	
	public void setNumber(long number) {
		editor.setText(UnsignedLong.format(number));
		validateEditor();
	}
	
	private void validateEditor() {
		long newValue=value;
		try {
			newValue=UnsignedLong.parse(editor.getText());
		}
		catch (NumberFormatException ex) {
		}
		if (0l!=Long.remainderUnsigned(newValue-min, step)) {
			newValue+=step-Long.remainderUnsigned(newValue-min, step);
		}
		newValue=UnsignedLong.max(min, UnsignedLong.min(max, newValue));
		value=newValue;
		fireListeners();
	}
}
