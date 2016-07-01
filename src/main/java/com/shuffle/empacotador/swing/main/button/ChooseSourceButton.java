package com.shuffle.empacotador.swing.main.button;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

import com.shuffle.empacotador.swing.core.button.BaseButton;
import com.shuffle.empacotador.swing.core.button.event.ClickAction;

public class ChooseSourceButton extends BaseButton implements ClickAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8499913574559650168L;

	private JTextField sourceField;

	private JFileChooser fileChooser;

	public ChooseSourceButton(JTextField sourceField) {
		super("Selecione...");
		setShowLoading(false);
		this.sourceField = sourceField;
		addClickAction(this);
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle("Selecionar pasta");
	}

	@Override
	public void click() {
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			sourceField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}
}
