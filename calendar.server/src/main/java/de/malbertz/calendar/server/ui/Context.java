package de.malbertz.calendar.server.ui;

import de.malbertz.calendar.server.core.Server;

/**
 * This class provides a static reference to the server.
 * 
 * @author Michael Albertz
 *
 */
public class Context {
	private final static Context instance = new Context();

	/**
	 * Get the current instance.
	 * 
	 * @return the instance
	 */
	public static Context getInstance() {
		return instance;
	}

	/** Holds the server thats referenced to */
	private Server server = null;

	/**
	 * Get the server thats referenced to.
	 * 
	 * @return the server
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Set the server to reference to.
	 * 
	 * @param server
	 *            the server to reference to
	 */
	public void setServer(Server server) {
		this.server = server;
	}
}
