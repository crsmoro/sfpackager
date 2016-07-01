package com.shuffle.empacotador.swing.core.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class BaseFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4458145327774103494L;

	public BaseFrame(String title) {
		super(title);
		setIconImage(new ImageIcon(getClass().getProtectionDomain().getClassLoader().getResource("icon.png")).getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar jMenuBar = new JMenuBar();
		JMenu jMenuArquivo = new JMenu("Arquivo");
		jMenuBar.add(jMenuArquivo);

		JMenuItem jMenuItemSair = new JMenuItem("Sair");
		jMenuItemSair.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		jMenuArquivo.add(jMenuItemSair);
		setJMenuBar(jMenuBar);
	}
}
