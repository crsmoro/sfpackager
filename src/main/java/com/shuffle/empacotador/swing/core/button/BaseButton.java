package com.shuffle.empacotador.swing.core.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;

import com.shuffle.empacotador.swing.core.button.event.ClickAction;
import com.shuffle.empacotador.swing.core.button.event.ClickJob;

public class BaseButton extends JButton implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7861539181329241956L;

	private ClickAction clickAction;

	private ClickJob clickJob;

	private boolean showLoading = true;

	public BaseButton() {
		addActionListener(this);
	}

	public BaseButton(String title) {
		super(title);
		addActionListener(this);
	}

	public void addClickAction(ClickAction clickAction) {
		this.clickAction = clickAction;
	}

	public boolean isShowLoading() {
		return showLoading;
	}

	public void setShowLoading(boolean showLoading) {
		this.showLoading = showLoading;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (clickAction != null) {
			clickJob = new ClickJob(this, clickAction);
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.execute(clickJob);
		}
	}
}
