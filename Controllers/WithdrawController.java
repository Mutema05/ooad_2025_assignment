package Controllers;

import Core.Account;
import Core.Customer;
import Core.Transaction;
import DAO.AccountDAO;
import DAO.AccountDAOImpl;
import DAO.TransactionDAO;
import DAO.TransactionDAOImpl;
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

public class WithdrawController {

    @FXML private Customer loggedInUser;
    @FXML private ComboBox<String> senderAccountDropdown;
    @FXML private TextField senderAmount;
    @FXML private Label statusLabel;
    @FXML private Button withdrawButton;
    @FXML private Label messageLabel;

    private Connection connection;
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;

    @FXML
    public void initialize(Customer user, Connection connection) {
        this.loggedInUser = user;
        this.connection = connection;
        this.accountDAO = new AccountDAOImpl(connection);
        this.transactionDAO = new TransactionDAOImpl(connection);

        setupAccountsDropdown();
    }

    // -------------------------
    //  SETUP ACCOUNTS DROPDOWN
    // -------------------------
    private void setupAccountsDropdown() {
        senderAccountDropdown.getItems().clear();

        for (Account acc : loggedInUser.getAccounts()) {
            String className = acc.getClass().getSimpleName();
            if (!className.equals("SavingsAccount")) {
                senderAccountDropdown.getItems().add(className + " - " + acc.getAccountNumber());
            }
        }

        int numAccounts = senderAccountDropdown.getItems().size();
        if (numAccounts == 0) {
            withdrawButton.setDisable(true);
            senderAccountDropdown.setVisible(false);
            messageLabel.setText("You do not have any accounts to perform a withdrawal.");
        } else if (numAccounts == 1) {
            senderAccountDropdown.getSelectionModel().selectFirst();
        } else {
            senderAccountDropdown.setVisible(true);
        }
    }

    // -------------------------
    //  HANDLE WITHDRAWAL
    // -------------------------
    @FXML
    private void handleWithdraw(ActionEvent event) {
        statusLabel.setText("");

        String senderValue = senderAccountDropdown.getValue();
        if (senderValue == null || senderValue.isEmpty()) {
            statusLabel.setText("Please select an account to withdraw from.");
            return;
        }

        String senderAccountNumber = senderValue.split(" - ")[1];

        Account senderAccount = loggedInUser.getAccounts().stream()
                .filter(a -> a.getAccountNumber().equals(senderAccountNumber))
                .findFirst()
                .orElse(null);

        if (senderAccount == null) {
            statusLabel.setText("Selected account not found.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(senderAmount.getText());
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid amount.");
            return;
        }

        if (amount <= 0) {
            statusLabel.setText("Amount must be greater than zero.");
            return;
        }

        if (senderAccount.getBalance() < amount) {
            statusLabel.setText("Insufficient funds in account.");
            return;
        }

        // Perform withdrawal
        try {
            senderAccount.withdraw(amount);
            accountDAO.update(senderAccount);

            Transaction t = new Transaction(senderAccount.getAccountId(), "Withdraw", amount, null);
            transactionDAO.create(t);
            loggedInUser.addTransaction(t);

            statusLabel.setText("✅ Withdrawal successful: $" + String.format("%.2f", amount));
            senderAmount.clear(); // Clear the input field

        } catch (Exception e) {
            statusLabel.setText("Error during withdrawal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -------------------------
    //  NAVIGATION METHODS
    // -------------------------
    @FXML private void openDashboard(ActionEvent event) { loadFXML(event, "/views/Dashboard.fxml", "Dashboard", true); }
    @FXML private void openTransaction(ActionEvent event) { loadFXML(event, "/views/Transaction.fxml", "Transaction", true); }
    @FXML private void openCustomer(ActionEvent event) { loadFXML(event, "/views/Profile.fxml", "Profile", true); }
    @FXML private void openTransfer(ActionEvent event) { loadFXML(event, "/views/Transfer.fxml", "Transfer", true); }
    @FXML private void openWithdraw(ActionEvent event) { loadFXML(event, "/views/Withdraw.fxml", "Withdraw", true); }

    // -------------------------
    //  LOGOUT
    // -------------------------
    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "You will need to log in again.", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Log Out");
        alert.setHeaderText("Do you want to log out?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            loadFXML(event, "/views/Login.fxml", "Login", false);
        }
    }

    // -------------------------
    //  FXML LOADING
    // -------------------------
    private void loadFXML(ActionEvent event, String fxmlPath, String title, boolean passUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (passUser) {
                Object controller = loader.getController();
                if (controller instanceof DashboardController) ((DashboardController) controller).initialize(loggedInUser, connection);
                else if (controller instanceof TransactionController) ((TransactionController) controller).initialize(loggedInUser, connection);
                else if (controller instanceof ProfileController) ((ProfileController) controller).initialize(loggedInUser, connection);
                else if (controller instanceof TransferController) ((TransferController) controller).initialize(loggedInUser, connection);
                else if (controller instanceof WithdrawController) ((WithdrawController) controller).initialize(loggedInUser, connection);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            showError("Failed to load page: " + title);
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
