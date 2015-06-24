package de.malbertz.calendar.client;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.client.core.Client;
import de.malbertz.calendar.client.core.Client.ClientState;
import de.malbertz.calendar.client.core.Context;
import de.malbertz.calendar.client.ui.dialogs.LoginDialog;

/**
 * Main class of the client application.
 * <p>
 * Starts the client application.
 * 
 * @author Michael Albertz
 *
 */
public class Main extends Application {

	private static final Logger log = LogManager.getLogger(Main.class);
	/** Client object the application is running on. */
	private Client client;

	@Override
	public void start(Stage primaryStage) throws Exception {
		log.entry(primaryStage);

		ResourceBundle bundle = ResourceBundle.getBundle("bundles.UIBundle",
				new Locale("en"));

		LoginDialog login = new LoginDialog(bundle);
		if (login.getOwner() == null) {
			login.initOwner(primaryStage);
		}

		client = login.waitForAction();
		if (client.getClientState() != ClientState.AUTHENTICATED) {
			log.info("Exit from login prompt with client state: "
					+ client.getClientState());
			return;
		}
		Context.getInstance().setClient(client);

		FXMLLoader loader = new FXMLLoader(getClass().getResource(
				"/fxml/MainScene.fxml"));
		loader.setResources(bundle);
		Parent root = loader.load();
		root.getStylesheets().add("css/style.css");
		Scene scene = new Scene(root);

		primaryStage.setScene(scene);
		primaryStage.setTitle(bundle.getString("appTitle") + " - "
				+ client.getHostName() + ":" + client.getPort());
		primaryStage.setMinWidth(400);
		primaryStage.setMinHeight(300);
		primaryStage.show();

	}

	/**
	 * Main method of the client application.
	 * <p>
	 * Application entry point.
	 */
	public static void main(String[] args) {
		launch(args);

	}

	@Override
	public void stop() {
		client.disconnect();
		log.info("Close app with client state: " + client.getClientState());
	}

}
