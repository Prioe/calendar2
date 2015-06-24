package de.malbertz.calendar.client.ui.elements;

/**
 * This interface is implemented by JavaFX controllers that control the contents
 * of {@link de.malbertz.calendar.client.ui.MainScene}
 * <p>
 * It is used to receive the currently selected item in a content pane.
 * 
 * @author Michael Albertz
 *
 * @param <T>
 *           the type of the item to receive
 */
public interface ContentPane<T> {
   /**
    * Get the selected item.
    * 
    * @return the selected item
    */
   T getSelectedItem();
}
