package de.malbertz.calendar.server;

import de.malbertz.calendar.server.core.Server;
import de.malbertz.calendar.server.ui.Context;
import de.malbertz.calendar.server.ui.GraphicalUI;
import de.malbertz.calendar.server.ui.TextUI;

/**
 * Main class of the server application.
 * <p>
 * Starts the server with a text based user interface or a graphical user
 * interface.
 * 
 * @author Michael Albertz
 *
 */
public class Main {
	/**
	 * Main method of the client application.
	 * <p>
	 * Application entry point. No arguments will start the text based user
	 * interface, the argument '-b' will start the graphical user interface.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.startServer();
		if (args.length > 0 && args[0].equals("-g")) { // gui
			Context.getInstance().setServer(server);
			GraphicalUI.launch(GraphicalUI.class);
			server.stopServer();
		} else {
			new TextUI(server).start();
		}

	}
}
