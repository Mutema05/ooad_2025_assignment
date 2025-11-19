package Controllers;

import Core.Customer;
import Core.Transaction;
import Core.DBConnection;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.util.Optional;

public class TransactionController {

    private Customer loggedInUser;
    private Connection connection;

    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, String> idColumn;
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    @FXML
    private TableColumn<Transaction, Double> amountColumn;
    @FXML
    private TableColumn<Transaction, String> senderColumn;
    @FXML
    private TableColumn<Transaction, String> receiverColumn;

    /**
     * Initialize with logged-in user and database connection
     */
    public void initialize(Customer user, Connection con){
        this.loggedInUser = user;
        this.connection = con;

        // Set up table columns
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getTransactionId())));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionType()));
        amountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        senderColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getAccountId())));
        receiverColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getTargetAccountId() != null ? String.valueOf(cellData.getValue().getTargetAccountId()) : ""
        ));

        // Load transactions for logged-in user
        transactionTable.getItems().setAll(loggedInUser.getTransactions());
    }

    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out?");
        alert.setHeaderText("Do you want to log out?");
        alert.setContentText("This will log you out and you will need to login again");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            loadFXML(event, "/views/Login.fxml", "Login");
        }
    }

    // Generic FXML loader for navigation
    private void loadFXML(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Pass user and connection to next controller if needed
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).initialize(loggedInUser, connection);
            } else if (controller instanceof ProfileController) {
                ((ProfileController) controller).initialize(loggedInUser, connection);
            } else if (controller instanceof TransferController) {
                ((TransferController) controller).initialize(loggedInUser, connection);
            } else if (controller instanceof WithdrawController) {
                ((WithdrawController) controller).initialize(loggedInUser, connection);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Navigation methods
    @FXML private void openDashboard(ActionEvent event) { loadFXML(event, "/views/Dashboard.fxml", "Dashboard"); }
    @FXML private void openTransaction(ActionEvent event) { loadFXML(event, "/views/Transaction.fxml", "Transaction"); }
    @FXML private void openCustomer(ActionEvent event) { loadFXML(event, "/views/Profile.fxml", "Profile"); }
    @FXML private void openTransfer(ActionEvent event) { loadFXML(event, "/views/Transfer.fxml", "Transfer"); }
    @FXML private void openWithdraw(ActionEvent event) { loadFXML(event, "/views/Withdraw.fxml", "Withdraw"); }

}
