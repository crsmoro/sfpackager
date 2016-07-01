package com.shuffle.empacotador.swing.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNException;

import com.shuffle.empacotador.EmpacotadorMain;
import com.shuffle.empacotador.core.Packer;
import com.shuffle.empacotador.swing.core.button.event.ClickAction;
import com.shuffle.empacotador.swing.core.frame.BaseFrame;
import com.shuffle.empacotador.swing.main.button.CreatePackageButton;
import com.shuffle.empacotador.swing.main.button.ViewFilesButton;
import com.shuffle.empacotador.swing.main.field.SourceField;
import com.shuffle.empacotador.swing.main.group.SourcePanel;
import com.shuffle.empacotador.swing.main.table.model.FilesModel;
import com.shuffle.empacotador.swing.main.table.model.FilesScrollTable;

public class Main {
	private transient static final Log log = LogFactory.getLog(EmpacotadorMain.class);

	private BaseFrame baseFrame = new BaseFrame("Empacotador");

	private Packer packer = createPackerInstance();

	private JPanel mainPanel = new JPanel(new BorderLayout());

	private FilesModel filesModel = new FilesModel();

	private JFileChooser saveAsChooser = createSaveAsChooser();

	public Main() {
		try {

			JPanel centerPanel = new JPanel();
			centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
			mainPanel.add(centerPanel, BorderLayout.CENTER);

			JPanel fieldsPanel = new JPanel(new BorderLayout());

			JPanel sourcePanels = new JPanel();
			sourcePanels.setLayout(new BoxLayout(sourcePanels, BoxLayout.Y_AXIS));

			SourcePanel sourcePanel = new SourcePanel();
			SourceField origemField = sourcePanel.getSourceField();
			sourcePanels.add(sourcePanel);

			fieldsPanel.add(sourcePanels, BorderLayout.WEST);

			centerPanel.add(fieldsPanel);

			FilesScrollTable filesScrollTable = new FilesScrollTable(filesModel);
			centerPanel.add(filesScrollTable);

			JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			ViewFilesButton viewFilesButton = new ViewFilesButton();
			viewFilesButton.addClickAction(new ClickAction() {

				@Override
				public void click() {
					if (StringUtils.isBlank(origemField.getField().getText())) {
						JOptionPane.showMessageDialog(null, "Preencha todos os campos", "Campos não preenchidos", JOptionPane.WARNING_MESSAGE);
					} else {

						try {
							removeAllRows(filesModel);
							packer.setSourceFolder(origemField.getField().getText());
							Map<File, File> patchFiles = packer.getPatchFiles();
							for (File source : patchFiles.keySet()) {
								File destination = patchFiles.get(source);
								filesModel.addRow(new Object[] { Boolean.TRUE, packer.getStructuredPath(destination), source, destination });
							}
							TableRowSorter<FilesModel> tableRowSorter = new TableRowSorter<FilesModel>(filesModel);
							tableRowSorter.setSortable(0, false);
							filesScrollTable.getTable().setRowSorter(tableRowSorter);
							List<RowSorter.SortKey> sortKeys = new ArrayList<>();
							sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
							tableRowSorter.setSortKeys(sortKeys);
							tableRowSorter.sort();

							JOptionPane.showMessageDialog(null, "Arquivos a serem utilizados na geração do pacote atualizados", "Concluído", JOptionPane.INFORMATION_MESSAGE);
						} catch (SVNException ex) {
							JOptionPane.showMessageDialog(null, ex.getErrorMessage().getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
						} catch (IllegalArgumentException ea) {
							JOptionPane.showMessageDialog(null, ea.getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
						}

					}
				}
			});
			actionButtonsPanel.add(viewFilesButton);

			CreatePackageButton createPackageButton = new CreatePackageButton();
			createPackageButton.addClickAction(new ClickAction() {

				@Override
				public void click() {
					if (filesModel.getRowCount() <= 0) {
						JOptionPane.showMessageDialog(null, "Visualize os arquivos antes de gerar o pacote", "Gerar pacote", JOptionPane.WARNING_MESSAGE);
					} else {
						try {

							Map<File, File> filesForPatch = new HashMap<>();
							for (int i = 0; i < filesModel.getRowCount(); i++) {
								Boolean checked = (Boolean) filesModel.getValueAt(i, 0);
								log.info("Checkado : " + checked);
								File source = (File) filesModel.getValueAt(i, 2);
								File destination = (File) filesModel.getValueAt(i, 3);
								log.info("File : " + destination.getAbsolutePath());
								if (checked) {
									filesForPatch.put(source, destination);
								}
							}
							if (filesForPatch.isEmpty()) {
								JOptionPane.showMessageDialog(null, "Selecione algum arquivo para gerar o pacote", "Gerar pacote", JOptionPane.WARNING_MESSAGE);
							} else {
								if (saveAsChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
									File saveAsLocation = saveAsChooser.getSelectedFile();
									if (saveAsLocation.exists() || saveAsLocation.isDirectory() || !saveAsLocation.getAbsolutePath().endsWith(".zip")) {
										JOptionPane.showMessageDialog(null, "Local inválido", "Salvar em...", JOptionPane.WARNING_MESSAGE);
									} else {
										packer.setDestinationFolder(saveAsLocation.getParent());
										packer.setZipName(saveAsLocation.getName());
										packer.createPatch(filesForPatch);
										JOptionPane.showMessageDialog(null, "Pacote gerado com sucesso disponível em " + saveAsLocation.getAbsolutePath(), "Concluído", JOptionPane.INFORMATION_MESSAGE);
									}
								}
							}
						} catch (SVNException ex) {
							JOptionPane.showMessageDialog(null, ex.getErrorMessage().getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
						} catch (IllegalArgumentException ea) {
							JOptionPane.showMessageDialog(null, ea.getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
						}

					}
				}
			});
			actionButtonsPanel.add(createPackageButton);
			mainPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

			baseFrame.add(mainPanel);
			baseFrame.pack();
			baseFrame.setSize(600, 450);
			baseFrame.setVisible(true);
			baseFrame.setLocationRelativeTo(null);
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					log.info("Removing tmpFolder " + packer.getTmpFolder());
					deleteDirectory(packer.getTmpFolder());
				}
			}));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteDirectory(String directory) {
		deleteDirectory(new File(directory));
	}

	private void deleteDirectory(File directory) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				deleteDirectory(file);
			} else {
				file.delete();
			}
		}
		directory.delete();
	}

	private JFileChooser createSaveAsChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Salvar em...");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos ZIP", "zip"));
		return fileChooser;
	}

	private Packer createPackerInstance() {
		Packer packer = new Packer();
		List<String> exceptionFiles = new ArrayList<>();
		exceptionFiles.add("pom.xml");
		exceptionFiles.add("target");
		packer.setExceptionFiles(exceptionFiles);
		return packer;
	}

	private void removeAllRows(DefaultTableModel model) {
		int totalRows = model.getRowCount();
		for (int i = totalRows - 1; i >= 0; i--) {
			model.removeRow(i);
		}
		model.fireTableDataChanged();
	}
}
