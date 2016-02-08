package org.stoevesand.brain.persistence;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stoevesand.brain.BrainSystem;
import org.stoevesand.brain.config.BrainConfigHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Administration {

	private static Logger log = LogManager.getLogger(Administration.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File dbfile = new File("resources/brain_db_basic.xml");
		loadDB(dbfile);
	}

	public static void createDB() {
	}

	public static void loadDB(File file) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			BrainDBHandler brainDBHandler = new BrainDBHandler();
			SAXParser parser = factory.newSAXParser();
			parser.parse(file, brainDBHandler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void loadConfig(File configFile, BrainSystem brainSystem) {
		log.debug("Load config.");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			BrainConfigHandler brainConfigHandler = new BrainConfigHandler(brainSystem);
			SAXParser parser = factory.newSAXParser();
			parser.parse(configFile, brainConfigHandler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadLesson(InputStream inputStream) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {

			InputSource is = new InputSource(inputStream);
			is.setEncoding("utf-8");

			BrainDBHandler brainDBHandler = new BrainDBHandler();
			SAXParser parser = factory.newSAXParser();
			parser.parse(is, brainDBHandler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



}
