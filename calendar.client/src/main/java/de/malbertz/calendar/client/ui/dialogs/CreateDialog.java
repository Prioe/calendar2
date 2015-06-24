package de.malbertz.calendar.client.ui.dialogs;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.client.core.Context;
import de.malbertz.calendar.client.ui.elements.nodes.TimeTextField;
import de.malbertz.calendar2.CalendarEntry;
import de.malbertz.calendar2.ServerCommand;
import de.malbertz.calendar2.ServerCommand.Command;

public class CreateDialog extends Stage implements Initializable {

	private final static Logger log = LogManager.getLogger(CreateDialog.class);

	@FXML
	private TimeTextField startTimeTextField;
	@FXML
	private TimeTextField endTimeTextField;
	@FXML
	private TextField nameTextField;
	@FXML
	private DatePicker datePicker;
	@FXML
	private TextArea descTextArea;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;

	private boolean canceled;
	private CalendarEntry modifiedEntry;
	private Mode mode;

	public CreateDialog(ResourceBundle bundle) {
		this(bundle, null);
	}

	public CreateDialog(ResourceBundle bundle, CalendarEntry entry) {
		super(StageStyle.UTILITY);
		initModality(Modality.WINDOW_MODAL);
		setTitle(bundle.getString("newButton"));
		setResizable(false);
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(
					"/fxml/CreateDialog.fxml"));
			loader.setResources(bundle);
			loader.setController(this);
			Parent dialog = loader.load();
			dialog.getStylesheets().add("css/style.css");
			Scene scene = new Scene(dialog);
			setScene(scene);
		} catch (Exception e) {
			log.error("Failed to load create dialog.", e);
		}
		this.modifiedEntry = entry;
		if (entry != null) {
			mode = Mode.EDIT;
			setTitle(bundle.getString("edit"));
			datePicker.setValue(entry.getDate());
			startTimeTextField.setValue(entry.getStartTime());
			endTimeTextField.setValue(entry.getEndTime());
			nameTextField.setText(entry.getName());
			descTextArea.setText(entry.getDescription());
		} else {
			mode = Mode.NEW;
		}
	}

	public CalendarEntry waitForEntry() {
		showAndWait();
		if (!canceled) {
			LocalDate ld = datePicker.getValue() != null ? datePicker
					.getValue() : LocalDate.now();
			LocalTime start = startTimeTextField.getValue();
			LocalTime end = endTimeTextField.getValue();
			String name = nameTextField.getText();
			String desc = descTextArea.getText();
			CalendarEntry newEntry = new CalendarEntry(ld, start, end, name,
					desc);
			switch (mode) {
			case EDIT:
				Context.getInstance()
						.getClient()
						.sendCommand(
								new ServerCommand(Command.MODIFY, newEntry,
										modifiedEntry));
				break;
			case NEW:
				Context.getInstance()
						.getClient()
						.sendCommand(
								new ServerCommand(Command.ADD, newEntry, null));
				break;
			default:
				break;
			}
			return newEntry;
		}
		return null;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		log.entry();
		canceled = true;
		okButton.setDefaultButton(true);
		okButton.setOnAction(event -> {
			canceled = false;
			hide();
		});
		cancelButton.setCancelButton(true);
		cancelButton.setOnAction(event -> hide());
		datePicker.setTooltip(new Tooltip(resources.getString("dateTT")));
		startTimeTextField.setTooltip(new Tooltip(resources
				.getString("startTT")));
		endTimeTextField.setTooltip(new Tooltip(resources.getString("endTT")));
		nameTextField.setTooltip(new Tooltip(resources.getString("nameTT")));
		descTextArea.setTooltip(new Tooltip(resources.getString("descTT")));

	}

	enum Mode {
		EDIT, NEW;
	}

}
