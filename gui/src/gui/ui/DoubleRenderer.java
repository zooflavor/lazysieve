package gui.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class DoubleRenderer implements TableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JPanel panel=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JLabel label=new JLabel(
				(null==value)
						?""
						:String.format("%1$,g", value));
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		panel.add(label);
		if (isSelected) {
			panel.setBackground(table.getSelectionBackground());
			label.setBackground(table.getSelectionBackground());
			label.setForeground(table.getSelectionForeground());
		}
		else {
			panel.setBackground(table.getBackground());
			label.setBackground(table.getBackground());
			label.setForeground(table.getForeground());
		}
		return panel;
	}
}
