package de.malbertz.calendar.client.core;

@Deprecated
/**
 * todo: gregorianischer kalender!
 * @author Michael Albertz
 */
public class CalendarEntryImpl {

	public static final String DATE_MATCHER_PATTERN = "^(\\d{2}).(\\d{2}).(\\d{4})$";
	public static final String DATE_FORMATTING_PATTERN = "%02d.%02d.%04d";
	public static final String TIME_MATCHER_PATTERN = "^(\\d{2}):(\\d{2})$";
	public static final String TIME_FORMATTING_PATTERN = "%02d:%02d";

	private int[] daysPerMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private int mDay;
	private int mMonth;
	private int mYear;
	private String mTime;

	private String mName;
	private String mNote;

	/**
	 * Constructor for a new Calendar Entry. 
	 * Vaidates the given date and creates a new Object.
	 * @param day
	 * @param month
	 * @param year
	 * @throws IllegalArgumentException
	 */
	public CalendarEntryImpl(int day, int month, int year) throws IllegalArgumentException {

		if ((year < 1100) || (year > 5000)) {
			throw new IllegalArgumentException();
		}
		this.mYear = year;
		daysPerMonth[1] = isLeapYear(this.mYear) ? 29 : 28;
		if ((month < 1) || (month > 12)) {
			throw new IllegalArgumentException();
		} 
		this.mMonth = month;
		if ((day < 1) || (day > daysPerMonth[this.mMonth - 1])) {
			throw new IllegalArgumentException();
		}
		this.mDay = day;
		/*
		this.mYear = 		(year < 1100) || (year > 5000) ? 1100 : year;
		daysPerMonth[1] = 	isLeapYear(this.mYear) ? 29 : 28;
		this.mMonth = 		(month < 1) || (month > 12) ? 1 : month;
		this.mDay = 		(day < 1) || (day > daysPerMonth[this.mMonth - 1]) ? 1 : day;
		*/
		this.mTime = "00:00";
		this.mName = "Default";
		this.mNote = "No Note set.";
	}

	/**
	 * Method to determine if given year is a leap year or not.
	 * 	@param  year The year to check for a leap year.
	 * 	@return 	isLeapYear
	 */
	private boolean isLeapYear(int year) {		
		// It is a leap year if the following conditions are met:
		//  	1. Year is divisible by 4
		//  	2. Year is not divisible by 100
		//  	3. Exeption: Year is divisible by 400		 
		return (((year % 400) == 0) || (((year % 4) == 0) && ((year % 100) != 0)));
	}

	/**
	 * A method that returns the week number of the calendar entry.
	 * @return week number
	 */
	public int getWeekNumber(){
		/* Initialize the week day and the days between 1.1. entry year and the calendar entry */
		int weekDayIndex = getWeekDayIndex();
		int daysOfYear = 0;
		for (int i = 0; i < this.mMonth; i++) {
			daysOfYear += daysPerMonth[i];
		}

		/*
		 * DE:
		 * Die erste Kalenderwoche ist die Woche in die der erste Donnerstag des Jahres fällt.
		 * Der erste Tag des Jahres muss folglich ein Mo,Di,Mi oder Do sein und wir müssen 1,2,3 
		 * oder 4 Tage von unserer Summe abziehen.
		 */
		if (weekDayIndex == 4) {
			return daysOfYear / 7;
		} else if (weekDayIndex == 0) {
			return (daysOfYear - 3) / 7;
		} else if (weekDayIndex > 4) {
			for (int i = weekDayIndex; i > 4; i--) {
				daysOfYear--;
			}
			return daysOfYear / 7;
		} else {
			for (int i = weekDayIndex; i < 4; i++) {
				daysOfYear++;
			}
			return daysOfYear / 7;
		}
	}

	/**
	 * Method to convert a day index to a String.
	 * @param  index     0-6 Sun-Sat
	 * @param  translate Translate to german.
	 * @return           Day-String
	 */
	private String indexToWeekday(int index, boolean translate){

		
		String[] weekdaysEnglish 	= { "Sunday",	 "Monday",     "Tuesday",
										"Wednesday", "Thursday",   "Friday",   "Saturday" };
		String[] weekdaysGerman 	= { "Sonntag",	 "Montag",	   "Dienstag",
										"Mittwoch",  "Donnerstag", "Freitag",  "Samstag" };
		return (translate ? weekdaysGerman[index] : weekdaysEnglish[index]);
	}
	
