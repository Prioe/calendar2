package de.malbertz.calendar.server.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.server.authentication.Authenticator;
import de.malbertz.calendar2.CalendarEntry;
import de.malbertz.calendar2.ServerCommand;
import de.malbertz.calendar2.ServerCommand.Command;
import de.malbertz.calendar2.util.SerializationUtils;

/**
 * This class implements a ClientThread.
 * <p>
 * After a new ClientThread instance was created the ClientThread will not
 * process any {@link ServerCommand}s until the client is authenticated.
 * <p>
 * The status {@link ClientThreadState#AUTHENTICATED} will be reached if the
 * client sent a String array that contains name and password of a user
 * existing in the user-password-table.
 * <p>
 * After successful authentication the ClientThread will keep listening for
 * ServerCommands. All other objects received will be discarded.
 * <p>
 * If a valid ServerCommand was received it is put on the CommandQueue where the
 * command consumer will eventually execute it.
 * <p>
 * This class extends {@link Observable}. That is used to notify the Server if
 * the client loses connection or a fatal error happened.
 * 
 * @author Michael Albertz
 *
 */
public class ClientThread extends Observable implements Runnable {

	private static final Logger log = LogManager.getLogger(ClientThread.class);

	/** Holds the output stream of the socket */
	private ObjectOutputStream out;
	/** Holds the input stream of the socker */
	private ObjectInputStream in;
	/** Holds the blocking command queue the command consumer is taking from */
	private BlockingQueue<SimpleEntry<ClientThread, ServerCommand>> commandQueue;
	/** Holds the socket of the client */
	private Socket socket;
	/** Determines if the client thread is running */
	private boolean running;
	/** Holds the current ClientThreadState the ClientThread is in */
	private ClientThreadState state;
	/** Holds the username of the connected user */
	private String userName = null;
	/** Holds the entrys of the connected and authenticated user */
	private List<CalendarEntry> entryList;

