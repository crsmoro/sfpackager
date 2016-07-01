package com.shuffle.empacotador.swing.core.button.event;

import javax.swing.SwingWorker;

import com.shuffle.empacotador.swing.core.button.BaseButton;

public class ClickJob extends SwingWorker<Void, Void> {

	private BaseButton baseButton;

	private ClickAction clickAction;

	private String originalLabel;

	public ClickJob(BaseButton baseButton, ClickAction clickAction) {
		this.baseButton = baseButton;
		this.clickAction = clickAction;
		this.originalLabel = this.baseButton.getText();
	}

	@Override
	protected Void doInBackground() throws Exception {
		if (baseButton.isShowLoading()) {
			baseButton.setText("Executando...");
		}
		baseButton.setEnabled(false);
		clickAction.click();
		return null;
	}

	@Override
	protected void done() {
		baseButton.setText(originalLabel);
		baseButton.setEnabled(true);
	}

}
