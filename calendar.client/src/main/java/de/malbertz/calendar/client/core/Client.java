package de.malbertz.calendar.client.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar2.CalendarEntry;
import de.malbertz.calendar2.ServerCommand;
import de.malbertz.calendar2.ServerCommand.Command;
import de.malbertz.calendar2.util.SerializationUtils;

/**
 * This class implements the Client for the application.
 * 
 * @author Michael Albertz
 *
 */
public class Client extends Observable {

	private static final Logger log = LogManager.getLogger(Client.class);
	/** The socket used by the client */
	private Socket socket;
	/**
	 * The OutputStream used by the client.
	 * 
	 * @see java.io.ObjectOutputStream
	 */
	private ObjectOutputStream out;
	/**
	 * The InputStream used by the client.
	 * 
	 * @see java.io.ObjectInputStream
	 */
	private ObjectInputStream in;
	/**
	 * The state the client is currently at.
	 * 
	 * @see de.malbertz.calendar.client.core.Client.ClientState
	 */
	private ClientState state;
	/** The port the socket is connected to. */
	private int port;
	/** The hostname of the server the socket is connected to. */
	private String hostName;
	/**
	 * A {@link List} containing {@link CalendarEntry} objects that holds the
	 * entrys that were received while authenticating.
	 */
	private List<CalendarEntry> list;
	/** The authenticated username of the client. */
	private String userName;

	/*
	 * Creates an unconnected client.
	 */
	public Client() {
		state = ClientState.NOT_CONNECTED;
	}

	/**
	 * Creates a client, connects it to the specified port number on the named
	 * host, tries to authenticate it.
	 * <p>
	 * After the authentication was successful, the client will request the data
	 * stored for the authenticated user. If the data was successfully received
	 * it will be stored in a list that can be accessed by calling
	 * {@link #getList()}.
	 * <p>
	 * After that the client will start a heartbeat thread that sends a
	 * heartbeat every 15 seconds and waits for a response. If no response was
	 * received or the heartbeat fails in any other way, the client will be
	 * closed.
	 * 
	 * @param hostName
	 *            the hostname of the server to connect to
	 * @param port
	 *            the port on the specified host to connect to
	 * @param name
	 *            the username to authenticate with
	 * @param password
	 *            the password to authenticate with
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void connect(String hostName, int port, String name, String password)
			throws IOException {
		disconnect();
		if (state == ClientState.NOT_CONNECTED) {
			this.hostName = hostName;
			this.port = port;
			this.socket = new Socket(hostName, port);

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			state = ClientState.CONNECTED;

			// Authentication
			authenticate(name, password);

			// request saved data
			requestData();

			if (state == ClientState.AUTHENTICATED) {
				Thread heartbeat = new Thread(new HeartBeat());
				heartbeat.setDaemon(true);
				heartbeat.start();
			}

		}
	}

	/**
	 * This method tries to authenticate the client.
	 * <p>
	 * If the authentication was successful the client {@link #state} will be
	 * set to {@link ClientState#AUTHENTICATED}.
	 * 
	 * @param name
	 *            the username to authenticate with
	 * @param password
	 *            the password to authenticate with
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private void authenticate(String name, String password) throws IOException {
		send(new String[] { name, password });
		try {
			Object obj;
			if (state == ClientState.CONNECTED) {
				if ((obj = in.readObject()) != null) {
					if (Boolean.class.isInstance(obj)) {
						log.info("Received from server: " + obj);
						if ((boolean) obj == true) {
							state = ClientState.AUTHENTICATED;
							userName = name;
							log.info("Client is authenticated");
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			log.fatal("Class of a serialized object cannot be found.", e);
		}
	}

	/**
	 * Requests the data stored for the authenticated user.
	 * <p>
	 * If the client {@link #state} is not {@link ClientState#AUTHENTICATED} its
	 * a noop.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private void requestData() throws IOException {
		if (state != ClientState.AUTHENTICATED) {
			return;
		}
		list = new ArrayList<>();
		send(new ServerCommand(Command.REQUEST_ALL, null, null));

		try {
			Object obj = in.readObject();
			if (obj instanceof byte[]) {
				try {
					CalendarEntry[] entrys = SerializationUtils.unpickle(
							(byte[]) obj, CalendarEntry[].class);
					for (CalendarEntry calendarEntry : entrys) {
						list.add(calendarEntry);
					}
				} catch (IOException | ClassNotFoundException e) {
					log.fatal("Can't load deserialize existing entry", e);
				}

			}
		} catch (ClassNotFoundException e) {
			log.fatal("Class of a serialized object cannot be found.", e);
		}
		log.info("Received Data:");
		for (CalendarEntry calendarEntry : list) {
			log.info(calendarEntry);
		}

	}

	/**
	 * Sends a {@link ServerCommand} to the server.
	 * 
	 * @param command
	 *            the command to send
	 */
	public void sendCommand(ServerCommand command) {
		send(command);
	}

