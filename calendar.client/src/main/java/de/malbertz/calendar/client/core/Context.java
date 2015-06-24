package de.malbertz.calendar.client.core;

/**
 * This class provides a static reference to the client the application is
 * currently running under.
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

	/** The client thats referenced to */
	private Client client = new Client();

	/**
	 * Get the client thats referenced to.
	 * 
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Set the client to reference to.
	 * 
	 * @param client
	 *            the client to reference to
	 */
	public void setClient(Client client) {
		this.client = client;
	}
}
