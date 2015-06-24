package de.malbertz.calendar.client.ui.dialogs;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ErrorDialog extends Stage implements Initializable{

	@FXML
	private Label errorLabel;
	@FXML
	private Button okButton;
	
	public ErrorDialog(ResourceBundle bundle, String str) {
		super(StageStyle.UTILITY);
		initModality(Modality.WINDOW_MODAL);
		setTitle(bundle.getString("error"));
		setResizable(false);

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(
					"/fxml/ErrorDialog.fxml"));
			loader.setResources(bundle);
			loader.setController(this);
			Parent dialog = loader.load();
			dialog.getStylesheets().add("css/style.css");
			Scene scene = new Scene(dialog);
			setScene(scene);
		} catch (Exception e) {
			
		}
		errorLabel.setText(str);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		okButton.setOnAction(event -> hide());
	}

}
