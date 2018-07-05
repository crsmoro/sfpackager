package com.shuffle.empacotador.core.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.shuffle.empacotador.core.AbstractPacker;
import com.shuffle.empacotador.core.Packer;
import com.shuffle.empacotador.exception.SFPackagerException;

public class GitPacker extends AbstractPacker implements Packer {

	private transient static final Log log = LogFactory.getLog(SVNPacker.class);

	@Override
	public List<File> getPendingFiles() throws SFPackagerException {
		Set<String> pendingFiles = new HashSet<>();
		try (Git git = Git.open(new File(getSourceFolder()))) {
			Status status = git.status().call();
			for (String file : status.getUncommittedChanges()) {
				log.trace("Uncommitted " + file);
				pendingFiles.add(file);
			}
			for (String file : status.getUntracked()) {
				log.trace("Untracked " + file);
				pendingFiles.add(file);
			}
			for (String file : status.getModified()) {
				log.trace("Modified " + file);
				pendingFiles.add(file);
			}
			for (String file : status.getAdded()) {
				log.trace("Added " + file);
				pendingFiles.add(file);
			}
			for (String file : status.getChanged()) {
				log.trace("Changed " + file);
				pendingFiles.add(file);
			}
			pendingFiles.removeAll(getExceptionFiles());
			return new ArrayList<>(pendingFiles.stream().map(f -> new File(getSourceFolder() + File.separator + f)).collect(Collectors.toList()));
		} catch (IOException e) {
			throw new SFPackagerException("Repositório Git não encontrado");
		} catch (GitAPIException e) {
			throw new SFPackagerException("Problema ao buscar as alterações no Git");
		}
	}

}