	/**
	 * This method is used to send Objects via ObjectOutputStream {@link out} to
	 * the connected server.
	 * <p>
	 * It prevents the user from sending unauthorized Objects to the server.
	 * 
	 * @param obj
	 *            the Object to send
	 */
	private void send(Object obj) {
		try {
			if (state == ClientState.CONNECTED) {
				if (obj instanceof String[] && ((String[]) obj).length == 2) {
					out.writeObject(obj);
				} else {
					throw new IllegalArgumentException(
							"Invalid arguments for ClientState." + state);
				}
			} else if (state == ClientState.AUTHENTICATED) {
				if (obj instanceof ServerCommand) {
					out.writeObject(SerializationUtils
							.pickle((ServerCommand) obj));
				} else {
					throw new IllegalArgumentException("Illegal object type");
				}
			} else {
				throw new IOException("Not connected to a Server");
			}
			log.info("Sent: " + obj);
		} catch (IOException e) {
			disconnect();
		}
	}

	/**
	 * This method sends an object to the connected server and waits for a
	 * response.
	 * 
	 * @param obj
	 *            the object to send to the connected server
	 * @return the response received by the connected server
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ClassNotFoundException
	 *             if the Class of a serialized object cannot be found
	 */
	private Object request(Object obj) throws IOException,
			ClassNotFoundException {
		send(obj);
		return in.readObject();
	}

	/**
	 * This method tries to close the socket, sets the {@link ClientState} of
	 * the client to {@link ClientState#NOT_CONNECTED} and notifies the
	 * observers.
	 */
	public void disconnect() {
		if (socket != null
				&& (state == ClientState.CONNECTED || state == ClientState.AUTHENTICATED)) {
			try {
				socket.close();
			} catch (Exception e) {
				// ignore this
			} finally {
				state = ClientState.NOT_CONNECTED;
				setChanged();
				notifyObservers();
			}
		}
	}

	/**
	 * Get current {@link ClientState} of the client.
	 * 
	 * @return the client state
	 */
	public ClientState getClientState() {
		return state;
	}

	/**
	 * Get list of received {@link CalendarEntry} after calling {@link #connect}
	 * .
	 * 
	 * @return the list of entries
	 */
	public List<CalendarEntry> getList() {
		return list;
	}

	/**
	 * Get the port of the socket the client is connected with.
	 * 
	 * @return the port of the socket
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Get the hostname of the of the server the socket is connected to.
	 * 
	 * @return the hostname of the server
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Get the username that the client authenticated with the server.
	 * 
	 * @return the username
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * States the {@link Client} can be in.
	 * 
	 * @author Michael Albertz
	 *
	 */
	public enum ClientState {
		/**
		 * If the client is connected but not authenticated.
		 */
		CONNECTED,
		/**
		 * If the client is not connected.
		 */
		NOT_CONNECTED,
		/**
		 * If the client is connected and authenticated.
		 */
		AUTHENTICATED
	}

	/**
	 * This class implements {@link Runnalbe}.
	 * <p>
	 * If it is started it will keep sending {@link ServerCommand.Command#HEARTBEAT}
	 * to the server, as long as the clients state is not
	 * {@link ClientState#NOT_CONNECTED} to the server.
	 * 
	 * @author Michael Albertz
	 *
	 */
	private class HeartBeat implements Runnable {

		private final static int HEARTBEAT_INTERVAL = 15000;

		@Override
		public void run() {
			Object obj;
			try {
				while (state != ClientState.NOT_CONNECTED
						&& (obj = request(new ServerCommand(Command.HEARTBEAT,
								null, null))) != null) {
					log.debug("Heartbeat received: " + obj);
					try {
						Thread.sleep(HEARTBEAT_INTERVAL);
					} catch (InterruptedException e) {
						log.fatal("Heartbeat thread was interrupted");
					}
				}
			} catch (SocketException e) {
				log.error("Connection lost ...");
				log.error("Closing client ...");
				Client.this.disconnect();
			} catch (ClassNotFoundException | IllegalArgumentException
					| IOException e) {
				log.fatal(
						"An unexpected Exception got caught. Closing client ...",
						e);
				Client.this.disconnect();
			}

		}

	}

}
