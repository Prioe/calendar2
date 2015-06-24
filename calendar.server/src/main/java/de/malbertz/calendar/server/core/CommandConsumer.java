package de.malbertz.calendar.server.core;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar2.CalendarEntry;
import de.malbertz.calendar2.ServerCommand;
import de.malbertz.calendar2.ServerCommand.Command;

/**
 * This class implements a command consumer.
 * <p>
 * After it is inititialized and the thread it implements is started it will
 * keep taking commands from a blocking queue and executing them.
 * 
 * @author Michael Albertz
 *
 */
public class CommandConsumer implements Runnable {

	private static final Logger log = LogManager
			.getLogger(CommandConsumer.class);
	/**
	 * This is the so called poison-pill-item. If this item is taken off the
	 * queue, the command consumer thread will stop
	 */
	private static final SimpleEntry<ClientThread, ServerCommand> POISON_PILL = new SimpleEntry<ClientThread, ServerCommand>(
			null, null);
	/** Holds the blocking queue the command consumer is taking from */
	private final BlockingQueue<SimpleEntry<ClientThread, ServerCommand>> queue;
	/** Determines whether of not the server is running */
	private boolean running;

	/**
	 * Creates a new CommandConsumer that is taking from the specified queue.
	 * 
	 * @param commandQueue
	 *            the queue to take from
	 */
	public CommandConsumer(
			BlockingQueue<SimpleEntry<ClientThread, ServerCommand>> commandQueue) {
		queue = commandQueue;
		running = false;
	}

	/**
	 * This method gets called if the thread gets started.
	 * <p>
	 * While the CommandConsumer is running it takes commands off the queue. If
	 * there are no items to take the {@link BlockingQueue} implementations
	 * method {@link BlockingQueue#take()} will wait until there is another
	 * element.
	 */
	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				consume(queue.take());
			} catch (InterruptedException e) {
				log.fatal("CommandConsumer was interrupted!", e);
			}
		}
		log.info("CommandConsumer closed.");
	}

	/**
	 * This method consumes the specified entry by executing the command for the
	 * client.
	 * 
	 * @param entry
	 *            the entry to consume
	 */
	private void consume(SimpleEntry<ClientThread, ServerCommand> entry)
			throws InterruptedException {
		if (entry.equals(POISON_PILL)) {
			running = false;
			return;
		}
		ClientThread client = entry.getKey();
		ServerCommand command = entry.getValue();
		if (command.getCommand() != Command.HEARTBEAT) {
			log.info("Executing command: " + command + " from "
					+ client.getIdentification());
		}
		List<CalendarEntry> list = client.getEntryList();
		if (command.getCommand() != Command.HEARTBEAT) {
			log.debug("Current List: ");
			for (CalendarEntry calendarEntry : list) {
				log.debug(calendarEntry);
			}
		}

		switch (command.getCommand()) {
		case ADD:
			log.debug("Adding " + command.getNewValue());
			list.add(command.getNewValue());
			break;
		case MODIFY:
			log.debug("Replacing " + command.getOldValue() + " with "
					+ command.getNewValue());
			list.set(list.indexOf(command.getOldValue()), command.getNewValue());
			break;
		case REMOVE:
			log.debug("Removing " + command.getOldValue());
			list.remove(command.getOldValue());
			break;
		case REQUEST_ALL:
			CalendarEntry[] array = new CalendarEntry[list.size()];
			array = list.toArray(array);
			try {
				client.send(array);
			} catch (IOException e) {
				log.error("Failed to send requested data: " + array + " to "
						+ client);
			}

			break;
		case HEARTBEAT:
			try {
				client.send(command);
			} catch (IOException e) {
				log.info("Failed to send heartbeat to: " + client);
			}
			break;
		default:
			log.debug("Commandswitch triggered default for: " + command);
			break;
		}
		if (command.getCommand() != Command.HEARTBEAT) {
			log.debug("Current List: ");
			for (CalendarEntry calendarEntry : list) {
				log.debug(calendarEntry);
			}
		}
		log.debug("Executed command: " + command + " from "
				+ client.getIdentification());
	}

	/**
	 * This methods destroys the CommandConsumer by putting the poison pill item
	 * {@link #POISON_PILL} on the queue.
	 */
	public void destroy() {
		try {
			log.debug("Closing CommandConsumer");
			queue.put(POISON_PILL);
		} catch (InterruptedException e) {
		}
	}

}
