package com.shuffle.empacotador.swing.core.field;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FieldLabel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4045632732130355546L;

	private JLabel label;

	private JTextField field;

	public FieldLabel(String label, int columns) {
		super();
		add(Box.createHorizontalGlue());
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.label = new JLabel(label, JLabel.RIGHT);
		this.label.setAlignmentX(Component.RIGHT_ALIGNMENT);
		add(this.label);
		
		add(Box.createRigidArea(new Dimension(5, 0)));
		
		this.field = new JTextField(columns);
		this.field.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.label.setLabelFor(this.field);
		add(this.field);
	}

	public JLabel getLabel() {
		return label;
	}

	public void setLabel(JLabel label) {
		this.label = label;
	}

	public JTextField getField() {
		return field;
	}

	public void setField(JTextField field) {
		this.field = field;
	}
}
