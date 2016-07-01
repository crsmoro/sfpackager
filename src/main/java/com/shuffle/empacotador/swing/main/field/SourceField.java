package com.shuffle.empacotador.swing.main.field;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import com.shuffle.empacotador.swing.core.field.FieldLabel;

public class SourceField extends FieldLabel implements FocusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6866631424499112419L;

	public SourceField(String label, int columns) {
		super(label, columns);
		getField().addFocusListener(this);
	}

	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {
		if (StringUtils.isNotBlank(getField().getText())) {
			if (getField().getText().endsWith(File.separator)) {
				getField().setText(getField().getText().substring(0, getField().getText().length() - 1));
			}
			if (!new File(getField().getText()).exists() || !new File(getField().getText()).isDirectory()) {
				if (JOptionPane.showConfirmDialog(null, "Origem não válida" + System.lineSeparator() + "Deseja limpar o campo?", "Origem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					getField().setText("");
				} else {
					getField().grabFocus();
				}

			}
		}
	}
}
