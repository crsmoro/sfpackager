package com.shuffle.empacotador.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.shuffle.empacotador.exception.SFPackagerException;

public abstract class AbstractPacker implements Packer {

	private transient static final Log log = LogFactory.getLog(AbstractPacker.class);

	private Map<File, File> patchFiles = new HashMap<>();

	private String sourceFolder;

	private String destinationFolder;

	private String tmpFolder;

	private boolean createZip = true;

	private String zipName;

	private boolean packCompiledClasses = true;

	private List<String> exceptionFiles = new ArrayList<>();

	private final String matchClassFile = File.separator + "src" + File.separator + "main" + File.separator + "java"
			+ File.separator;

	private final String matchWebappFile = File.separator + "src" + File.separator + "main" + File.separator + "webapp"
			+ File.separator;

	private final String matchResourceFile = File.separator + "src" + File.separator + "main" + File.separator
			+ "resources" + File.separator;

	private final String sourceFolderClasses = matchClassFile;

	private final String compiledSourceFolderClasses = File.separator + "target" + File.separator + "classes"
			+ File.separator;

	private final String compiledDestinationFolderClasses = File.separator + "WEB-INF" + File.separator + "classes"
			+ File.separator;

	public AbstractPacker() {
		updateTmpFolder();
	}

	private void updateTmpFolder() {
		if (StringUtils.isNotBlank(this.tmpFolder) && new File(this.tmpFolder).exists()) {
			log.info("Removing old temp folder " + this.tmpFolder);
			deleteDirectoryContent(this.tmpFolder);
		}
		this.tmpFolder = System.getProperty("java.io.tmpdir") + new Date().getTime();
		log.info("tmpFolder : " + this.tmpFolder);
		File tmpFile = new File(this.tmpFolder);
		tmpFile.mkdir();
	}

	private void deleteDirectoryContent(String directory) {
		deleteDirectoryContent(new File(directory));
	}

