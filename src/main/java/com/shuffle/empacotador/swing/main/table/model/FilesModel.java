package com.shuffle.empacotador.swing.main.table.model;

import javax.swing.table.DefaultTableModel;

public class FilesModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1710568054549411899L;
	
	public FilesModel() {
		addColumn("Incluir?");
		addColumn("Arquivos");
		addColumn("Origem");
		addColumn("Destino");
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		default:
			return String.class;
		}
	}
}
