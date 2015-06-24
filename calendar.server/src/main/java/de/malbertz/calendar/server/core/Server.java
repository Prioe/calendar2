package de.malbertz.calendar.server.core;

import java.io.File;
import java.io.FilenameFilter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.server.authentication.Authenticator;
import de.malbertz.calendar2.ServerCommand;

/**
 * This class implements a Server.
 * <p>
 * It is used to store CalendarEntry for authenticated clients.
 * <p>
 * The Server will start a Thread that keeps listening for incoming connections.
 * If a new client connected a new {@link ClientThread} will be created and
 * started.
 * <p>
 * It implements the {@link Observer} interface. If a {@link ClientThread} ends
 * it will notify the server and the client will be removed.
 * 
 * @author Michael Albertz
 *
 */
public class Server implements Observer {

	private static Logger log = LogManager.getLogger(Server.class);

	/** Holds all active ClientThreads. */
	private Vector<ClientThread> clients;
	/** Holds received commands. */
	private BlockingQueue<SimpleEntry<ClientThread, ServerCommand>> commandQueue;
	/** Holds the ServerSocket. */
	private ServerSocket serverSocket;
	/** Holds the ServerThread. */
	private ServerThread serverThread;
	/** Determines if the ServerThread is running or not. */
	private boolean listening;
	/** Holds the port the ServerSocket is listening to. */
	private int port;

	/**
	 * Creates a new server at default port 27999. No server thread is running
	 * and its not listening for new connections.
	 */
	public Server() {
		this.clients = new Vector<ClientThread>();
		this.commandQueue = new LinkedBlockingQueue<SimpleEntry<ClientThread, ServerCommand>>();
		this.port = 27999;
		this.listening = false;
	}

	/**
	 * Create a new user with specified name and password.
	 * 
	 * @param name
	 *            the name of the new user
	 * @param password
	 *            the password of the new user
	 * @return was the operation successful
	 */
	public boolean createUser(String name, String password) {
		return Authenticator.add(name, password);
	}

	/**
	 * Remove an existing user by a specified name.
	 * 
	 * @param name
	 *            the name of the user to remove
	 * @return was the operation successful
	 */
	public boolean removeUser(String name) {
		return Authenticator.remove(name);
	}

	/**
	 * Kicks a connected user by a specified name from the server.
	 * 
	 * @param name
	 *            the name of the user to kick
	 * @return was the opperation successful
	 */
	public boolean kickUser(String name) {
		for (ClientThread client : clients) {
			if (client.getName() != null && (client.getName().equals(name))) {
				client.stopClient();
				log.info("Kicked " + client);
				return true;
			}
		}
		log.debug("Failed to kick " + name);
		return false;
	}

	/**
	 * This method removes data files of users that do not exist in the
	 * user-password table anymore.
	 */
	private void cleanData() {
		File root = new File(".");
		File[] data = root.listFiles((FilenameFilter) (dir, name) -> name
				.toLowerCase().endsWith(".dat"));

		for (File file : data) {
			String name = file.getName().replaceFirst("[.][^.]+$", "");
			if (!Authenticator.exists(name)) {
				if (file.delete()) {
					log.info("Deleted data file for non existent user '" + name
							+ "'");
				}
			}

		}

	}

	/**
	 * This method starts the server thread and it cleans the data files.
	 */
	public void startServer() {
		if (!listening) {
			serverThread = new ServerThread();
			serverThread.start();
			cleanData();
			listening = true;
		}
	}

	/**
	 * This method removes all clients from the server and stops the server
	 * thread.
	 */
	public void stopServer() {
		if (listening) {
			serverThread.stopServerThread();
			Enumeration<ClientThread> e = clients.elements();
			while (e.hasMoreElements()) {
				ClientThread ct = (ClientThread) e.nextElement();
				ct.stopClient();
			}
			listening = false;
		}
	}

	/**
	 * This method will be called if Observables notify the Observer.
	 * <p>
	 * This happens when a client thread ends. The server will then remove the
	 * client from the client list.
	 */
	@Override
	public void update(Observable o, Object arg) {
		clients.removeElement(o);
		if (o instanceof ClientThread) {
			log.info("Client(" + ((ClientThread) o).getIdentification()
					+ ") disconnected");
		} else {
			log.info("Client(" + o + ") disconnected");
		}
	}

	public Vector<ClientThread> getClients() {
		return clients;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isRunning() {
		return this.serverThread.listen;
	}

	/**
	 * This inner class will keep listening to incoming connections and initiate
	 * a ClientThread object for each connection.
	 * 
	 * @author Michi
	 *
	 */
	private class ServerThread extends Thread {
		/** Determines if the ServerThread is listening or not */
		private boolean listen;
		/** Holds the command consumer */
		private CommandConsumer commandConsumer;

		/**
		 * Initiates the server thread.
		 */
		public ServerThread() {
			this.listen = false;
		}

		/**
		 * This will be called if the server thread is started.
		 * <p>
		 * It will first initiate and start a {@link CommandConsumer}. Then it
		 * will keep accepting new {@link Socket}s and create a new
		 * {@link ClientThread} for each socket.
		 */
		@Override
		public void run() {
			listen = true;
			commandConsumer = new CommandConsumer(commandQueue);
			new Thread(commandConsumer).start();
			try {
				log.info("Starting ServerSocket on port: " + Server.this.port
						+ " ...");
				Server.this.serverSocket = new ServerSocket(Server.this.port);
				log.info("Successfully started ServerSocket ("
						+ Server.this.serverSocket + ")");
				while (this.listen) {
					log.debug("Start listening for connections ...");
					Socket socket = Server.this.serverSocket.accept();
					log.info("New incoming connection");
					try {
						log.debug("Creating new ClientThread ...");
						ClientThread client = new ClientThread(socket,
								commandQueue);
						log.debug("Successfully created ClientThread: "
								+ client);
						client.addObserver(Server.this);
						Server.this.clients.addElement(client);
						log.debug("Creating thread for: " + client + " ...");
						Thread t = new Thread(client);
						log.debug("Successfully created thread for: " + client);
						log.debug("Starting thread for: " + client + " ...");
						t.start();
						log.debug("Successfully started thread for: " + client);
						log.info("New Client connected: "
								+ client.getIdentification());
					} catch (Exception e) {
						log.error(
								"An error occured while creating a new ClientThread for socket: "
										+ socket, e);
					}
				}
			} catch (SocketException se) {
				if (!"socket closed".equals(se.getMessage())) {
					log.fatal(
							"An fatal error occured. Stopping server thread ...",
							se);
					this.stopServerThread();
				}
			} catch (Exception e) {
				log.fatal("An fatal error occured. Stopping server thread ...",
						e);
				this.stopServerThread();
			}
		}

		/**
		 * This method stops the server thread and destroy the command consumer.
		 */
		public void stopServerThread() {
			try {
				Server.this.serverSocket.close();
			} catch (Exception e) {
				log.info("Failed to stop the server clean");
			} finally {
				this.listen = false;
				commandConsumer.destroy();
				log.info("Stopped server thread");
			}

		}

	}

}
