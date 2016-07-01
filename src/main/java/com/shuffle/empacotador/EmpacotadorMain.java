package com.shuffle.empacotador;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import com.shuffle.empacotador.swing.main.Main;

public class EmpacotadorMain {

	private transient static final Log log = LogFactory.getLog(EmpacotadorMain.class);

	public static void main(String[] args) {
		initLogger();
		new Main();
	}

	private static void initLogger() {
		Logger rootLogger = Logger.getRootLogger();
		PatternLayout layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");

		try {
			RollingFileAppender fileAppender = new RollingFileAppender(layout, System.getProperty("log.file", "./sfpackager.log"));
			fileAppender.setImmediateFlush(true);
			fileAppender.setThreshold(Level.DEBUG);
			fileAppender.setAppend(true);
			fileAppender.setMaxFileSize("5MB");
			fileAppender.setMaxBackupIndex(2);

			rootLogger.addAppender(fileAppender);
		} catch (IOException e) {
			log.error("Failed to add appender !!", e);
		}
	}
}
