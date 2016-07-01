package com.shuffle.empacotador.swing.main.button;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.shuffle.empacotador.swing.core.button.BaseButton;
import com.shuffle.empacotador.swing.core.button.event.ClickAction;

public class ChooseDestinationButton extends BaseButton implements ClickAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 748699220601977267L;

	private JTextField destinationField;

	private JFileChooser fileChooser;

	public ChooseDestinationButton(JTextField destinationField) {
		super("Selecione...");
		setShowLoading(false);
		this.destinationField = destinationField;
		addClickAction(this);
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Selecionar pasta");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos ZIP", "zip"));
	}

	@Override
	public void click() {
		if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			destinationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}
}
