package Controllers;

import Core.Account;
import Core.Customer;
import Core.Transaction;
import DAO.TransactionDAO;
import DAO.TransactionDAOImpl;
import DAO.AccountDAO;
import DAO.AccountDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.util.Optional;

public class TransferController {

    private Customer loggedInUser;
    private Connection connection;
    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO;

    @FXML
    private ComboBox<String> senderAccountDropdown;

    @FXML
    private ComboBox<String> receiverAccountDropdown;

    @FXML
    private TextField senderAccountField;

    @FXML
    private TextField receiverAccountField;

    @FXML
    private TextField senderAmount;

    @FXML
    private Label statusLabel;

    // ✅ Correct initialization with both user and connection
    public void initialize(Customer user, Connection con) {
        this.loggedInUser = user;
        this.connection = con;
        this.transactionDAO = new TransactionDAOImpl(con);
        this.accountDAO = new AccountDAOImpl(con);

        // Clear dropdowns first
        senderAccountDropdown.getItems().clear();
        receiverAccountDropdown.getItems().clear();

        // Populate sender accounts (current user's)
        for (Account acc : user.getAccounts()) {
            String displayName = acc.getClass().getSimpleName() + " - " + acc.getAccountNumber();
            senderAccountDropdown.getItems().add(displayName);
        }

        // Populate receiver dropdown with all accounts (from DB)
        for (Account acc : accountDAO.getAllAccounts()) {
            String displayName = acc.getClass().getSimpleName() + " - " + acc.getAccountNumber();
            receiverAccountDropdown.getItems().add(displayName);
        }

        // Hide dropdowns if only one account
        if (user.getAccounts().size() == 1) {
            senderAccountDropdown.setVisible(false);
            senderAccountDropdown.setManaged(false);
            System.out.println("Only one sender account, dropdown hidden.");
        }
    }

    @FXML
    private void handleTransfer(ActionEvent event) {
        try {
            // Sender selection
            String senderValue = senderAccountDropdown.isVisible()
                    ? senderAccountDropdown.getValue()
                    : senderAccountField.getText();

            if (senderValue == null || senderValue.isEmpty()) {
                statusLabel.setText("Please select or enter a sender account.");
                return;
            }

            String senderAccountNumber = senderValue.contains(" - ")
                    ? senderValue.split(" - ")[1]
                    : senderValue;

            Account senderAccount = accountDAO.getByAccountNumber(senderAccountNumber);
            if (senderAccount == null) {
                statusLabel.setText("Sender account not found.");
                return;
            }

            // Receiver selection
            String receiverValue = receiverAccountDropdown.isVisible()
                    ? receiverAccountDropdown.getValue()
                    : receiverAccountField.getText();

            if (receiverValue == null || receiverValue.isEmpty()) {
                statusLabel.setText("Please select or enter a receiver account.");
                return;
            }

            String receiverAccountNumber = receiverValue.contains(" - ")
                    ? receiverValue.split(" - ")[1]
                    : receiverValue;

            Account receiverAccount = accountDAO.getByAccountNumber(receiverAccountNumber);
            if (receiverAccount == null) {
                statusLabel.setText("Receiver account not found.");
                return;
            }

            // Amount validation
            double amount = Double.parseDouble(senderAmount.getText());
            if (amount <= 0) {
                statusLabel.setText("Amount must be greater than zero.");
                return;
            }

            if (senderAccount.getBalance() < amount) {
                statusLabel.setText("Insufficient funds.");
                return;
            }

            // Perform transfer
            senderAccount.withdraw(amount);
            receiverAccount.deposit(amount);

            // Update DB balances
            accountDAO.update(senderAccount);
            accountDAO.update(receiverAccount);

            // Create and save transaction
            Transaction t = new Transaction(
                    senderAccount.getAccountId(),
                    "Transfer",
                    amount,
                    receiverAccount.getAccountId()
            );
            transactionDAO.create(t);

            // Add transaction to user (for session view)
            loggedInUser.addTransaction(t);

            // Confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Transfer Successful");
            alert.setHeaderText("Transfer completed");
            alert.setContentText(amount + " has been sent to account " + receiverAccountNumber);
            alert.showAndWait();

            statusLabel.setText("Transfer successful ✅");

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount entered.");
        } catch (Exception e) {
            statusLabel.setText("Error during transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Navigation helpers
    @FXML
    private void openDashboard(ActionEvent event) { loadScene(event, "/views/Dashboard.fxml", "Dashboard"); }

    @FXML
    private void openTransaction(ActionEvent event) { loadScene(event, "/views/Transaction.fxml", "Transactions"); }

    @FXML
    private void openCustomer(ActionEvent event) { loadScene(event, "/views/Profile.fxml", "Profile"); }

    @FXML
    private void openWithdraw(ActionEvent event) { loadScene(event, "/views/Withdraw.fxml", "Withdraw"); }

    @FXML
    private void openTransfer(ActionEvent event) { loadScene(event, "/views/Transfer.fxml", "Transfer"); }

    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out?");
        alert.setHeaderText("Confirm logout");
        alert.setContentText("This will log you out of the system.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
            loadScene(event, "/views/Login.fxml", "Login");
    }

    // ✅ Utility for consistent scene loading
    private void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            // Pass loggedInUser and connection to new controllers (if supported)
            try {
                controller.getClass().getMethod("initialize", Customer.class, Connection.class)
                        .invoke(controller, loggedInUser, connection);
            } catch (NoSuchMethodException ignored) { }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error passing data to new scene: " + e.getMessage());
        }
    }
}