	/**
	 * Creates a new ClientThread thats connected to the specified socket and
	 * puts commands in the specified command queue.
	 * 
	 * @param socket
	 *            the socket connected to a client
	 * @param commandQueue
	 *            the command queue to put commands
	 */
	public ClientThread(Socket socket,
			BlockingQueue<SimpleEntry<ClientThread, ServerCommand>> commandQueue)
			throws IOException {
		this.socket = socket;
		this.commandQueue = commandQueue;
		running = false;
		state = ClientThreadState.AUTHENTICATING;
		entryList = new ArrayList<>();
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			running = true;
		} catch (IOException ioe) {
			log.error("Couldn't create client for socket: " + socket, ioe);
			throw ioe;
		}
	}

	/**
	 * This methods closes the socket the client is connected to.
	 */
	public void stopClient() {
		try {
			socket.close();
		} catch (IOException ioe) {
			log.warn("Couldn't close socket: " + socket);
		}

	}

	/**
	 * This method will be called if the ClientThread is started.
	 * <p>
	 * It will keep waiting for new object read from the sockets inputstream and
	 * handle them.
	 */
	@Override
	public void run() {
		Object obj = null;
		try {
			while ((obj = in.readObject()) != null && running) {
				log.debug("Received from client(" + this + "): " + obj);
				handle(obj);
			}
			running = false;
		} catch (IOException e) {
			running = false;
		} catch (ClassNotFoundException e) {
			log.fatal("Class of a serialized object cannot be found.", e);
		} finally {
			stopClient();

			// notify observers to clean up
			this.setChanged();
			this.notifyObservers(this);
			try {
				saveEntryList();
			} catch (IOException e) {
				log.fatal("Couln't save entry list", e);
			}
		}
	}

	/**
	 * This method sends the specified object to the connected client.
	 * 
	 * @param obj
	 *            the object to send
	 */
	public void send(Object obj) throws IOException {
		log.debug("Sending to client: " + obj);
		if (obj instanceof CalendarEntry[]) {
			for (CalendarEntry entry : (CalendarEntry[]) obj) {
				log.debug(entry);
			}
			out.writeObject(SerializationUtils.pickle((CalendarEntry[]) obj));
		}
		out.writeObject(obj);
	}

	/**
	 * This method processes an object.
	 * <p>
	 * If the object is of type String array the client tries to authenticate.
	 * If the object is a byte array the method tries to cast it to a server
	 * command. If successful the command gets put on the command queue. If not
	 * the object gets discarded.
	 * 
	 * @param obj
	 *            the object to handle
	 */
	private void handle(Object obj) throws IOException {
		if (obj instanceof String[]) {
			if (state != ClientThreadState.AUTHENTICATING) {
				log.info("Illegal command: Received a String array while not authenticating from: "
						+ this);
				return;
			}
			String[] login = (String[]) obj;
			if (login.length == 2) {
				log.info("Checking credentials ...");
				log.trace("name=" + login[0] + ",pass=" + login[1]);
				boolean authenticated = Authenticator.authenticate(login[0],
						login[1]);
				state = authenticated ? ClientThreadState.AUTHENTICATED
						: ClientThreadState.AUTHENTICATING;
				if (authenticated) {
					this.userName = login[0];
					loadEntryList();
				}
				log.info(authenticated ? "Client authenticated"
						: "Client not authenticated as " + login[0]);
				send(authenticated);
			} else {
				log.warn("Illegal String array from client: " + this);
			}
		} else if (obj instanceof byte[]) {
			ServerCommand command = null;
			try {
				command = SerializationUtils.unpickle((byte[]) obj,
						ServerCommand.class);
				if (state != ClientThreadState.AUTHENTICATED) {
					log.warn("Illegal command: Received a ServerCommand while not authenticated from: "
							+ this);
					return;
				}
				if (command.getCommand() != Command.HEARTBEAT) {
					log.info("Server command received: " + command);
				}
				commandQueue.put(new SimpleEntry<ClientThread, ServerCommand>(
						this, command));
			} catch (ClassCastException cce) {
				log.error("the class of the specified element prevented it from being added to the command queue "
						+ command);
			} catch (NullPointerException npe) {
				log.error("the specified element is null " + command);
			} catch (IllegalArgumentException iae) {
				log.error("a property of the specified element prevented it from being added to the command queue "
						+ command);
			} catch (Exception e) {
				log.warn("Illegal byte array received: " + obj);
			}
		} else {
			log.warn("Unknown object received: " + obj + " by " + this);
		}

	}

	/**
	 * This method loads the entry list for an authenticated client from a
	 * datafile to a list accessible by {@link ClientThread#getEntryList()}.
	 */
	private void loadEntryList() throws IOException {
		File f = new File(userName + ".dat");
		f.createNewFile();
		try (ObjectInputStream in = new ObjectInputStream(
				new FileInputStream(f))) {
			Object obj = in.readObject();
			if (obj instanceof CalendarEntry[]) {
				CalendarEntry[] array = (CalendarEntry[]) obj;
				for (CalendarEntry calendarEntry : array) {
					entryList.add(calendarEntry);
				}
			} else {
				log.error("Corrupt data file for: " + getIdentification());
				f.delete();
			}
		} catch (ClassNotFoundException e) {
			log.error("Corrupt data file for: " + getIdentification());
			f.delete();
		}

	}

	/**
	 * This method saves the current entry list to a datafile with the filename
	 * "USER.dat".
	 */
	private void saveEntryList() throws IOException {
		File f = new File(userName + ".dat");
		f.createNewFile();
		try (ObjectOutputStream out = new ObjectOutputStream(
				new FileOutputStream(f, false))) {
			CalendarEntry[] array = new CalendarEntry[entryList.size()];
			array = entryList.toArray(array);
			out.writeObject(array);
		}
	}

	public List<CalendarEntry> getEntryList() {
		return entryList;
	}

	public String getName() {
		return userName;
	}

	public String getIdentification() {
		return userName != null ? userName + socket.getRemoteSocketAddress()
				: socket.getRemoteSocketAddress().toString();
	}

	@Override
	public String toString() {
		return getIdentification() + ": " + socket;

	}

	/**
	 * The State a {@link ClientThread} can be in.
	 * 
	 * @author Michael Albertz
	 *
	 */
	public enum ClientThreadState {
		/** Connected but not authenticated. */
		AUTHENTICATING,
		/** Connected and authenticated. */
		AUTHENTICATED
	}

}
