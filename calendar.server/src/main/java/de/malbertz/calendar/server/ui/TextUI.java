package de.malbertz.calendar.server.ui;

import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import de.malbertz.calendar.server.authentication.Authenticator;
import de.malbertz.calendar.server.core.ClientThread;
import de.malbertz.calendar.server.core.Server;

public class TextUI extends Thread {

	private Server server;

	public TextUI(Server server) {
		this.server = server;
		
	}

	@Override
	public void run() {
		try (Scanner in = new Scanner(System.in)) {
			String cmd = "";
			while (server.isRunning() && handle(cmd)) {
				cmd = in.nextLine();
			}
		} finally {
			server.stopServer();
		}
	}

	/**
	 * @param cmd
	 */
	public boolean handle(String cmd) {
		if (cmd.contains("create")) {
			String[] cmdArray = cmd.split(" ");
			if (cmdArray.length != 3) {
				printHelp();
				return true;
			}
			if (server.createUser(cmdArray[1], cmdArray[2])) {
				System.out.println("Created user '" + cmdArray[1] + ":"
						+ cmdArray[2] + "'");
			} else {
				System.out.println("User already exists");
			}
		} else if (cmd.contains("remove")) {
			String[] cmdArray = cmd.split(" ");
			if (cmdArray.length != 2) {
				printHelp();
				return true;
			}
			if (server.removeUser(cmdArray[1])) {
				System.out.println("Removed user '" + cmdArray[1]
						+ "' from database");
			} else {
				System.out.println("No user by name '" + cmdArray[1]
						+ "' found");
			}
		} else if (cmd.contains("kick")) {
			String[] cmdArray = cmd.split(" ");
			if (cmdArray.length != 2) {
				printHelp();
				return true;
			}
			if (server.kickUser(cmdArray[1])) {
				System.out.println("Kicked user '" + cmdArray[1] + "'");
			} else {
				System.out.println("No user by name '" + cmdArray[1]
						+ "' found");
			}
		} else if (cmd.contains("help")) {
			printHelp();
		} else if (cmd.contains("list")) {
			List<ClientThread> e = server.getClients();
			Authenticator.make();
			System.out.println("Connected: ");
			for (ClientThread client : e) {
				System.out.println(client.getIdentification());
			}
			System.out.println("Registered: ");
			Enumeration<Object> en = Authenticator.getAll();
			while (en.hasMoreElements()) {
				Object object = (Object) en.nextElement();
				if (object instanceof String) {
					System.out.println(object);
				}
			}
		} else if (cmd.equals("stop")) {
			return false;
		} else {
			printHelp();
		}
		return true;
	}

	private void printHelp() {
		System.out.println("Help:");
		System.out.println("\tstop - stops the server");
		System.out.println("\tcreate <username> <password> - creates new user");
		System.out.println("\tremove <username> - removes user");
		System.out.println("\tkick <username|ip> - kicks user from server");
		System.out.println("\tlist - lists all users");
		System.out.println("\thelp - prints out help");
	}

}
