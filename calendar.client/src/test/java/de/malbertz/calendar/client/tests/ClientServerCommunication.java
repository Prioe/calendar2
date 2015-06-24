package de.malbertz.calendar.client.tests;

import static org.junit.Assert.fail;

import java.net.ConnectException;

import org.junit.Test;

import de.malbertz.calendar.client.core.Client;
import de.malbertz.calendar2.CalendarEntry;

public class ClientServerCommunication {

	@Test
	public void test() {
		CalendarEntry entry = new CalendarEntry();
		entry.setDescription("TestDescription");
		try {
			Client c = new Client();
			c.connect("localhost", 27999, "Michael", "passwort");
			Thread.sleep(10000);	
			c.disconnect();
		} catch (ConnectException ce) {
			fail("Server is is propably not running.");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
			
		}
	
	}

}
