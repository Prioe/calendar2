package de.malbertz.calendar.client.ui.elements;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.malbertz.calendar.client.ui.elements.nodes.CalendarContextMenu;
import de.malbertz.calendar2.CalendarEntry;

public class AgendaView extends TableView<CalendarEntry> implements
      Initializable, ContentPane<CalendarEntry> {

   private static final Logger log = LogManager.getLogger(AgendaView.class);

   @FXML
   private TableView<CalendarEntry> root;
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

   private final SimpleListProperty<CalendarEntry> entryList = new SimpleListProperty<CalendarEntry>();
   private CalendarEntry initialEntry;
   private ResourceBundle bundle;

   public AgendaView(ResourceBundle bundle, CalendarEntry initialEntry)
         throws Exception {
      this.initialEntry = initialEntry;
      if (bundle == null) {
         throw new ExceptionInInitializerError();
      }
      log.info(bundle);
      FXMLLoader loader = new FXMLLoader(getClass().getResource(
            "/fxml/AgendaView.fxml"));
      loader.setResources(bundle);
      loader.setRoot(this);
      loader.setController(this);
      loader.load();

   }

   @Override
   public void initialize(URL location, ResourceBundle resources) {
      log.entry();
      log.info(bundle);
      root.setItems(entryList);
      root.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

      dayColumn
            .setCellValueFactory(new PropertyValueFactory<CalendarEntry, LocalDate>(
                  "date"));
      dayColumn
            .setCellFactory(param -> new TableCell<CalendarEntry, LocalDate>() {
               @Override
               protected void updateItem(LocalDate item, boolean empty) {
                  super.updateItem(item, empty);
                  if (!empty) {
                     DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                           "EEE, d MMM yyyy", resources.getLocale());
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

      root.setContextMenu(new CalendarContextMenu(resources, root));

      root.getSelectionModel().select(initialEntry);

   }

   public SimpleListProperty<CalendarEntry> entryListProperty() {
      return entryList;
   }

   @Override
   public CalendarEntry getSelectedItem() {
      return root.getSelectionModel().getSelectedItem();
   }

}
