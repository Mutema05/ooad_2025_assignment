package Controllers;

import Core.Customer;
import Core.Transaction;
import DAO.TransactionDAO;
import DAO.TransactionDAOImpl;
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
import java.util.List;
import java.util.Optional;

public class TransactionController {

    private Customer loggedInUser;
    private Connection connection;
    private TransactionDAO transactionDAO;

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
    @FXML


    public void initialize(Customer user, Connection con) {
        this.loggedInUser = user;
        this.connection = con;
        this.transactionDAO = new TransactionDAOImpl(connection);

        // Table Columns – now user friendly
        idColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getTransactionId()))
        );

        typeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTransactionType())
        );

        amountColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getAmount()).asObject()
        );

        senderColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSenderName())
        );

        receiverColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getReceiverName() != null
                                ? c.getValue().getReceiverName()
                                : "—"
                )
        );



        loadTransactionsFromDB();
    }

    private void loadTransactionsFromDB() {
        try {
            List<Transaction> list =
                    transactionDAO.getByCustomerId(loggedInUser.getCustomerId());
            transactionTable.getItems().setAll(list);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load transactions: " + e.getMessage());
        }
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

    private void loadFXML(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController)
                ((DashboardController) controller).initialize(loggedInUser, connection);
            if (controller instanceof ProfileController)
                ((ProfileController) controller).initialize(loggedInUser, connection);
            if (controller instanceof TransferController)
                ((TransferController) controller).initialize(loggedInUser, connection);
            if (controller instanceof WithdrawController)
                ((WithdrawController) controller).initialize(loggedInUser, connection);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void openDashboard(ActionEvent e) { loadFXML(e, "/views/Dashboard.fxml", "Dashboard"); }
    @FXML private void openTransaction(ActionEvent e) { loadFXML(e, "/views/Transaction.fxml", "Transactions"); }
    @FXML private void openCustomer(ActionEvent e) { loadFXML(e, "/views/Profile.fxml", "Profile"); }
    @FXML private void openTransfer(ActionEvent e) { loadFXML(e, "/views/Transfer.fxml", "Transfer"); }
    @FXML private void openWithdraw(ActionEvent e) { loadFXML(e, "/views/Withdraw.fxml", "Withdraw"); }
}
