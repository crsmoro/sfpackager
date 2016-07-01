package com.shuffle.empacotador.swing.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
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
import com.shuffle.empacotador.swing.main.button.ChooseDestinationButton;
import com.shuffle.empacotador.swing.main.button.CreatePackageButton;
import com.shuffle.empacotador.swing.main.button.ViewFilesButton;
import com.shuffle.empacotador.swing.main.field.DestinationField;
import com.shuffle.empacotador.swing.main.field.SourceField;
import com.shuffle.empacotador.swing.main.group.SourcePanel;
import com.shuffle.empacotador.swing.main.table.model.FilesModel;
import com.shuffle.empacotador.swing.main.table.model.FilesScrollTable;

public class Main {
	private transient static final Log log = LogFactory.getLog(EmpacotadorMain.class);

	private Dimension labelDimension = new Dimension(80, 25);

	public Main() {
		try {
			BaseFrame frame = new BaseFrame("Empacotador");

			JPanel jPanel = new JPanel(new BorderLayout());

			JPanel jPanelCenter = new JPanel();
			jPanelCenter.setLayout(new BoxLayout(jPanelCenter, BoxLayout.Y_AXIS));
			jPanel.add(jPanelCenter, BorderLayout.CENTER);
			JPanel jPanelFields = new JPanel(new BorderLayout());

			JPanel panelOrigens = new JPanel();
			panelOrigens.setLayout(new BoxLayout(panelOrigens, BoxLayout.Y_AXIS));

			SourcePanel sourcePanel = new SourcePanel();
			SourceField origemField = sourcePanel.getSourceField();
			panelOrigens.add(sourcePanel);

			JPanel jPanelDestino = new JPanel(new FlowLayout(FlowLayout.LEFT));
			DestinationField destinationField = new DestinationField("Salvar em", 30);
			destinationField.getLabel().setPreferredSize(labelDimension);
			jPanelDestino.add(destinationField);
			ChooseDestinationButton destinationButton = new ChooseDestinationButton(destinationField.getField());
			jPanelDestino.add(destinationButton);

			jPanelFields.add(panelOrigens, BorderLayout.NORTH);
			jPanelFields.add(jPanelDestino, BorderLayout.SOUTH);
			jPanelCenter.add(jPanelFields);

			FilesModel filesModel = new FilesModel();

			FilesScrollTable filesScrollTable = new FilesScrollTable(filesModel);
			jPanelCenter.add(filesScrollTable);

			JPanel jPanelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			ViewFilesButton viewFilesButton = new ViewFilesButton();
			viewFilesButton.addClickAction(new ClickAction() {

				@Override
				public void click() {
					if (StringUtils.isBlank(origemField.getField().getText()) || StringUtils.isBlank(destinationField.getField().getText())) {
						JOptionPane.showMessageDialog(null, "Preencha todos os campos", "Campos não preenchidos", JOptionPane.WARNING_MESSAGE);
					} else {

						try {
							removeAllRows(filesModel);
							Packer packer = new Packer();
							packer.setSourceFolder(origemField.getField().getText());
							packer.getExceptionFiles().add("pom.xml");
							packer.setDestinationFolder(destinationField.getField().getText().substring(0, destinationField.getField().getText().lastIndexOf(File.separator)));
							Map<File, File> patchFiles = packer.getPatchFiles();
							for (File source : patchFiles.keySet()) {
								File destination = patchFiles.get(source);
								filesModel.addRow(new Object[] { Boolean.TRUE, destination.getAbsolutePath().replace(packer.getDestinationFolder() + File.separator, ""), source, destination });
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
			jPanelButtons.add(viewFilesButton);

			CreatePackageButton createPackageButton = new CreatePackageButton();
			createPackageButton.addClickAction(new ClickAction() {

				@Override
				public void click() {
					if (StringUtils.isBlank(origemField.getField().getText()) || StringUtils.isBlank(destinationField.getField().getText())) {
						JOptionPane.showMessageDialog(null, "Preencha todos os campos", "Campos não preenchidos", JOptionPane.WARNING_MESSAGE);
					} else if (filesModel.getRowCount() <= 0) {
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
								Packer packer = new Packer();
								packer.setSourceFolder(origemField.getField().getText());
								packer.getExceptionFiles().add("pom.xml");
								packer.setDestinationFolder(destinationField.getField().getText().substring(0, destinationField.getField().getText().lastIndexOf(File.separator)));
								packer.setZipName(destinationField.getField().getText().substring(destinationField.getField().getText().lastIndexOf(File.separator) + 1, destinationField.getField().getText().length()));
								packer.createPatch(filesForPatch);
								JOptionPane.showMessageDialog(null, "Pacote gerado com sucesso disponível em " + destinationField.getField().getText(), "Concluído", JOptionPane.INFORMATION_MESSAGE);
							}
						} catch (SVNException ex) {
							JOptionPane.showMessageDialog(null, ex.getErrorMessage().getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
						} catch (IllegalArgumentException ea) {
							JOptionPane.showMessageDialog(null, ea.getMessage(), "Problema na geração", JOptionPane.ERROR_MESSAGE);
						}

					}
				}
			});
			jPanelButtons.add(createPackageButton);
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

	private void removeAllRows(DefaultTableModel model) {
		int totalRows = model.getRowCount();
		for (int i = totalRows - 1; i >= 0; i--) {
			model.removeRow(i);
		}
		model.fireTableDataChanged();
	}
}
