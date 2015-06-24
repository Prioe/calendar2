package de.malbertz.calendar.client.ui.elements;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.client.ui.dialogs.CreateDialog;
import de.malbertz.calendar.client.ui.elements.nodes.CalendarContextMenu;
import de.malbertz.calendar2.CalendarEntry;

public class WeekView extends GridPane implements Initializable,
		ContentPane<CalendarEntry> {

	private static final Logger log = LogManager.getLogger(WeekView.class);

	@FXML
	private TableView<CalendarEntry> entryTable;
	@FXML
	private Button previousButton;
	@FXML
	private Button nextButton;
	@FXML
	private Button todayButton;
	@FXML
	private TableColumn<CalendarEntry, LocalDate> dayColumn;
	@FXML
	private TableColumn<CalendarEntry, LocalTime> startColumn;
	@FXML
	private TableColumn<CalendarEntry, LocalTime> endColumn;
	@FXML
	private TableColumn<CalendarEntry, String> nameColumn;
	@FXML
	private TableColumn<CalendarEntry, String> descColumn;
	@FXML
	private Label currentLabel;

	private final SimpleListProperty<CalendarEntry> entryList = new SimpleListProperty<CalendarEntry>();
	private FilteredList<CalendarEntry> filteredList;
	private CalendarEntry initialEntry;
	private LocalDate firstDay;
	private LocalDate lastDay;
	private ResourceBundle bundle;

	public WeekView(ResourceBundle bundle) throws Exception {
		this(bundle, null);

	}

	public WeekView(ResourceBundle bundle, CalendarEntry startEntry)
			throws Exception {
		this.bundle = bundle;
		initialEntry = startEntry;

		LocalDate now = startEntry != null ? startEntry.getDate() : LocalDate.now();
		TemporalField tf = WeekFields.of(bundle.getLocale()).dayOfWeek();
		firstDay = now.with(tf, 1);
		lastDay = now.with(tf, 7);

		FXMLLoader loader = new FXMLLoader(getClass().getResource(
				"/fxml/WeekView.fxml"));
		loader.setResources(bundle);
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		initTableView();
		initButtonIcons();
		initButtons();
		updateCurrentLabel();
		updateList();

		entryTable.getSelectionModel().select(initialEntry);
	}

	private void initTableView() {
		entryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		dayColumn
				.setCellValueFactory(new PropertyValueFactory<CalendarEntry, LocalDate>(
						"date"));
		dayColumn
				.setCellFactory(param -> new TableCell<CalendarEntry, LocalDate>() {
					@Override
					protected void updateItem(LocalDate item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							DateTimeFormatter formatter = DateTimeFormatter
									.ofPattern("EEE, d MMM yyyy",
											bundle.getLocale());
							setText(item.format(formatter));
						} else {
							setText(null);
						}

					}
				});

		startColumn
				.setCellValueFactory(new PropertyValueFactory<CalendarEntry, LocalTime>(
						"startTime"));
		startColumn
				.setCellFactory(param -> new TableCell<CalendarEntry, LocalTime>() {
					@Override
					protected void updateItem(LocalTime item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							DateTimeFormatter formatter = DateTimeFormatter
									.ofPattern("HH:mm");
							setText(item.format(formatter));
						} else {
							setText(null);
						}

					}
				});
		startColumn.setSortable(false);
		endColumn
				.setCellValueFactory(new PropertyValueFactory<CalendarEntry, LocalTime>(
						"endTime"));
		endColumn
				.setCellFactory(param -> new TableCell<CalendarEntry, LocalTime>() {
					@Override
					protected void updateItem(LocalTime item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							DateTimeFormatter formatter = DateTimeFormatter
									.ofPattern("HH:mm");
							setText(item.format(formatter));
						} else {
							setText(null);
						}

					}
				});
		endColumn.setSortable(false);

		nameColumn
				.setCellValueFactory(new PropertyValueFactory<CalendarEntry, String>(
						"name"));
		descColumn
				.setCellValueFactory(new PropertyValueFactory<CalendarEntry, String>(
						"description"));
		
		// we have to override since filteredlist doesnt support add and we have to add it directly to
		// the observablelist wrapped by our listproperty.
		entryTable.setContextMenu(new CalendarContextMenu(bundle, entryTable) {
			
			@Override
			protected void remove(Collection<CalendarEntry> c) {
				entryList.getValue().removeAll(c);
				entryTable.getSelectionModel().selectFirst();
			}
			
			@Override
			protected void create(CalendarEntry entry) {
				CreateDialog dialog = new CreateDialog(bundle, entry);
				if (dialog.getOwner() == null) {
					dialog.initOwner(entryTable.getScene().getWindow());
				}
				CalendarEntry newEntry = dialog.waitForEntry();
				log.entry();
				if (newEntry != null) {
					if (entry != null) {
						entryList.getValue().set(entryTable.getSelectionModel()
								.getSelectedIndex(), newEntry);
					} else {
						entryList.getValue().add(newEntry);
					}
				}
			}
		});
		

	}

	private void initButtons() {
		nextButton.setOnAction(event -> {
			firstDay = firstDay.plusWeeks(1);
			lastDay = lastDay.plusWeeks(1);
			updateCurrentLabel();
			updateList();
		});
		previousButton.setOnAction(event -> {
			firstDay = firstDay.minusWeeks(1);
			lastDay = lastDay.minusWeeks(1);
			updateCurrentLabel();
			updateList();
		});
		todayButton.setOnAction(event -> {
			LocalDate now = LocalDate.now();
			TemporalField tf = WeekFields.of(bundle.getLocale()).dayOfWeek();
			firstDay = now.with(tf, 1);
			lastDay = now.with(tf, 7);
			updateCurrentLabel();
			updateList();
		});
	}

	private void updateList() {
		filteredList = entryList.filtered(entry -> {

			if (entry.getDate().isAfter(firstDay.minusDays(1))
					&& entry.getDate().isBefore(lastDay.plusDays(1))) {
				return true;
			}
			return false;
		});
		entryTable.setItems(filteredList);
	}

	private void updateCurrentLabel() {
		DateTimeFormatter firstFormat = DateTimeFormatter.ofPattern("d."
				+ (firstDay.getMonth() == lastDay.getMonth() ? "" : " MMM"),
				bundle.getLocale());
		DateTimeFormatter lastFormat = DateTimeFormatter.ofPattern(
				"d. MMM yyyy", bundle.getLocale());
		currentLabel.setText(firstDay.format(firstFormat) + " - "
				+ lastDay.format(lastFormat));
	}

	private void initButtonIcons() {
		SVGPath svg = new SVGPath();
		svg.setContent("M 0 0 v -8 l 4 4 z");
		svg.setFill(Color.DIMGREY);
		nextButton.setGraphic(svg);
		svg = new SVGPath();
		svg.setContent("M 0 0 v 8 l -4 -4 z");
		svg.setFill(Color.DIMGREY);
		previousButton.setGraphic(svg);
	}

	public final SimpleListProperty<CalendarEntry> entryListProperty() {
		return this.entryList;
	}

	@Override
	public CalendarEntry getSelectedItem() {
		return entryTable.getSelectionModel().getSelectedItem();
	}

}
