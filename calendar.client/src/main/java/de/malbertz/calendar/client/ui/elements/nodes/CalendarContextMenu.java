package de.malbertz.calendar.client.ui.elements.nodes;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.client.core.Context;
import de.malbertz.calendar.client.ui.dialogs.CreateDialog;
import de.malbertz.calendar2.CalendarEntry;
import de.malbertz.calendar2.ServerCommand;
import de.malbertz.calendar2.ServerCommand.Command;

public class CalendarContextMenu extends ContextMenu {

	private static Logger log = LogManager.getLogger(CalendarContextMenu.class);

	private MenuItem editItem;
	private MenuItem deleteItem;
	private MenuItem newItem;

	private ResourceBundle bundle;
	private TableView<CalendarEntry> table;

	public CalendarContextMenu(ResourceBundle bundle,
			TableView<CalendarEntry> table) {
		super();
		this.table = table;
		this.bundle = bundle;

		editItem = new MenuItem(bundle.getString("edit"));
		deleteItem = new MenuItem(bundle.getString("delete"));
		newItem = new MenuItem(bundle.getString("new"));
		getItems().addAll(editItem, deleteItem, newItem);

		editItem.setOnAction(event -> create(table.getSelectionModel()
				.getSelectedItem()));
		deleteItem.setOnAction(event -> {
			List<CalendarEntry> items = table.getSelectionModel()
					.getSelectedItems();
			
			for (CalendarEntry calendarEntry : items) {
				Context.getInstance()
						.getClient()
						.sendCommand(
								new ServerCommand(Command.REMOVE, null,
										calendarEntry));
			}
			remove(items);

		});
		newItem.setOnAction(event -> create(null));

		deleteItem.disableProperty().bind(
				Bindings.isEmpty(table.getSelectionModel().getSelectedItems()));
		editItem.setAccelerator(new KeyCodeCombination(KeyCode.E,
				KeyCombination.CONTROL_DOWN));
		deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
		newItem.setAccelerator(new KeyCodeCombination(KeyCode.N,
				KeyCombination.CONTROL_DOWN));

		//@formatter:off
		editItem.disableProperty().bind(
				Bindings.greaterThan(						
						Bindings.size(table.getSelectionModel().getSelectedItems()),
						1)
							.or(
									Bindings.isEmpty(table.getSelectionModel().getSelectedItems())
									)
								);
		//@formatter:on
	}

	protected void remove(Collection<CalendarEntry> c) {
		table.getItems().removeAll(c);
		table.getSelectionModel().clearSelection();
		table.getSelectionModel().selectFirst();
	}

	protected void create(CalendarEntry entry) {
		CreateDialog dialog = new CreateDialog(bundle, entry);
		if (dialog.getOwner() == null) {
			dialog.initOwner(table.getScene().getWindow());
		}
		CalendarEntry newEntry = dialog.waitForEntry();
		log.debug(newEntry);
		if (newEntry != null) {
			if (entry != null) {
				table.getItems().set(
						table.getSelectionModel().getSelectedIndex(), newEntry);
			} else {
				table.getItems().add(newEntry);
			}
		}
	}
}
