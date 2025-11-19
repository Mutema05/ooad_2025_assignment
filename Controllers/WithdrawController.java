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

    @FXML
    private Customer loggedInUser;

    @FXML
    private ComboBox<String> senderAccountDropdown;

    @FXML
    private TextField senderAmount;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField senderAccountField;

    private Connection connection;

    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;

    // ✅ Initialize controller with DB connection
    public void initialize(Customer user, Connection connection) {
        this.loggedInUser = user;
        this.connection = connection;
        this.accountDAO = new AccountDAOImpl(connection);
        this.transactionDAO = new TransactionDAOImpl(connection);

        // Clear previous items
        senderAccountDropdown.getItems().clear();

        // Populate dropdown with non-savings accounts
        for (Account acc : user.getAccounts()) {
            String className = acc.getClass().getSimpleName();
            if (!className.equals("SavingsAccount")) {
                senderAccountDropdown.getItems().add(className + " - " + acc.getAccountNumber());
            }
        }

        // Handle single account scenario
        if (senderAccountDropdown.getItems().size() == 1) {
            senderAccountDropdown.setVisible(false);
            senderAccountDropdown.setManaged(false);
            senderAccountField.setText(senderAccountDropdown.getItems().get(0).split(" - ")[1]);
        } else {
            senderAccountDropdown.setVisible(true);
            senderAccountDropdown.setManaged(true);
        }
    }

    @FXML
    private void handleWithdraw(ActionEvent event) {
        try {
            // 1️⃣ Get account number from dropdown or textfield
            String senderValue = senderAccountDropdown.isVisible()
                    ? senderAccountDropdown.getValue()
                    : senderAccountField.getText();

            if (senderValue == null || senderValue.isEmpty()) {
                statusLabel.setText("Please select or enter a sender account.");
                return;
            }

            String senderAccountNumber = senderAccountDropdown.isVisible()
                    ? senderValue.split(" - ")[1]
                    : senderValue;

            // 2️⃣ Lookup account
            Account senderAccount = loggedInUser.getAccounts().stream()
                    .filter(a -> a.getAccountNumber().equals(senderAccountNumber))
                    .findFirst()
                    .orElse(null);

            if (senderAccount == null) {
                statusLabel.setText("Sender account not found.");
                return;
            }

            // 3️⃣ Parse amount
            double amount = Double.parseDouble(senderAmount.getText());
            if (amount <= 0) {
                statusLabel.setText("Amount must be greater than zero.");
                return;
            }

            if (senderAccount.getBalance() < amount) {
                statusLabel.setText("Insufficient funds.");
                return;
            }

            // 4️⃣ Withdraw & persist
            senderAccount.withdraw(amount);
            accountDAO.update(senderAccount);

            // 5️⃣ Create & persist transaction
            Transaction t = new Transaction(senderAccount.getAccountId(), "Withdraw", amount, null);
            transactionDAO.create(t);
            loggedInUser.addTransaction(t);

            // 6️⃣ Update status
            statusLabel.setText("Withdrawal successful: $" + String.format("%.2f", amount));
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount entered.");
        } catch (Exception e) {
            statusLabel.setText("Error during withdrawal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===================== Navigation Methods =====================
    @FXML
    private void openDashboard(ActionEvent event) {
        loadFXML(event, "/views/Dashboard.fxml", "Dashboard", true);
    }

    @FXML
    private void openTransaction(ActionEvent event) {
        loadFXML(event, "/views/Transaction.fxml", "Transaction", true);
    }

    @FXML
    private void openCustomer(ActionEvent event) {
        loadFXML(event, "/views/Profile.fxml", "Profile", true);
    }

    @FXML
    private void openTransfer(ActionEvent event) {
        loadFXML(event, "/views/Transfer.fxml", "Transfer", true);
    }

    @FXML
    private void openWithdraw(ActionEvent event) {
        loadFXML(event, "/views/Withdraw.fxml", "Withdraw", true);
    }

    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out?");
        alert.setHeaderText("Do you want to log out?");
        alert.setContentText("This will log you out and you will need to login again");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            loadFXML(event, "/views/Login.fxml", "Login", false);
        }
    }

    // General method to load FXML with optional controller initialization
    private void loadFXML(ActionEvent event, String fxmlPath, String title, boolean passUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (passUser) {
                Object controller = loader.getController();
                if (controller instanceof DashboardController) {
                    ((DashboardController) controller).initialize(loggedInUser,connection);
                } else if (controller instanceof TransactionController) {
                    ((TransactionController) controller).initialize(loggedInUser,connection);
                } else if (controller instanceof ProfileController) {
                    ((ProfileController) controller).initialize(loggedInUser,connection);
                } else if (controller instanceof TransferController) {
                    ((TransferController) controller).initialize(loggedInUser, connection);
                } else if (controller instanceof WithdrawController) {
                    ((WithdrawController) controller).initialize(loggedInUser, connection);
                }
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
