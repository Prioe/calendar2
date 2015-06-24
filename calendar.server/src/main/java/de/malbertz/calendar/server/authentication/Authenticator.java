package de.malbertz.calendar.server.authentication;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements static methods to manage the user database of the
 * server.
 * 
 * @author Michael Albertz
 *
 */
public class Authenticator {

	private static final Logger log = LogManager.getLogger(Authenticator.class);
	/** Holds the properties that contain the user-password key-value pairs */
	private static Properties table = new Properties();
	/** Holds the file path of the property file */
	private static final String DATA_PATH = "/table.properties";

	/*
	 * Initiate the property object before the first call of a method.
	 */
	static {
		try {
			log.debug("Loading name-password-table ...");
			table.load(Authenticator.class.getResourceAsStream(DATA_PATH));
			log.debug("Successfully loaded name-password-table");
		} catch (Exception e) {
			log.fatal("name-password-table could not be loaded. Exiting ...");
			System.exit(1);
		}
	}

	/**
	 * This method is used to call the static-block of {@link Authenticator}
	 * manually.
	 * <p>
	 * If no method of {@link Authenticator} has been called yet. The static
	 * block will be executed. It is a noop.
	 */
	public static void make() {
		/*
		 * Do nothing, the static block will be executed if it hasn't been yet.
		 */
	}

	/**
	 * Get an enumeration of all user-password key-value pairs.
	 * 
	 * @return an enumeration of all user-password key-value pairs
	 */
	public static Enumeration<Object> getAll() {
		return table.keys();
	}

	/**
	 * This method removes a user by a specified name from the user-password
	 * table.
	 * 
	 * @param name
	 *            the name of the user to remove
	 * @return was the operation successful
	 */
	public static boolean remove(String name) {
		String prop = table.getProperty(name);
		if (prop != null) {
			table.remove(name);
			store();
			return true;
		}
		return false;
	}

	/**
	 * This method adds a new user with specified name and password to the
	 * user-password-table.
	 * 
	 * @param name
	 *            the name of the new user
	 * @param password
	 *            the password for the new user
	 * @return was the operation successful
	 */
	public static boolean add(String name, String password) {
		String prop = table.getProperty(name);
		if (prop == null) {
			table.setProperty(name, password);
			store();
			table.getProperty(name);
			return true;
		}
		return false;
	}

	/**
	 * This method authenticates a user.
	 * <p>
	 * It compares specified name and password to the name-password table.
	 * 
	 * @param name
	 *            the name of the user to be authenticated
	 * @param password
	 *            the password of the user to be authenticated
	 * @return does the user exist and are the credentials correct
	 */
	public static boolean authenticate(String name, String password) {
		log.debug("Trying to authenticate: name=" + name + ",password="
				+ password);
		String prop = table.getProperty(name);
		if (prop != null) {
			return prop.equals(password);
		}
		return false;
	}

	/** Stores the current properties. */
	private static void store() {
		try (FileOutputStream os = new FileOutputStream(new File(
				Authenticator.class.getResource(DATA_PATH).toURI()))) {
			table.store(os, null);
		} catch (Exception e) {
			log.fatal("Failed to store name-password-table.", e);
		}
	}

	/**
	 * This method checks if a user by the specified name exists in the
	 * name-password table.
	 * 
	 * @param name
	 *            the name of the user
	 * @return does the user exist
	 */
	public static boolean exists(String name) {
		return null != table.getProperty(name);

	}

	private Authenticator() {
	}

}
