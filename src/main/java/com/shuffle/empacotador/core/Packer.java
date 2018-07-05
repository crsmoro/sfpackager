package com.shuffle.empacotador.core;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.shuffle.empacotador.exception.SFPackagerException;

public interface Packer {

	String getSourceFolder();

	void setSourceFolder(String sourceFolder);

	String getDestinationFolder();

	void setDestinationFolder(String destinationFolder);

	String getTmpFolder();

	boolean isCreateZip();

	void setCreateZip(boolean createZip);

	String getZipName();

	void setZipName(String zipName);

	List<File> getPendingFiles() throws SFPackagerException;

	Map<File, File> getPatchFiles() throws SFPackagerException;

	boolean isClass(File file);

	boolean isWebapp(File file);

	boolean isResource(File file);

	boolean isPackCompiledClasses();

	void setPackCompiledClasses(boolean packCompiledClasses);

	List<String> getExceptionFiles();

	void setExceptionFiles(List<String> exceptionFiles);

	String getFullPathSource(File file);

	String getFullPathDestination(File file);

	String getStructuredPath(File destination);

	void createPatch() throws SFPackagerException;

	void createPatch(Map<File, File> files) throws SFPackagerException;

	static Packer newIstance(String type) {
		try {
			return Class.forName("com.shuffle.empacotador.core.impl." + type + "Packer").asSubclass(Packer.class).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}