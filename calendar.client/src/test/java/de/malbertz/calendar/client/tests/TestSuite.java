package de.malbertz.calendar.client.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ClientServerCommunication.class,
      ServerCommandSerialization2.class })
public class TestSuite {
}
