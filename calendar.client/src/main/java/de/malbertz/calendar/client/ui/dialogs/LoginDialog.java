package de.malbertz.calendar.client.ui.dialogs;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.client.core.Client;
import de.malbertz.calendar.client.core.Client.ClientState;

public class LoginDialog extends Stage implements Initializable {

   private static final Logger log = LogManager.getLogger(LoginDialog.class);

   @FXML
   private Parent root;
   @FXML
   private Label loginLabel;
   @FXML
   private Text notificationText;
   @FXML
   private TextField nameTextField;
   @FXML
   private TextField passwordTextField;
   @FXML
   private TextField serverTextField;
   @FXML
   private Button loginButton;
   @FXML
   private Button cancelButton;
   @FXML
   private ProgressBar progressBar;

   private SimpleBooleanProperty working;
   private ResourceBundle bundle;
   private Client client;

   public LoginDialog(ResourceBundle bundle) {
      super(StageStyle.UTILITY);
      initModality(Modality.WINDOW_MODAL);
      setTitle(bundle.getString("login"));
      setResizable(false);

      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource(
               "/fxml/LoginDialog.fxml"));
         loader.setResources(bundle);
         loader.setController(this);
         Parent dialog = loader.load();
         dialog.getStylesheets().add("css/style.css");
         Scene scene = new Scene(dialog);
         setScene(scene);
      } catch (Exception e) {
         log.error("Failed to load login dialog.", e);
      }

   }

   @Override
   public void initialize(URL location, ResourceBundle resources) {

      client = new Client();
      this.bundle = resources;
      this.working = new SimpleBooleanProperty(false);

      // set label graphic
      loginLabel.setGraphic(new ImageView(new Image(getClass()
            .getResourceAsStream("/images/login_small.png"))));

      serverTextField.setText("localhost:27999");

      loginButton.setOnAction(event -> login());
      loginButton.setDefaultButton(true);

      cancelButton.setOnAction(event -> {
         stopProgressBar();
         client.disconnect();
         hide();
      });
      cancelButton.setCancelButton(true);

      loginButton.disableProperty().bind(
            Bindings.equal("", nameTextField.textProperty())
                  .or(Bindings.equal("", passwordTextField.textProperty()))
                  .or(Bindings.equal("", serverTextField.textProperty()))
                  .or(working));

      setOnCloseRequest(event -> client.disconnect());

      Platform.runLater(() -> nameTextField.requestFocus());

   }

   /**
    * @param resources
    */
   private void login() {

      Task<Object> login = new Task<Object>() {

         {
            setOnSucceeded(event -> {
               if (client.getClientState() == ClientState.AUTHENTICATED) {
                  hide();
               }
            });
         }

         @Override
         protected Object call() throws Exception {
            notificationText.setText("");
            startProgressBar();
            String[] server = serverTextField.getText().split(":");
            if (server.length != 2) {
               notificationText.setText(bundle.getString("invalidServer"));
               return null;
            }

            try {
               client.connect(server[0], Integer.parseInt(server[1]),
                     nameTextField.getText(), passwordTextField.getText());
            } catch (NumberFormatException nfe) {
               notificationText.setText(bundle.getString("invalidServer"));
               client.disconnect();
            } catch (ConnectException ce) {
               notificationText.setText(bundle.getString("connectionRefused"));
            } catch (SocketException se) {
               // cancel button pressed while authenticating, doesn't
               // matter since we quit the app anyways
            } catch (IOException ioe) {
               log.fatal(
                     "I/O Exception caught while trying to connect to a server.",
                     ioe);
            }
            if (client.getClientState() == ClientState.AUTHENTICATED) {
               notificationText.setText(bundle.getString("connected"));
               log.info("Successfully authenticated");
            } else if (client.getClientState() == ClientState.CONNECTED) {
               notificationText.setText(bundle.getString("incorrectLogin"));
            }
            stopProgressBar();
            return null;
         }

      };
      new Thread(login).start();
   }

   private boolean stopProgress = true;

   private void startProgressBar() {
      progressBar.setVisible(true);
      progressBar.setProgress(0);
      stopProgress = false;
      working.set(true);
      Task<Object> inf = new Task<Object>() {

         @Override
         protected Object call() throws Exception {
            while (!stopProgress) {
               for (double p = 0; p < 1; p = p + 0.01d) {
                  if (stopProgress) {
                     break;
                  }
                  Thread.sleep(7);
                  progressBar.setProgress(p);
               }
            }
            progressBar.setVisible(false);
            working.set(false);
            return null;
         }

      };
      new Thread(inf).start();
   }

   private void stopProgressBar() {
      stopProgress = true;
   }

   public Client waitForAction() {
      showAndWait();
      return client;
   }

}
