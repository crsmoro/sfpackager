package com.shuffle.empacotador.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNException;

import com.shuffle.empacotador.EmpacotadorMain;
import com.shuffle.empacotador.core.Packer;

public class MainFrame {
	private transient static final Log log = LogFactory.getLog(EmpacotadorMain.class);

	public MainFrame() {
		try {
			JFrame frame = new JFrame("Empacotador");
			frame.setIconImage(new ImageIcon(getClass().getProtectionDomain().getClassLoader().getResource("icon.png")).getImage());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
			frame.setJMenuBar(jMenuBar);

			JPanel jPanel = new JPanel(new BorderLayout());

			JPanel jPanelCenter = new JPanel();
			jPanelCenter.setLayout(new BoxLayout(jPanelCenter, BoxLayout.Y_AXIS));
			jPanel.add(jPanelCenter, BorderLayout.CENTER);

			JPanel jPanelOrigem = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JTextField jTextFieldSource = new JTextField(30);
			jTextFieldSource.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					if (StringUtils.isNotBlank(jTextFieldSource.getText())) {
						if (jTextFieldSource.getText().endsWith("/")) {
							jTextFieldSource.setText(jTextFieldSource.getText().substring(0, jTextFieldSource.getText().length() - 1));
						}
						if (!new File(jTextFieldSource.getText()).exists() || !new File(jTextFieldSource.getText()).isDirectory()) {
							if (JOptionPane.showConfirmDialog(null, "Origem não válida" + System.lineSeparator() + "Deseja limpar o campo?", "Origem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
								jTextFieldSource.setText("");
							}
							else {
								jTextFieldSource.grabFocus();
							}
							
						}
					}
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					
				}
			});
			JLabel jLabelChooseSource = new JLabel("Escolha a origem");
			jLabelChooseSource.setLabelFor(jTextFieldSource);
			jPanelOrigem.add(jLabelChooseSource);
			jPanelOrigem.add(jTextFieldSource);
			jPanelCenter.add(jPanelOrigem);

			JPanel jPanelDestino = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JTextField jTextFieldDestination = new JTextField(30);
			JLabel jLabelChooseDestination = new JLabel("Escolha o destino");
			jLabelChooseDestination.setLabelFor(jTextFieldDestination);
			jPanelDestino.add(jLabelChooseDestination);
			jPanelDestino.add(jTextFieldDestination);
			jPanelCenter.add(jPanelDestino);

