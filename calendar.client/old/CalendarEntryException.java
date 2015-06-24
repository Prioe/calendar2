package de.malbertz.calendar.client.core;

@Deprecated
public class CalendarEntryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String msg;
	private int val;
	
	public CalendarEntryException(String msg, int val) {
		super("Invalid " + msg + " (" + val + ")");
		this.msg = msg;
	}
	
	public String getInvalidArg() {
		return msg;
	}

	public int getVal() {
		return val;
	}
	
}