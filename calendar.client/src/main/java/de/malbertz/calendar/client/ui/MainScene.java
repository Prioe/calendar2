package de.malbertz.calendar.client.ui;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.client.core.Context;
import de.malbertz.calendar.client.ui.dialogs.CreateDialog;
import de.malbertz.calendar.client.ui.dialogs.ErrorDialog;
import de.malbertz.calendar.client.ui.elements.AgendaView;
import de.malbertz.calendar.client.ui.elements.ContentPane;
import de.malbertz.calendar.client.ui.elements.MonthView;
import de.malbertz.calendar.client.ui.elements.WeekView;
import de.malbertz.calendar2.CalendarEntry;

public class MainScene implements Initializable, Observer {

	private static final Logger log = LogManager.getLogger(MainScene.class);

	@FXML
	private Parent root;
	@FXML
	private HBox contentPane;
	@FXML
	private ToggleButton overviewToggleButton;
	@FXML
	private ToggleButton weekToggleButton;
	@FXML
	private ToggleButton monthToggleButton;
	@FXML
	private Button newButton;
	@FXML
	private Label appLabel;

	private SimpleListProperty<CalendarEntry> entryList;
	private ResourceBundle bundle;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bundle = resources;
		entryList = new SimpleListProperty<CalendarEntry>(
				FXCollections.observableArrayList());
		Context.getInstance().getClient().addObserver(this);

		initModeToggleGroup();
		newButton.setOnAction(event -> {
			CreateDialog dialog = new CreateDialog(bundle);
			if (dialog.getOwner() == null) {
				dialog.initOwner(root.getScene().getWindow());
			}
			CalendarEntry newEntry = dialog.waitForEntry();
			if (newEntry != null) {
				entryList.add(newEntry);
			}
		});
		newButton.setTooltip(new Tooltip(bundle.getString("createButtonTT")));
		entryList.addAll(Context.getInstance().getClient().getList());
		appLabel.setText(Context.getInstance().getClient().getUserName());
	}

	@SuppressWarnings("unchecked")
	private void initModeToggleGroup() {
		log.entry();

		PersistentToggleGroup group = new PersistentToggleGroup();
		overviewToggleButton.setToggleGroup(group);
		weekToggleButton.setToggleGroup(group);
		monthToggleButton.setToggleGroup(group);
		overviewToggleButton.setTooltip(new Tooltip(bundle.getString("agendaViewTT")));
		weekToggleButton.setTooltip(new Tooltip(bundle.getString("weekViewTT")));
		monthToggleButton.setTooltip(new Tooltip(bundle.getString("monthViewTT")));

		group.selectedToggleProperty()
				.addListener(
						(ChangeListener<Toggle>) (observable, oldValue,
								newValue) -> {
							ToggleButton chk = (ToggleButton) newValue
									.getToggleGroup().getSelectedToggle();
							try {
								if (chk.equals(overviewToggleButton)) {

									CalendarEntry entry;
									try {
										entry = ((ContentPane<CalendarEntry>) contentPane
												.getChildren().get(0))
												.getSelectedItem();
									} catch (IndexOutOfBoundsException e) {
										entry = null;
									}
									contentPane.getChildren().clear();
									AgendaView content = new AgendaView(bundle,
											entry);
									content.entryListProperty()
											.bindBidirectional(entryList);
									HBox.setHgrow(content, Priority.ALWAYS);
									contentPane.getChildren().add(content);
								} else if (chk.equals(weekToggleButton)) {
									CalendarEntry entry = ((ContentPane<CalendarEntry>) contentPane
											.getChildren().get(0))
											.getSelectedItem();
									contentPane.getChildren().clear();
									WeekView content = new WeekView(bundle,
											entry);
									content.entryListProperty()
											.bindBidirectional(entryList);
									HBox.setHgrow(content, Priority.ALWAYS);
									contentPane.getChildren().add(content);
								} else if (chk.equals(monthToggleButton)) {
									CalendarEntry entry = ((ContentPane<CalendarEntry>) contentPane
											.getChildren().get(0))
											.getSelectedItem();
									contentPane.getChildren().clear();
									MonthView content = new MonthView(bundle,
											entry);
									content.entryListProperty()
											.bindBidirectional(entryList);
									HBox.setHgrow(content, Priority.ALWAYS);
									contentPane.getChildren().add(content);
								}
							} catch (Exception e) {
								log.error(
										"Failed to load a resource to the content pane. "
												+ chk, e);
							}
						});
		overviewToggleButton.setSelected(true);
	}

	private class PersistentToggleGroup extends ToggleGroup {
		public PersistentToggleGroup() {
			super();
			getToggles().addListener(new ListChangeListener<Toggle>() {
				@Override
				public void onChanged(Change<? extends Toggle> c) {
					while (c.next())
						for (final Toggle addedToggle : c.getAddedSubList())
							((ToggleButton) addedToggle).addEventFilter(
									MouseEvent.MOUSE_RELEASED,
									new EventHandler<MouseEvent>() {
										@Override
										public void handle(MouseEvent mouseEvent) {
											if (addedToggle
													.equals(getSelectedToggle()))
												mouseEvent.consume();
										}
									});
				}
			});
		}
	}

	public SimpleListProperty<CalendarEntry> getEntryList() {
		return entryList;
	}

	@Override
	public void update(Observable o, Object arg) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (root.getScene().getWindow().isShowing()) {
					ErrorDialog error = new ErrorDialog(bundle,
							arg instanceof String ? (String) arg : bundle
									.getString("connectionLost"));
					if (error.getOwner() == null) {
						error.initOwner(root.getScene().getWindow());
					}
					error.showAndWait();
				}
				Platform.exit();
			}
		});

	}

}