			DefaultTableModel tableModel = new DefaultTableModel() {

				private static final long serialVersionUID = 8238068056009508913L;

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					switch (columnIndex) {
					case 0:
						return Boolean.class;
					default:
						return String.class;
					}
				}
			};
			tableModel.addColumn("Incluir?");
			tableModel.addColumn("Arquivos");
			tableModel.addColumn("File");
			
			JScrollPane jScrollPane = new JScrollPane();
			JTable jTableFiles = new JTable(tableModel);
			jTableFiles.getColumnModel().removeColumn(jTableFiles.getColumnModel().getColumn(2));
			setWidthAsPercentages(jTableFiles, 0.02, 0.98);
			jScrollPane.getViewport().add(jTableFiles);
			jPanelCenter.add(jScrollPane);

			JPanel jPanelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			JButton jButtonViewFiles = new JButton("Visualizar Arquivos");
			jButtonViewFiles.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (StringUtils.isBlank(jTextFieldSource.getText()) || StringUtils.isBlank(jTextFieldDestination.getText())) {
						JOptionPane.showMessageDialog(null, "Preencha todos os campos", "Campos não preenchidos", JOptionPane.WARNING_MESSAGE);
					} else {

						ExecuteJob executeJob = new ExecuteJob(jButtonViewFiles, new ClickAction() {

							@Override
							public void click() {
								try {
									removeAllRows(tableModel);
									Packer packer = new Packer();
									packer.setSourceFolder(jTextFieldSource.getText());
									packer.getExceptionFiles().add("pom.xml");
									packer.setDestinationFolder(jTextFieldDestination.getText().substring(0, jTextFieldDestination.getText().lastIndexOf(File.separator)));
									for (File file : packer.getPendingFiles()) {
										tableModel.addRow(new Object[] { Boolean.TRUE, packer.getFullPathDestination(file).replace(packer.getDestinationFolder() + File.separator, ""), file});
									}
									JOptionPane.showMessageDialog(null, "Arquivos a serem utilizados na geração do pacote atualizados", "Concluído", JOptionPane.INFORMATION_MESSAGE);
								} catch (SVNException ex) {
									JOptionPane.showMessageDialog(null, ex.getErrorMessage().getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
								} catch (IllegalArgumentException ea) {
									JOptionPane.showMessageDialog(null, ea.getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
								}
							}
						});
						ExecutorService executorService = Executors.newSingleThreadExecutor();
						executorService.execute(executeJob);
					}
				}
			});
			jPanelButtons.add(jButtonViewFiles);

			JButton jButtonCratePackage = new JButton("Criar pacote");
			jButtonCratePackage.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (StringUtils.isBlank(jTextFieldSource.getText()) || StringUtils.isBlank(jTextFieldDestination.getText())) {
						JOptionPane.showMessageDialog(null, "Preencha todos os campos", "Campos não preenchidos", JOptionPane.WARNING_MESSAGE);
					}
					else if (tableModel.getRowCount() <= 0) {
						JOptionPane.showMessageDialog(null, "Visualize os arquivos antes de gerar o pacote", "Gerar pacote", JOptionPane.WARNING_MESSAGE);
					}
					else {
						ExecuteJob executeJob = new ExecuteJob(jButtonCratePackage, new ClickAction() {

							@Override
							public void click() {
								try {

									List<File> filesForPatch = new ArrayList<>();
									for (int i = 0; i < tableModel.getRowCount(); i++) {
										Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
										log.info("Checkado : " + checked);
										File file = (File)tableModel.getValueAt(i, 2);
										log.info("File : " + file.getAbsolutePath());
										if (checked) {
											filesForPatch.add(file);
										}
									}
									if (filesForPatch.isEmpty()) {
										JOptionPane.showMessageDialog(null, "Selecione algum arquivo para gerar o pacote", "Gerar pacote", JOptionPane.WARNING_MESSAGE);
									}
									else {
										Packer packer = new Packer();
										packer.setSourceFolder(jTextFieldSource.getText());
										packer.getExceptionFiles().add("pom.xml");
										packer.setDestinationFolder(jTextFieldDestination.getText().substring(0, jTextFieldDestination.getText().lastIndexOf(File.separator)));
										packer.setZipName(jTextFieldDestination.getText().substring(jTextFieldDestination.getText().lastIndexOf(File.separator) + 1, jTextFieldDestination.getText().length()));
										packer.setPendingFiles(filesForPatch);
										packer.createPatch();
										JOptionPane.showMessageDialog(null, "Pacote gerado com sucesso disponível em " + jTextFieldDestination.getText(), "Concluído", JOptionPane.INFORMATION_MESSAGE);
									}
								} catch (SVNException ex) {
									JOptionPane.showMessageDialog(null, ex.getErrorMessage().getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
								} catch (IllegalArgumentException ea) {
									JOptionPane.showMessageDialog(null, ea.getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
								}
							}
						});
						ExecutorService executorService = Executors.newSingleThreadExecutor();
						executorService.execute(executeJob);
					}
				}
			});
			jPanelButtons.add(jButtonCratePackage);
			jPanel.add(jPanelButtons, BorderLayout.SOUTH);

			frame.add(jPanel);
			frame.pack();
			frame.setSize(600, 450);
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setWidthAsPercentages(JTable table, double... percentages) {
		final double factor = table.getPreferredScrollableViewportSize().getWidth();

		TableColumnModel model = table.getColumnModel();
		for (int columnIndex = 0; columnIndex < percentages.length; columnIndex++) {
			TableColumn column = model.getColumn(columnIndex);
			column.setPreferredWidth((int) (percentages[columnIndex] * factor));
		}
	}

	private void removeAllRows(DefaultTableModel model) {
		int totalRows = model.getRowCount();
		for (int i = totalRows - 1; i >= 0; i--) {
			model.removeRow(i);
		}
		model.fireTableDataChanged();
	}

	private interface ClickAction {

		void click();

	}

	private class ExecuteJob extends SwingWorker<Void, Void> {

		private JButton jButton;

		private ClickAction clickAction;

		private String originalLabel;

		public ExecuteJob(JButton jButton, ClickAction clickAction) {
			this.jButton = jButton;
			this.clickAction = clickAction;
			this.originalLabel = this.jButton.getText();
		}

		@Override
		protected Void doInBackground() throws Exception {
			jButton.setText("Executando...");
			jButton.setEnabled(false);
			clickAction.click();
			return null;
		}

		@Override
		protected void done() {
			jButton.setText(originalLabel);
			jButton.setEnabled(true);
		}

	}
}