	public enum Weekday {
		SUNDAY,
		MONDAY,
		TUESDAY,
		WEDNESDAY,
		THURSDAY,
		FRIDAY,
		SATURDAY
	}

	/**
	 * Method to calculate the week-day-index of Calendar Entry.
	 * @return weekday index
	 */
	private int getWeekDayIndex() {
		/* Initialize the Sum of all days between the Calendar Entry and 1.1.1100 (Monday) */
		int temp = 0;
		/* At first we loop though the years, therefore start at 1100 */
		for (int i = 1100; i < this.mYear; i++) {
			temp += isLeapYear(i) ? 366 : 365; // add 366 instead of 365 if its a leap year
		} // We're at mYear - 1
		/* Now we need to add the days of the month before the month of the entry */
		for (int i = 0; i < this.mMonth - 1; i++) {
			temp += daysPerMonth[i];
		} // We're at mMonth - 1 . mYear
		return (temp + this.mDay) % 7; // At last we add the remaining days of the entry
		/* 
		 * We're at mDay. mMonth. mYear
		 * Since we don't subtract by 1 to return an index starting at Monday, we can use
		 * the more common table starting at Sunday.
		 */
	}

	public String getWeekDay(boolean translate) {
		return this.indexToWeekday(this.getWeekDayIndex(), translate);
	}

	protected void setDay(int day) {
		// this.mDay = (day < 1) || (day > daysPerMonth[this.mMonth - 1]) ? 1 : day;
		if ((day < 1) || (day > daysPerMonth[this.mMonth - 1])) {
			throw new IllegalArgumentException();
		}
		this.mDay = day;
	}

	/**
	 * A method that returns the day of the calendar entry.
	 * @return entry day
	 */
	protected int getDay() {
		return this.mDay;
	}

	protected void setMonth(int month) {
		this.mMonth = (month < 1) || (month > 12) ? 1 : month;
	}

	/**
	 * A method that returns the month of the calendar entry. 
	 * @return entry month
	 */
	protected int getMonth() {
		return this.mMonth;
	}

	protected void setYear(int year) {
		this.mYear = (year < 1100) || (year > 5000) ? 1100 : year;
		daysPerMonth[1] = isLeapYear(this.mYear) ? 29 : 28;
		if (this.mMonth == 2 && this.mDay == 29 && !isLeapYear(this.mYear)) {
			this.mDay = 28;
			System.out.println("The entry is no leap year anymore. Changed day to 28.");
		}
	}

	/**
	 * A method that returns the month of the calendar entry.
	 * @return entry year
	 */
	protected int getYear() {
		return this.mYear;
	}

	/**
	 * Method that returns the Date of the Calendar Entry as a formatted String.
	 * @return formatted String
	 */
	public String getDateFormatted() {
		return String.format(CalendarEntryImpl.DATE_FORMATTING_PATTERN, this.mDay, this.mMonth, this.mYear);
	}

	protected String getTime() {
		return this.mTime;
	}
	
	protected void setTime(int hours, int minutes) {
		this.mTime = String.format(CalendarEntryImpl.TIME_FORMATTING_PATTERN, (hours % 24), (minutes % 60)); 
	}

	protected void setTime(String time) {
		this.mTime = time;
	}
	
	protected int getHours() {
		return Integer.parseInt(this.mTime.split(":")[0]);
	}
	
	protected void setHours(int hours) {
		this.mTime = String.format(CalendarEntryImpl.TIME_FORMATTING_PATTERN, (hours % 24), this.getMinutes()); 
	}
	
	protected int getMinutes() {
		return Integer.parseInt(this.mTime.split(":")[1]);
	}
	
	protected void setMinutes(int minutes) {
		this.mTime = String.format(CalendarEntryImpl.TIME_FORMATTING_PATTERN, this.getHours(), (minutes % 60)); 
	}

	protected void setName(String name) {
		this.mName = name;
	}

	protected String getName() {
		return this.mName;
	}

	protected void setNote(String desc){
		this.mNote = desc;
	}

	protected String getNote() {
		return this.mNote;
	}

	@Override
	public String toString() {
		return (
			"Date: " + this.getDateFormatted() + 
				" (" + this.indexToWeekday(getWeekDayIndex(), false) + ")" +
			", Time: " + this.getTime() +
			", Name: " + this.getName() +
			", Description: \'" + this.getNote() + "\'"
		);
	}

}
