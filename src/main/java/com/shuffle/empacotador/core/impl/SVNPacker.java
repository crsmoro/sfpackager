package com.shuffle.empacotador.core.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import com.shuffle.empacotador.core.AbstractPacker;
import com.shuffle.empacotador.core.Packer;
import com.shuffle.empacotador.exception.SFPackagerException;

public class SVNPacker extends AbstractPacker implements Packer {

	private transient static final Log log = LogFactory.getLog(SVNPacker.class);

	private List<File> pendingFiles = new ArrayList<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getPendingFiles()
	 */
	@Override
	public List<File> getPendingFiles() throws SFPackagerException {

		try {
			pendingFiles.clear();
			SVNClientManager svnClientManager = SVNClientManager.newInstance();
			svnClientManager.getStatusClient().doStatus(new File(getSourceFolder()), SVNRevision.WORKING,
					SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
						@Override
						public void handleStatus(SVNStatus status) throws SVNException {
							SVNStatusType statusType = status.getNodeStatus();
							log.info("---");
							log.info("statusType : " + statusType);
							if (statusType == SVNStatusType.STATUS_DELETED) {
								log.info("File " + status.getFile().getName() + " deleted, no need to be in the patch");
								return;
							}
							if (getExceptionFiles().contains(status.getFile().getName())) {
								log.info("File " + status.getFile().getName()
										+ " is on exception list, it wont be added on pending files");
								return;
							}
							log.info("Last Author : " + (status.getAuthor() != null ? status.getAuthor() : "-"));
							log.info("Is Folder : " + status.getFile().isDirectory());
							log.info("Path : " + status.getFile().getAbsolutePath());
							if (!status.getFile().isDirectory()) {
								pendingFiles.add(status.getFile());
							}
							if (statusType == SVNStatusType.STATUS_UNVERSIONED) {
								log.info("New File/Folder");
								if (status.getFile().isDirectory()) {
									pendingFiles.addAll(getAllFiles(status.getFile()));
								}
							}
						}

					}, null);
			return pendingFiles;
		} catch (SVNException e) {
			SFPackagerException sfPackagerException = new SFPackagerException();
			sfPackagerException.initCause(e);
			;
			throw sfPackagerException;
		}
	}

	private List<File> getAllFiles(File file) {
		List<File> files = new ArrayList<>();
		if (file.isDirectory()) {
			for (File subFiles : file.listFiles()) {
				files.addAll(getAllFiles(subFiles));
			}
		} else {
			files.add(file);
		}
		return files;
	}
}
