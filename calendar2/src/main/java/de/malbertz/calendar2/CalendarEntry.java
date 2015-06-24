package de.malbertz.calendar2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * This Class implements the data object used to store entries on the server and
 * the client.
 * <p>
 * Date, start time, end time, name and description of the entry are stored as
 * JavaFX properties.
 * 
 * @author Michael Albertz
 *
 */
public class CalendarEntry implements Serializable {

	/** The serial version UID used for Serialization */
	private static final long serialVersionUID = 5965631935186460395L;

	/* The fields that store the data */
	private SimpleObjectProperty<LocalDate> date;
	private SimpleObjectProperty<LocalTime> startTime;
	private SimpleObjectProperty<LocalTime> endTime;
	private SimpleStringProperty name;
	private SimpleStringProperty description;

	/**
	 * This constructor creates a new default {@link CalendarEntry} object.
	 * <p>
	 * The default properties are:
	 * <p>
	 * <table>
	 * <tr>
	 * <th>Date</th>
	 * <th>Start</th>
	 * <th>End</th>
	 * <th>Name</th>
	 * <th>Description</th>
	 * </tr>
	 * <tr>
	 * <td>{@link LocalDate#now()}</td>
	 * <td>{@link LocalDate#MIN}</td>
	 * <td>{@link LocalDate#MAX}</td>
	 * <td>""</td>
	 * <td>""</td>
	 * </tr>
	 * </table>
	 */
	public CalendarEntry() {
		this(LocalDate.now(), LocalTime.MIN, LocalTime.MAX, "", "");
	}

	/**
	 * This constructor creates a new {@link CalendarEntry} object with
	 * specified parameters.
	 * 
	 * @param date
	 *            the date of the entry
	 * @param startTime
	 *            the start time of the entry
	 * @param endTime
	 *            the end time of the entry
	 * @param name
	 *            the name of the entry
	 * @param description
	 *            the description of the entry
	 */
	public CalendarEntry(LocalDate date, LocalTime startTime,
			LocalTime endTime, String name, String description) {

		this.date = new SimpleObjectProperty<LocalDate>(date);
		this.startTime = new SimpleObjectProperty<LocalTime>(startTime);
		this.endTime = new SimpleObjectProperty<LocalTime>(endTime);
		this.name = new SimpleStringProperty(name);
		this.description = new SimpleStringProperty(description);
	}

	/**
	 * The string representation of a CalendarEntry is
	 * "[date=DATE, start=START, end=END, name=NAME, description=DESCRIPTION]".
	 * 
	 * @return the string representation
	 */
	@Override
	public String toString() {
		return "[date=" + getDate() + ", start=" + getStartTime() + ", end="
				+ getEndTime() + ", name=" + getName() + ", description="
				+ getDescription() + "]";
	}

	/**
	 * This method overrides {@link Object#equals(Object)} to compare this
	 * instant to other objects.
	 * 
	 * @param obj
	 *            the object to compare to
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
		if (!(obj instanceof CalendarEntry)) {
			return false;
		}
		CalendarEntry entry = (CalendarEntry) obj;
		if (!this.getDate().equals(entry.getDate())) {
			return false;
		} else if (!this.getStartTime().equals(entry.getStartTime())) {
			return false;
		} else if (!this.getEndTime().equals(entry.getEndTime())) {
			return false;
		} else if (!this.getName().equals(entry.getName())) {
			return false;
		} else if (!this.getDescription().equals(entry.getDescription())) {
			return false;
		}
		return true;
	}

	/**
	 * This method writes this instance to a ObjectOutputStream. Used for
	 * Serialization.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(getDate());
		out.writeObject(getStartTime());
		out.writeObject(getEndTime());
		out.writeUTF(getName());
		out.writeUTF(getDescription());
	}

	/**
	 * This method reads an instance of {@link CalendarEntry} from an
	 * ObjectInputStream. Used for Serialization.
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		this.date = new SimpleObjectProperty<LocalDate>(
				(LocalDate) in.readObject());
		this.startTime = new SimpleObjectProperty<LocalTime>(
				(LocalTime) in.readObject());
		this.endTime = new SimpleObjectProperty<LocalTime>(
				(LocalTime) in.readObject());
		this.name = new SimpleStringProperty((String) in.readUTF());
		this.description = new SimpleStringProperty((String) in.readUTF());
	}


	/*
	 * Getters and Setters are quite self explanatory.
	 */

	public final SimpleStringProperty nameProperty() {
		return this.name;
	}

	public final java.lang.String getName() {
		return this.nameProperty().get();
	}

	public final void setName(final java.lang.String name) {
		this.nameProperty().set(name);
	}

	public final SimpleStringProperty descriptionProperty() {
		return this.description;
	}

	public final java.lang.String getDescription() {
		return this.descriptionProperty().get();
	}

	public final void setDescription(final java.lang.String description) {
		this.descriptionProperty().set(description);
	}

	public final SimpleObjectProperty<LocalDate> dateProperty() {
		return this.date;
	}

	public final java.time.LocalDate getDate() {
		return this.dateProperty().get();
	}

	public final void setDate(final java.time.LocalDate date) {
		this.dateProperty().set(date);
	}

	public final SimpleObjectProperty<LocalTime> startTimeProperty() {
		return this.startTime;
	}

	public final java.time.LocalTime getStartTime() {
		return this.startTimeProperty().get();
	}

	public final void setStartTime(final java.time.LocalTime startTime) {
		this.startTimeProperty().set(startTime);
	}

	public final SimpleObjectProperty<LocalTime> endTimeProperty() {
		return this.endTime;
	}

	public final java.time.LocalTime getEndTime() {
		return this.endTimeProperty().get();
	}

	public final void setEndTime(final java.time.LocalTime endTime) {
		this.endTimeProperty().set(endTime);
	}

}
