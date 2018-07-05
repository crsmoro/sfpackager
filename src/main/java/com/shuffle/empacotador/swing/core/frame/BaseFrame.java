package com.shuffle.empacotador.swing.core.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class BaseFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4458145327774103494L;
	
	private ButtonGroup opcoesGroup;

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
		
		JMenu jMenuOpcoes = new JMenu("Opções");
		jMenuBar.add(jMenuOpcoes);
		
		opcoesGroup = new ButtonGroup();
		JMenuItem jMenuItemSvn = new JRadioButtonMenuItem("SVN", false);
		opcoesGroup.add(jMenuItemSvn);
		jMenuOpcoes.add(jMenuItemSvn);
		JMenuItem jMenuItemGit = new JRadioButtonMenuItem("Git", true);
		opcoesGroup.add(jMenuItemGit);
		jMenuOpcoes.add(jMenuItemGit);

		setJMenuBar(jMenuBar);
	}
	
	public ButtonGroup getOpcoesGroup()
	{
		return opcoesGroup;
	}
}
