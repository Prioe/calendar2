package de.malbertz.calendar.server.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import de.malbertz.calendar.server.core.Server;

public class GraphicalUI extends Application implements Initializable {


	private Server server = null;
	private TextUI handler = null;
	
	@FXML
	private TextArea consoleArea;
	@FXML
	private Button sendButton;
	@FXML
	private TextField commandTextField;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {	
		this.server = Context.getInstance().getServer();
		this.handler = new TextUI(server);
		Console console = new Console(consoleArea);
		PrintStream ps = new PrintStream(console, true);
        System.setOut(ps);
        
        sendButton.setOnAction(event -> {
        	if(!handler.handle(commandTextField.getText())) {
        		Platform.exit();
        	}
        	commandTextField.clear();
        });
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(
				"/ServerView.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Calendar Server");
		primaryStage.setMinWidth(400);
		primaryStage.setMinHeight(300);		
		primaryStage.show();
	}

	private class Console extends OutputStream {
		private TextArea output;

		public Console(TextArea ta) {
			this.output = ta;
		}


		@Override
		public void write(int b) throws IOException {
			output.appendText(String.valueOf((char) b));
			
		}

	}
	

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public TextUI getHandler() {
		return handler;
	}

	public void setHandler(TextUI handler) {
		this.handler = handler;
	}

}
