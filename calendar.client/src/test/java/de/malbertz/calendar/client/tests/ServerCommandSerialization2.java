package de.malbertz.calendar.client.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import de.malbertz.calendar2.CalendarEntry;
import de.malbertz.calendar2.ServerCommand;
import de.malbertz.calendar2.ServerCommand.Command;
import de.malbertz.calendar2.util.SerializationUtils;

public class ServerCommandSerialization2 {

   @Test
   public void test() {
      CalendarEntry entry = new CalendarEntry();
      entry.setName("TestName");
      entry.setDescription("TestDescription");
      byte[] ser = null;
      ServerCommand copy = null;
      ServerCommand command = null;

      command = new ServerCommand(Command.MODIFY, new CalendarEntry(), entry);
      try {
         ser = SerializationUtils.pickle(command);
         copy = SerializationUtils.unpickle(ser, ServerCommand.class);
      } catch (IOException e) {
         e.printStackTrace();
         fail("I/O exception happend for MODIFY");
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
         fail("ClassNotFoundException happend for MODIFY");
      }
      assertEquals(command, copy);

      command = new ServerCommand(Command.ADD, entry, null);
      try {
         ser = SerializationUtils.pickle(command);
         copy = SerializationUtils.unpickle(ser, ServerCommand.class);
      } catch (IOException e) {
         e.printStackTrace();
         fail("I/O exception happend for ADD");
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
         fail("ClassNotFoundException happend for ADD");
      }
      assertEquals(command, copy);

      command = new ServerCommand(Command.REMOVE, null, entry);
      try {
         ser = SerializationUtils.pickle(command);
         copy = SerializationUtils.unpickle(ser, ServerCommand.class);
      } catch (IOException e) {
         e.printStackTrace();
         fail("I/O exception happend for ADD");
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
         fail("ClassNotFoundException happend for ADD");
      }
      assertEquals(command, copy);

   }

}