	private void deleteDirectoryContent(File directory) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				deleteDirectoryContent(file);
			} else {
				file.delete();
			}
		}
		if (!new File(getTmpFolder()).getAbsolutePath().equals(directory.getAbsolutePath())) {
			directory.delete();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getSourceFolder()
	 */
	@Override
	public String getSourceFolder() {
		return sourceFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#setSourceFolder(java.lang.String)
	 */
	@Override
	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getDestinationFolder()
	 */
	@Override
	public String getDestinationFolder() {
		return destinationFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.shuffle.empacotador.core.IPacker#setDestinationFolder(java.lang.String)
	 */
	@Override
	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getTmpFolder()
	 */
	@Override
	public String getTmpFolder() {
		return isCreateZip() ? tmpFolder : getDestinationFolder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#isCreateZip()
	 */
	@Override
	public boolean isCreateZip() {
		return createZip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#setCreateZip(boolean)
	 */
	@Override
	public void setCreateZip(boolean createZip) {
		this.createZip = createZip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getZipName()
	 */
	@Override
	public String getZipName() {
		return zipName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#setZipName(java.lang.String)
	 */
	@Override
	public void setZipName(String zipName) {
		this.zipName = zipName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getPatchFiles()
	 */
	@Override
	public Map<File, File> getPatchFiles() throws SFPackagerException {
		patchFiles.clear();
		for (File pendingFiles : getPendingFiles()) {
			String sourceFullPath = getFullPathSource(pendingFiles);
			String destinarionFullPath = getFullPathDestination(pendingFiles);
			patchFiles.put(new File(sourceFullPath), new File(destinarionFullPath));
			if (isPackCompiledClasses() && isClass(pendingFiles)) {
				File pendingFileSource = new File(sourceFullPath);
				String wildcard = pendingFileSource.getName().substring(0, pendingFileSource.getName().lastIndexOf("."))
						+ "$*" + pendingFileSource.getName().substring(pendingFileSource.getName().lastIndexOf("."),
								pendingFileSource.getName().length());
				log.trace("wildcard : " + wildcard);

				FileUtils.iterateFiles(pendingFileSource.getParentFile(),
						new WildcardFileFilter(wildcard, IOCase.INSENSITIVE), null).forEachRemaining(file -> {
							String newSourceFullPath = getFullPathSource(file);
							String newDestinationFullPath = getFullPathDestination(
									new File(pendingFiles.getParent() + File.separator + file.getName()));
							log.trace("newSourceFullPath : " + newSourceFullPath);
							log.trace("newDestinationFullPath : " + newDestinationFullPath);

							File fileSource = new File(newSourceFullPath);
							File fileDestination = new File(newDestinationFullPath);

							patchFiles.put(fileSource, fileDestination);
						});
			}
		}

		return patchFiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#isClass(java.io.File)
	 */
	@Override
	public boolean isClass(File file) {
		return file.getAbsolutePath().contains(matchClassFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#isWebapp(java.io.File)
	 */
	@Override
	public boolean isWebapp(File file) {
		return file.getAbsolutePath().contains(matchWebappFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#isResource(java.io.File)
	 */
	@Override
	public boolean isResource(File file) {
		return file.getAbsolutePath().contains(matchResourceFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#isPackCompiledClasses()
	 */
	@Override
	public boolean isPackCompiledClasses() {
		return packCompiledClasses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#setPackCompiledClasses(boolean)
	 */
	@Override
	public void setPackCompiledClasses(boolean packCompiledClasses) {
		this.packCompiledClasses = packCompiledClasses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getExceptionFiles()
	 */
	@Override
	public List<String> getExceptionFiles() {
		return exceptionFiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#setExceptionFiles(java.util.List)
	 */
	@Override
	public void setExceptionFiles(List<String> exceptionFiles) {
		this.exceptionFiles = exceptionFiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getFullPathSource(java.io.File)
	 */
	@Override
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
		log.trace("Origem Caminho a copiar : " + fullPathCopyFile);
		return fullPathCopyFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.shuffle.empacotador.core.IPacker#getFullPathDestination(java.io.File)
	 */
	@Override
	public String getFullPathDestination(File file) {
		String fullPathDestino = file.getAbsolutePath();
		if (!file.isDirectory()) {
			if (isClass(file)) {
				fullPathDestino = fullPathDestino.replace(getSourceFolder(), getTmpFolder())
						.replace(sourceFolderClasses, compiledDestinationFolderClasses);
				if (isPackCompiledClasses()) {
					fullPathDestino = fullPathDestino.replace(".java", ".class");
				}
			} else if (isWebapp(file)) {
				fullPathDestino = fullPathDestino.replace(getSourceFolder() + matchWebappFile,
						getTmpFolder() + File.separator);
			} else if (isResource(file)) {
				fullPathDestino = fullPathDestino.replace(getSourceFolder() + matchResourceFile,
						getTmpFolder() + compiledDestinationFolderClasses);
			} else {
				fullPathDestino = fullPathDestino.replace(getSourceFolder() + File.separator,
						getTmpFolder() + File.separator);
			}
		}
		log.trace("Destino Caminho a copiar : " + fullPathDestino);
		return fullPathDestino;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#getStructuredPath(java.io.File)
	 */
	@Override
	public String getStructuredPath(File destination) {
		return destination.getAbsolutePath().replace(tmpFolder + File.separator, "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#createPatch()
	 */
	@Override
	public void createPatch() throws SFPackagerException {
		createPatch(getPatchFiles());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.shuffle.empacotador.core.IPacker#createPatch(java.util.Map)
	 */
	@Override
	public void createPatch(Map<File, File> files) throws SFPackagerException {
		if (StringUtils.isBlank(getDestinationFolder()) || !new File(getDestinationFolder()).isDirectory()
				|| (!isCreateZip() && new File(getDestinationFolder()).list().length > 0)) {
			throw new IllegalArgumentException("Destination folder not valid");
		}
		deleteDirectoryContent(getTmpFolder());
		for (File source : files.keySet()) {
			File destination = files.get(source);
			createFolder(destination.getParent(), getTmpFolder());
			try {
				FileInputStream fileInputStreamSource = new FileInputStream(source);
				FileOutputStream fileOutputStreamDestination = new FileOutputStream(destination);
				IOUtils.copy(fileInputStreamSource, fileOutputStreamDestination);
				fileInputStreamSource.close();
				fileOutputStreamDestination.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (isCreateZip()) {
			Zip zip = new Zip(getTmpFolder());
			zip.generateFileList(new File(getTmpFolder()));
			zip.zipIt(getDestinationFolder() + File.separator
					+ (StringUtils.isNotBlank(getZipName()) ? getZipName() : "patch.zip"));
		}
		deleteDirectoryContent(getTmpFolder());
	}

	private void createFolder(String path, String baseFolder) {
		String[] folders = path.replace(baseFolder + File.separator, "").split(File.separator + File.separator);
		log.trace(Arrays.toString(folders));
		if (folders.length > 1) {
			createFolder(StringUtils.join(folders, File.separator, 1, folders.length),
					baseFolder + File.separator + folders[0]);
		} else {
			File folder = new File(baseFolder + File.separator + path);
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
	}
}
