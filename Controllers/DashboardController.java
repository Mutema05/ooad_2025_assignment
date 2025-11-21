package Controllers;

import Core.Account;
import Core.Customer;
import Core.Transaction;
import DAO.CustomerDAO;
import DAO.CustomerDAOImpl;
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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    private Customer loggedInUser;
    private Connection connection;

    private TransactionDAO transactionDAO;
    private CustomerDAO customerDAO;

    @FXML private Label userLabel;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> idColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> senderColumn;
    @FXML private TableColumn<Transaction, String> receiverColumn;
    @FXML private HBox accountsContainer;

    public void initialize(Customer user, Connection con) {
        this.loggedInUser = user;
        this.connection = con;

        this.transactionDAO = new TransactionDAOImpl(connection);
        this.customerDAO = new CustomerDAOImpl(connection);

        if (user == null) {
            userLabel.setText("Unknown User");
            return;
        }

        userLabel.setText("Welcome, " + user.getFullName() + "!");

        loadAccountsFromDB();
        loadTransactionsFromDB();
    }

    // -------------------------
    //  LOAD ACCOUNTS FROM DB (USER-FRIENDLY)
    // -------------------------
    private void loadAccountsFromDB() {
        accountsContainer.getChildren().clear();

        try {
            // Get latest user data including accounts
            Customer fullUser = customerDAO.read(loggedInUser.getCustomerId());
            List<Account> accounts = fullUser.getAccounts();

            if (accounts == null || accounts.isEmpty()) {
                Label emptyLabel = new Label("No accounts found for this user.");
                emptyLabel.getStyleClass().add("empty-text");
                accountsContainer.getChildren().add(emptyLabel);
                return;
            }

            for (Account acc : accounts) {
                AnchorPane card = new AnchorPane();
                card.setPrefSize(160, 100);
                card.getStyleClass().add("card");

                Label typeLabel = new Label(acc.getClass().getSimpleName() + " Account");
                typeLabel.setLayoutX(10);
                typeLabel.setLayoutY(10);
                typeLabel.getStyleClass().add("card-title");

                ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/Resources/logo.png")));
                img.setFitWidth(60);
                img.setFitHeight(60);
                img.setLayoutX(90);
                img.setLayoutY(25);
                img.setPreserveRatio(true);

                card.getChildren().addAll(typeLabel, img);
                accountsContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            showError("Failed to load accounts from database.");
            e.printStackTrace();
        }
    }

    // -------------------------
    //  LOAD TRANSACTIONS FROM DB (USER-FRIENDLY)
    // -------------------------
    private void loadTransactionsFromDB() {
        try {
            List<Transaction> transactions = transactionDAO.getByCustomerId(loggedInUser.getCustomerId());

            idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTransactionId())));
            typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTransactionType()));
            amountColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getAmount()).asObject());
            senderColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getAccountId())));
            receiverColumn.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().getTargetAccountId() != null ? String.valueOf(data.getValue().getTargetAccountId()) : "—"
            ));

            if (transactions == null || transactions.isEmpty()) {
                transactionTable.setPlaceholder(new Label("No transactions found for this user."));
            } else {
                transactionTable.getItems().setAll(transactions);
            }
        } catch (Exception e) {
            showError("Failed to load transactions from database.");
            e.printStackTrace();
        }
    }

    // -------------------------
    //  SCENE SWITCHING
    // -------------------------
    private void switchScene(ActionEvent event, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Object controller = loader.getController();
            controller.getClass().getMethod("initialize", Customer.class, Connection.class)
                    .invoke(controller, loggedInUser, connection);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception ex) {
            showError("Failed to load page: " + title);
            ex.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // -------------------------
    //  NAVIGATION
    // -------------------------
    @FXML private void openDashboard(ActionEvent e) { switchScene(e, "/views/Dashboard.fxml", "Dashboard"); }
    @FXML private void openTransaction(ActionEvent e) { switchScene(e, "/views/Transaction.fxml", "Transaction"); }
    @FXML private void openCustomer(ActionEvent e) { switchScene(e, "/views/Profile.fxml", "Profile"); }
    @FXML private void openTransfer(ActionEvent e) { switchScene(e, "/views/Transfer.fxml", "Transfer"); }
    @FXML private void openWithdraw(ActionEvent e) { switchScene(e, "/views/Withdraw.fxml", "Withdraw"); }

    // -------------------------
    //  LOGOUT
    // -------------------------
    @FXML
    private void logout(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "You will need to log in again.", ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
                stage.show();
            } catch (IOException e) {
                showError("Failed to load login page.");
                e.printStackTrace();
            }
        }
    }
}
