package com.shuffle.empacotador.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

public class Packer {

	private transient static final Log log = LogFactory.getLog(Packer.class);

	private List<File> pendingFiles = new ArrayList<>();

	private String sourceFolder;

	private String destinationFolder;

	private boolean createZip = true;

	private String zipName;

	private boolean packCompiledClasses = true;

	private List<String> exceptionFiles = new ArrayList<>();

	private final String matchClassFile = File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;

	private final String matchWebappFile = File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator;

	private final String matchResourceFile = File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator;

	private final String sourceFolderClasses = matchClassFile;

	private final String compiledSourceFolderClasses = File.separator + "target" + File.separator + "classes" + File.separator;

	private final String compiledDestinationFolderClasses = File.separator + "WEB-INF" + File.separator + "classes" + File.separator;

	public String getSourceFolder() {
		return sourceFolder;
	}

	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public String getDestinationFolder() {
		return destinationFolder;
	}

	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public boolean isCreateZip() {
		return createZip;
	}

	public void setCreateZip(boolean createZip) {
		this.createZip = createZip;
	}

	public String getZipName() {
		return zipName;
	}

	public void setZipName(String zipName) {
		this.zipName = zipName;
	}

	public List<File> getPendingFiles() throws SVNException {
		pendingFiles.clear();
		SVNClientManager svnClientManager = SVNClientManager.newInstance();
		svnClientManager.getStatusClient().doStatus(new File(getSourceFolder()), SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
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
					log.info("File " + status.getFile().getName() + " is on exception list, it wont be added on pending files");
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
	}

	/**
	 * Use to override the pending files to crate the patch
	 * 
	 * @param pendingFiles
	 */
	public void setPendingFiles(List<File> pendingFiles) {
		this.pendingFiles = pendingFiles;
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

	public boolean isClass(File file) {
		return file.getAbsolutePath().contains(matchClassFile);
	}

	public boolean isWebapp(File file) {
		return file.getAbsolutePath().contains(matchWebappFile);
	}

	public boolean isResource(File file) {
		return file.getAbsolutePath().contains(matchResourceFile);
	}

	public boolean isPackCompiledClasses() {
		return packCompiledClasses;
	}

	public void setPackCompiledClasses(boolean packCompiledClasses) {
		this.packCompiledClasses = packCompiledClasses;
	}

	public List<String> getExceptionFiles() {
		return exceptionFiles;
	}

	public void setExceptionFiles(List<String> exceptionFiles) {
		this.exceptionFiles = exceptionFiles;
	}

	public String getFullPathSource(File file) {
		String fullPathCopyFile = file.getAbsolutePath();
		if (!file.isDirectory()) {
			if (isClass(file)) {
				fullPathCopyFile = fullPathCopyFile.replace(sourceFolderClasses, compiledSourceFolderClasses);
				if (isPackCompiledClasses()) {
					fullPathCopyFile = fullPathCopyFile.replace(".java", ".class");
				}
			}
		}
		log.info("Origem Caminho a copiar : " + fullPathCopyFile);
		return fullPathCopyFile;
	}

	public String getFullPathDestination(File file) {
		String fullPathDestino = file.getAbsolutePath();
		if (!file.isDirectory()) {
			if (isClass(file)) {
				fullPathDestino = fullPathDestino.replace(getSourceFolder(), getDestinationFolder()).replace(sourceFolderClasses, compiledDestinationFolderClasses);
				if (isPackCompiledClasses()) {
					fullPathDestino = fullPathDestino.replace(".java", ".class");
				}
			} else if (isWebapp(file)) {
				fullPathDestino = fullPathDestino.replace(getSourceFolder() + matchWebappFile, getDestinationFolder() + File.separator);
			} else if (isResource(file)) {
				fullPathDestino = fullPathDestino.replace(getSourceFolder() + matchResourceFile, getDestinationFolder() + compiledDestinationFolderClasses);
			} else {
				fullPathDestino = fullPathDestino.replace(getSourceFolder() + File.separator, getDestinationFolder() + File.separator);
			}
		}
		log.info("Destino Caminho a copiar : " + fullPathDestino);
		return fullPathDestino;
	}

	public void createPatch() throws SVNException {
		if (StringUtils.isBlank(getDestinationFolder()) || !new File(getDestinationFolder()).isDirectory() || new File(getDestinationFolder()).list().length > 0) {
			throw new IllegalArgumentException("Destination folder not valid");
		}
		if (pendingFiles.isEmpty()) {
			getPendingFiles();
		}
		for (File pendingFiles : pendingFiles) {
			String source = getFullPathSource(pendingFiles);
			String destination = getFullPathDestination(pendingFiles);
			createFolder(destination.substring(0, destination.lastIndexOf(File.separator)), getDestinationFolder());

			File fileSource = new File(source);
			String wildcard = fileSource.getName().substring(0, fileSource.getName().lastIndexOf(".")) + "*" + fileSource.getName().substring(fileSource.getName().lastIndexOf("."), fileSource.getName().length());
			String sourceFolder = source.substring(0, source.lastIndexOf(File.separator));
			File fileSourceFolder = new File(sourceFolder);
			log.debug("wildcard : " + wildcard);
			log.debug("source folder : " + sourceFolder);
			
			FileUtils.iterateFiles(fileSourceFolder, new WildcardFileFilter(wildcard, IOCase.INSENSITIVE), null).forEachRemaining(file -> {
				try {
					String newSource = getFullPathSource(file);
					String newDestination = getFullPathDestination(new File(pendingFiles.getParent() + File.separator + file.getName()));
					log.debug("newSource : " + newSource);
					log.debug("newDestination : " + newDestination);
					FileInputStream fileInputStreamSource = new FileInputStream(newSource);
					FileOutputStream fileOutputStreamDestination = new FileOutputStream(newDestination);
					IOUtils.copy(fileInputStreamSource, fileOutputStreamDestination);
					fileInputStreamSource.close();
					fileOutputStreamDestination.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

		}
		if (isCreateZip()) {
			Zip zip = new Zip(getDestinationFolder());
			zip.generateFileList(new File(getDestinationFolder()));
			zip.zipIt(getDestinationFolder() + File.separator + (StringUtils.isNotBlank(getZipName()) ? getZipName() : "pacote.zip"));
		}
	}

	private void createFolder(String path, String baseFolder) {
		String[] folders = path.replace(baseFolder + File.separator, "").split(File.separator + File.separator);
		log.debug(Arrays.toString(folders));
		if (folders.length > 1) {
			createFolder(StringUtils.join(folders, File.separator, 1, folders.length), baseFolder + File.separator + folders[0]);
		} else {
			File folder = new File(baseFolder + File.separator + path);
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
	}
}