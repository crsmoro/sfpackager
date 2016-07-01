package com.shuffle.empacotador.swing.main.group;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;

import com.shuffle.empacotador.swing.main.button.ChooseSourceButton;
import com.shuffle.empacotador.swing.main.field.SourceField;

public class SourcePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1572784062061479106L;

	private Dimension labelDimension = new Dimension(80, 25);

	private SourceField sourceField;

	private ChooseSourceButton chooseSourceButton;

	public SourcePanel() {
		super(new FlowLayout(FlowLayout.LEFT));
		sourceField = new SourceField("Pasta SVN", 30);
		sourceField.getLabel().setPreferredSize(labelDimension);
		add(sourceField);
		chooseSourceButton = new ChooseSourceButton(sourceField.getField());
		add(chooseSourceButton);
	}

	public SourceField getSourceField() {
		return sourceField;
	}

	public void setSourceField(SourceField sourceField) {
		this.sourceField = sourceField;
	}

	public ChooseSourceButton getChooseSourceButton() {
		return chooseSourceButton;
	}

	public void setChooseSourceButton(ChooseSourceButton chooseSourceButton) {
		this.chooseSourceButton = chooseSourceButton;
	}
}
