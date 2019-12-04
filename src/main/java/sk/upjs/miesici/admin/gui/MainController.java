package sk.upjs.miesici.admin.gui;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import sk.upjs.miesici.admin.storage.*;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static sk.upjs.miesici.admin.storage.MySQLCustomerDao.errorCheck;
import static sk.upjs.miesici.admin.storage.MySQLEntranceDao.idOfEntrance;

public class MainController {

    private CustomerDao customerDao = DaoFactory.INSTANCE.getCustomerDao();
    private EntranceDao entranceDao = DaoFactory.INSTANCE.getEntranceDao();
    private ObservableList<Customer> customersModel;
    public static long idOfCustomer;

    @FXML
    private TextField filterTextField;

    @FXML
    private Button addCustomer;

    @FXML
    private Button entryCustomer;

    @FXML
    private Label textEntrance;

    @FXML
    public TableView<Customer> customerTableView;


    @FXML
    void addMouseEntered(MouseEvent event) {
        Tooltip tt = new Tooltip();
        tt.setText("Pridaj používateľa");
        addCustomer.setTooltip(tt);
    }

    @FXML
    void entryMouseEntered(MouseEvent event) {
        Tooltip tt = new Tooltip();
        tt.setText("Vstupy");
        entryCustomer.setTooltip(tt);
    }

    @FXML
    void addCustomerButtonClick(ActionEvent event) {
        CustomerAddController controller = new CustomerAddController();
        showAddCustomerAddWindow(controller, "CustomerAdd.fxml");
        if (controller.getSavedCustomer() != null) {
            customersModel = FXCollections.observableArrayList(customerDao.getAll());
            customerTableView.setItems(FXCollections.observableArrayList(customersModel));
            filterTableView();
        }
    }

    @FXML
    void entryCustomerButtonClick(ActionEvent event) {
        EntranceController controller = new EntranceController();
        showEntryWindow(controller, "Entry.fxml");
    }

    @FXML
    void arrivalButtonClick(ActionEvent event) throws InterruptedException {
        Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        Entrance entrance = new Entrance();
        entrance.setKlient_id(selectedCustomer.getId());
        entrance.setName(selectedCustomer.getName());
        entrance.setSurname(selectedCustomer.getSurname());

        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        entrance.setArrival(format.format(ldt));
        entranceDao.saveArrival(entrance);

        if (idOfEntrance != 0) {
            textEntrance.setText("Vstup bol zaznamenaný!");
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(2));
            pauseTransition.setOnFinished(e -> textEntrance.setText(""));
            pauseTransition.play();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Neplatný príchod");
            alert.setHeaderText("Zákazík už má zaznamenaný vstup.");
            alert.setContentText("Prosím zaznačte odchod!");
            alert.show();
        }

    }

    @FXML
    void exitButtonClick(ActionEvent event) {
        Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        Entrance entrance = new Entrance();
        entrance.setKlient_id(selectedCustomer.getId());
        entrance.setName(selectedCustomer.getName());
        entrance.setSurname(selectedCustomer.getSurname());

        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        entrance.setExit(format.format(ldt));
        entranceDao.saveExit(entrance);

        if (idOfEntrance != 0) {
            textEntrance.setText("Odchod bol zaznamenaný!");
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(2));
            pauseTransition.setOnFinished(e -> textEntrance.setText(""));
            pauseTransition.play();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Neplatný odchod");
            alert.setHeaderText("Zákazík nemá zaznamenaný vstup.");
            alert.setContentText("Prosím zaznačte vstup!");
            alert.show();

        }

    }

    @FXML
    void initialize() {
        customersModel = FXCollections.observableArrayList(customerDao.getAll());
        customerTableView.setItems(FXCollections.observableArrayList(customersModel));

        TableColumn<Customer, String> idCol = new TableColumn<>("id");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerTableView.getColumns().add(idCol);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Meno");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        customerTableView.getColumns().add(nameCol);

        TableColumn<Customer, String> surnameCol = new TableColumn<>("Priezvisko");
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
        customerTableView.getColumns().add(surnameCol);

        TableColumn<Customer, String> addressCol = new TableColumn<>("Adresa");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        customerTableView.getColumns().add(addressCol);

        TableColumn<Customer, String> emailCol = new TableColumn<>("E-mail");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        customerTableView.getColumns().add(emailCol);

        TableColumn<Customer, Double> creditCol = new TableColumn<>("Kredit");
        creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        customerTableView.getColumns().add(creditCol);

        TableColumn<Customer, Date> permanentkaCol = new TableColumn<>("Permanentka");
        permanentkaCol.setCellValueFactory(new PropertyValueFactory<>("membershipExp"));
        customerTableView.getColumns().add(permanentkaCol);

        TableColumn<Customer, Date> adminCol = new TableColumn<>("Admin");
        adminCol.setCellValueFactory(new PropertyValueFactory<>("admin"));
        customerTableView.getColumns().add(adminCol);

        customerTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() > 1) {
                onEdit();
            }
        });
        filterTableView();
    }

    private void onEdit() {
        if (customerTableView.getSelectionModel().getSelectedItem() != null) {
            Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
            idOfCustomer = selectedCustomer.getId();

            CustomerEditController controller = new CustomerEditController();
            showEditCustomerWindow(controller, "CustomerEdit.fxml");

            // refresh table
            customersModel = FXCollections.observableArrayList(customerDao.getAll());
            customerTableView.setItems(FXCollections.observableArrayList(customersModel));
            filterTableView();
        }

    }

    private void showAddCustomerAddWindow(CustomerAddController controller, String nameOfFxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(nameOfFxml));
            fxmlLoader.setController(controller);
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            Stage modalStage = new Stage();
            modalStage.setScene(scene);
            modalStage.setResizable(false);
            modalStage.getIcons().add(new Image("https://www.tailorbrands.com/wp-content/uploads/2019/04/Artboard-5-copy-13xxhdpi.png"));
            modalStage.setTitle("Pridanie");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEntryWindow(EntranceController controller, String nameOfFxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(nameOfFxml));
            fxmlLoader.setController(controller);
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            Stage modalStage = new Stage();
            modalStage.setScene(scene);
            modalStage.setMinHeight(800);
            modalStage.setMinWidth(500);
            modalStage.getIcons().add(new Image("https://www.tailorbrands.com/wp-content/uploads/2019/04/Artboard-5-copy-13xxhdpi.png"));
            modalStage.setTitle("Vstupy");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditCustomerWindow(CustomerEditController controller, String nameOfFxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(nameOfFxml));
            fxmlLoader.setController(controller);
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            Stage modalStage = new Stage();
            modalStage.setScene(scene);
            modalStage.setResizable(false);
            modalStage.getIcons().add(new Image("https://www.tailorbrands.com/wp-content/uploads/2019/04/Artboard-5-copy-13xxhdpi.png"));
            modalStage.setTitle("Editácia");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterTableView() {
        // https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        FilteredList<Customer> filteredData = new FilteredList<>(customersModel, p -> true);

        // 2. Set the filter Predicate whenever the filter changes.
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();
                String surname = StringUtils.stripAccents(customer.getSurname().toLowerCase());
                if (surname.contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                } else return customer.getId().toString().contains(lowerCaseFilter);
            });
        });

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<Customer> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(customerTableView.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        customerTableView.setItems(sortedData);
    }
}