package de.malbertz.calendar2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class implements a ServerCommand that is mainly used for communication
 * between the client and the server.
 * 
 * @author Michael Albertz
 *
 */
public class ServerCommand implements Serializable {

   private static final long serialVersionUID = 3363555647729913524L;

   private Command command;
   private CalendarEntry oldValue;
   private CalendarEntry newValue;

   /**
    * Creates a new ServerCommand.
    * <p>
    * 
    * <pre>
    * {@code
    * new ServerCommand(Command.ADD, new CalendarEntry(), null);
    * new ServerCommand(Command.REMOVE, null, new CalendarEntry());
    * new ServerCommand(Command.MODIFY, new CalendarEntry(), new CalendarEntry());
    * }
    * </pre>
    * 
    * @param command
    *           the type of <i>Command</i>
    * @param newValue
    *           the <i>newValue</i> used by the command
    * @param oldValue
    *           the <i>oldValue</i> used by the command
    * @throws IllegalArgumentException
    *            if the null / not null state of the values don't match the
    *            required ones
    */
   public ServerCommand(Command command, CalendarEntry newValue,
         CalendarEntry oldValue) throws IllegalArgumentException {
      this.command = command;
      this.oldValue = oldValue;
      this.newValue = newValue;
      validate();
   }

   /**
    * This method checks if the arguments given to the contructor are valid.
    * 
    * @throws IllegalArgumentException
    *            if the command is invalid
    */
   private void validate() throws IllegalArgumentException {
      switch (command) {
      case ADD:
         if (!(oldValue == null && newValue != null)) {
            throw new IllegalArgumentException(
                  "ADD requires oldValue to be null and newValue not to be null");
         }
         break;
      case REMOVE:
         if (!(oldValue != null && newValue == null)) {
            throw new IllegalArgumentException(
                  "REMOVE requires oldValue not to be null and newValue to be null");
         }
         break;
      case MODIFY:
         if (!(oldValue != null && newValue != null)) {
            throw new IllegalArgumentException(
                  "MODIFY requires both oldValue and newValue not to be null");
         }
         break;
      case REQUEST_ALL:
         if (!(oldValue == null && newValue == null)) {
            throw new IllegalArgumentException(
                  "REQUEST_ALL requires both oldValue and newValue to be null");
         }
         break;
      case HEARTBEAT:
         if (!(oldValue == null && newValue == null)) {
            throw new IllegalArgumentException(
                  "REQUEST_ALL requires both oldValue and newValue to be null");
         }
         break;
      default:
      }
   }

   /**
    * This method writes this instance to a ObjectOutputStream. Used for
    * Serialization.
    */
   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(command.getInt());
      if (oldValue == null) {
         out.writeUTF("null");
      } else {
         out.writeUTF("notnull");
         out.writeObject(oldValue);
      }
      if (newValue == null) {
         out.writeUTF("null");
      } else {
         out.writeUTF("notnull");
         out.writeObject(newValue);
      }
   }

   /**
    * This method reads an instance of {@link ServerCommand} from an
    * ObjectInputStream. Used for Serialization.
    */
   private void readObject(ObjectInputStream in) throws IOException,
         ClassNotFoundException {
      this.command = Command.getCommand(in.readInt());
      Object obj = in.readUTF();
      if (obj.equals("null")) {
         this.oldValue = null;
      } else {
         this.oldValue = (CalendarEntry) in.readObject();
      }
      obj = in.readUTF();
      if (obj.equals("null")) {
         this.newValue = null;
      } else {
         this.newValue = (CalendarEntry) in.readObject();
      }

   };

   /**
    * The string representation of a ServerCommand is
    * "ServerCommand[command=COMMAND,oldValue=OLD_VALUE,newValue=NEW_VALUE]".
    */
   @Override
   public String toString() {
      return "ServerCommand[command=" + command + ",oldValue=" + oldValue
            + ",newValue=" + newValue + "]";
   }

   /**
    * This method overrides {@link Object#equals(Object)} to compare this
    * instant to other objects.
    * 
    * @param obj
    *           the object to compare to
    * @return are the object and this instance equal
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj == this) {
         return true;
      }
      if (!(obj instanceof ServerCommand)) {
         return false;
      }
      ServerCommand comp = (ServerCommand) obj;
      if (oldValue == null) {
         if (comp.getOldValue() != null) {
            return false;
         }
      }

      if (oldValue == null) {
         if (comp.getOldValue() != null) {
            return false;
         }
      } else {
         if (!oldValue.equals(comp.getOldValue())) {
            return false;
         }
      }
      if (newValue == null) {
         if (comp.getNewValue() != null) {
            return false;
         }
      } else {
         if (!newValue.equals(comp.getNewValue())) {
            return false;
         }
      }
      if (!(command == comp.getCommand())) {
         System.out.println("command");
         return false;
      }
      return true;

   }

   /*
    * Getters and Setters are quite self explanatory.
    */

   public Command getCommand() {
      return command;
   }

   public void setCommand(Command command) {
      this.command = command;
   }

   public CalendarEntry getOldValue() {
      return oldValue;
   }

   public void setOldValue(CalendarEntry oldValue) {
      this.oldValue = oldValue;
   }

   public CalendarEntry getNewValue() {
      return newValue;
   }

   public void setNewValue(CalendarEntry newValue) {
      this.newValue = newValue;
   }

   /**
    * Commands the class {@link ServerCommand} can use.
    * 
    * @author Michael Albertz
    *
    */
   public enum Command {

      NOOP(0),
      /**
       * Add new <i>newValue</i> to the authenticated users list.
       * <p>
       * requires oldValue to be null and newValue not to be null
       */
      ADD(1),
      /**
       * Remove <i>oldValue</i> from the authenticated users list.
       * <p>
       * requires oldValue not to be null and newValue to be null
       */
      REMOVE(2),
      /**
       * Replaces <i>oldValue</i> with <i>newValue</i> in the authenticated
       * users list.
       * <p>
       * requires both oldValue and newValue not to be null
       */
      MODIFY(3),
      /**
       * Requests the authenticated users list.
       * <p>
       * requires both oldValue and newValue to be null
       */
      REQUEST_ALL(4),
      /**
       * Requests a heartbeat
       * <p>
       * required both oldValue and newValue to be null
       */
      HEARTBEAT(5);

      /** integer representation of the command. Used for Serialization */
      private final int num;

      /** Assign integer values to the enum */
      private Command(int num) {
         this.num = num;
      }

      /**
       * Get the {@link Command} thats represented by the specified integer
       * value.
       * 
       * @param num
       *           the specified integer value
       * @return the command
       */
      public static Command getCommand(int num) {
         for (Command command : Command.values()) {
            if (command.getInt() == num) {
               return command;
            }
         }
         return null;
      }

      /**
       * Get the integer value of the {@link Command}.
       * 
       * @return
       */
      private int getInt() {
         return this.num;
      }
   }
}
