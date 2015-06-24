package de.malbertz.calendar.client.core;

import java.util.Date;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class CalendarEntry {

	//TODO: max und min year in konstante
	
	
	
	/** Regex {@link java.util.regex.Pattern} to match a Date. */
	public static final String DATE_MATCHER_PATTERN = "^(\\d{2}).(\\d{2}).(\\d{4})$";
	/** Pattern to be used by {@link java.lang.String#format(String, Object...)} to format a Date. */
	public static final String DATE_FORMATTING_PATTERN = "%02d.%02d.%04d";
	/** Regex {@link java.util.regex.Pattern} to match a Time. */
	public static final String TIME_MATCHER_PATTERN = "^(\\d{2}):(\\d{2})$";
	/** Pattern to be used by {@link java.lang.String#format(String, Object...)} to format a Time. */
	public static final String TIME_FORMATTING_PATTERN = "%02d:%02d";
	public static final int MIN_YEAR = 1100;
	public static final int MAX_YEAR = 5000;
		
	private int[] daysPerMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
			31 };

	private final SimpleIntegerProperty day;
	private final SimpleIntegerProperty month;
	private final SimpleIntegerProperty year;
	private final SimpleIntegerProperty hour;
	private final SimpleIntegerProperty minute;
	private final SimpleStringProperty name;
	private final SimpleStringProperty description;
	
	public CalendarEntry() {
		this(1, 1, 1100, 0, 0, "", "");
	}

	public CalendarEntry(int day, int month, int year) {
		this(day, month, year, 0, 0, "", "");
	}
	
	public CalendarEntry(int day, int month, int year, int hour, int minute) {
		this(day, month, year, hour, minute, "", "");
	}

	public CalendarEntry(int day, int month, int year, int hour, int minute,
			String name, String description) throws IllegalArgumentException {
		
		// validate and initialize all properties
		if ((year < CalendarEntry.MIN_YEAR) || (year > CalendarEntry.MAX_YEAR)) {
			// TODO gregorianischer kalender!
			throw new IllegalArgumentException();
		}
		this.year = new SimpleIntegerProperty(year);
		daysPerMonth[1] = isLeapYear(getYear()) ? 29 : 28;
		if ((month < 1) || (month > 12)) {
			throw new IllegalArgumentException();
		}
		this.month = new SimpleIntegerProperty(month);
		if ((day < 1) || (day > daysPerMonth[getMonth() - 1])) {
			throw new IllegalArgumentException();
		}
		this.day = new SimpleIntegerProperty(day);

		if ((hour < 1) || (hour > 24)) {
			throw new IllegalArgumentException();
		}
		this.hour = new SimpleIntegerProperty(hour);
		
		if ((minute < 1) || (minute > 60)) {
			throw new IllegalArgumentException();
		}
		this.minute = new SimpleIntegerProperty(minute);

		this.name = new SimpleStringProperty(name);
		this.description = new SimpleStringProperty(description);
		
		// since bindings dont call setters on changing we have to do the
		// validation in changelisteners
		yearProperty().addListener((InvalidationListener) observable -> {
			System.out.println(observable);
			
		});
		
	}

	/**
	 * Method to determine if given year is a leap year or not.
	 * 
	 * @param year
	 *            The year to check for a leap year.
	 * @return isLeapYear
	 */
	private boolean isLeapYear(int year) {
		// It is a leap year if the following conditions are met:
		// 1. Year is divisible by 4
		// 2. Year is not divisible by 100
		// 3. Exeption: Year is divisible by 400
		return (((year % 400) == 0) || (((year % 4) == 0) && ((year % 100) != 0)));
	}

	public final SimpleIntegerProperty dayProperty() {
		return this.day;
	}

	public final int getDay() {
		return this.dayProperty().get();
	}

	public final void setDay(final int day) throws IllegalArgumentException {
		this.dayProperty().set(day);
	}

	public final SimpleIntegerProperty monthProperty() {
		return this.month;
	}

	public final int getMonth() {
		return this.monthProperty().get();
	}

	public final void setMonth(final int month) {
		this.monthProperty().set(month);
	}

	public final SimpleIntegerProperty yearProperty() {
		return this.year;
	}

	public final int getYear() {
		return this.yearProperty().get();
	}

	public final void setYear(final int year) {
		this.yearProperty().set(year);
	}
	
	public final SimpleIntegerProperty hourProperty() {
		return this.hour;
	}

	public final int getHour() {
		return this.hourProperty().get();
	}

	public final void setHour(final int hour) {
		this.hourProperty().set(hour);
	}

	public final SimpleIntegerProperty minuteProperty() {
		return this.minute;
	}

	public final int getMinute() {
		return this.minuteProperty().get();
	}

	public final void setMinute(final int minute) {
		this.minuteProperty().set(minute);
	}

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


}
