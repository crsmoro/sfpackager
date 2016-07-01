package com.shuffle.empacotador.swing.main.table.model;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class FilesScrollTable extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4956759300700251229L;

	private JTable table;

	public FilesScrollTable(FilesModel filesModel) {
		table = new JTable(filesModel);
		table.getColumnModel().removeColumn(table.getColumnModel().getColumn(3));
		table.getColumnModel().removeColumn(table.getColumnModel().getColumn(2));
		setWidthAsPercentages(table, 0.02, 0.98);
		getViewport().add(table);
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	private static void setWidthAsPercentages(JTable table, double... percentages) {
		final double factor = table.getPreferredScrollableViewportSize().getWidth();

		TableColumnModel model = table.getColumnModel();
		for (int columnIndex = 0; columnIndex < percentages.length; columnIndex++) {
			TableColumn column = model.getColumn(columnIndex);
			column.setPreferredWidth((int) (percentages[columnIndex] * factor));
		}
	}
}
