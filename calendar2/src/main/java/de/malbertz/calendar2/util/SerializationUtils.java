package de.malbertz.calendar2.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class implements static methods to help with Serialization.
 * 
 * @author Michael Albertz
 *
 */
public class SerializationUtils {

	/**
	 * This method writes an object of a specified type, that extends
	 * {@link java.io.Serializable} to a byte array.
	 * 
	 * @param <T>
	 *            the specified type
	 * @param obj
	 *            the object to write
	 * @return the byte array
	 */
	public static <T extends Serializable> byte[] pickle(T obj)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.close();
		return baos.toByteArray();
	}

	/**
	 * This method reads an object of a specified type, that extends
	 * {@link java.io.Serializable} from a byte array.
	 * 
	 * @param b
	 *            the byte array to read from
	 * @param cl
	 *            the class of the object to cast to
	 * @return the object read from the byte array
	 * @throws ClassNotFoundException
	 *             if the class of a serialized object cannot be found
	 */
	public static <T extends Serializable> T unpickle(byte[] b, Class<T> cl)
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object o = ois.readObject();
		return cl.cast(o);
	}

	/** Make the constructor invisible */
	private SerializationUtils() {
	}
}
