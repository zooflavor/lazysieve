package gui.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;

public class ColorRenderer
		implements ListCellRenderer<Color>, TableCellRenderer {
	private Component getComponent(java.awt.Color background, Color color,
			java.awt.Color foreground) {
		JPanel panel0=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel0.setBackground(background);
		if (null!=color) {
			JPanel panel1=new JPanel();
			panel1.setBackground(Color.TRANSPARENT.awt());
			panel0.add(panel1);
			panel1.setBackground(color.awt());
			JLabel label=new JLabel(color.name);
			label.setFont(label.getFont().deriveFont(Font.PLAIN));
			label.setForeground(foreground);
			panel0.add(label);
		}
		return panel0;
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends Color> list,
			Color value, int index, boolean isSelected, boolean cellHasFocus) {
		return getComponent(
				isSelected
						?list.getSelectionBackground()
						:list.getBackground(),
				value,
				isSelected
						?list.getSelectionForeground()
						:list.getForeground());
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return getComponent(
				isSelected
						?table.getSelectionBackground()
						:table.getBackground(),
				(Color)value,
				isSelected
						?table.getSelectionForeground()
						:table.getForeground());
	}
}
