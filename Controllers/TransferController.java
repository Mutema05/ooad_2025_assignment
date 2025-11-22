package Controllers;

import Core.Account;
import Core.Customer;
import Core.Transaction;
import DAO.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransferController {

    private Customer loggedInUser;
    private Connection connection;
    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO;
    private CustomerDAO customerDAO;


    @FXML
    private ComboBox<String> senderAccountDropdown;
    @FXML
    private Button transferButton;
    @FXML
    private Label messageLabel;

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

    public void initialize(Customer user, Connection con) {
        this.loggedInUser = user;
        this.connection = con;
        this.transactionDAO = new TransactionDAOImpl(con);
        this.accountDAO = new AccountDAOImpl(con);
        this.customerDAO = new CustomerDAOImpl(connection);

        List<Customer> allCustomers = customerDAO.getAllCustomers();

        // Populate sender dropdown with user's accounts
        senderAccountDropdown.getItems().clear();
        for (Account acc : user.getAccounts()) {
            String displayName = acc.getClass().getSimpleName() + " - " + acc.getAccountNumber();
            senderAccountDropdown.getItems().add(displayName);
        }

        // Disable transfer button if no accounts
        if (user.getAccounts().isEmpty()) {
            transferButton.setDisable(true);
            messageLabel.setText("You do not have any accounts to perform a transfer.");
        }

        // Setup receiver dropdown as editable
        receiverAccountDropdown.setEditable(true);
        receiverAccountDropdown.setPromptText("Type recipient name or number");

        // Autocomplete listener
        receiverAccountDropdown.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isBlank()) {
                receiverAccountDropdown.hide();
                return;
            }

            // Build suggestions
            List<String> suggestions = new ArrayList<>();
            for (Customer c : allCustomers) {
                if (c.getFullName().toLowerCase().contains(newText.toLowerCase())
                        || c.getPhoneNumber().contains(newText)) {

                    // Prefer Cheque account
                    Account targetAcc = c.getAccounts().stream()
                            .filter(a -> a.getClass().getSimpleName().equals("Cheque"))
                            .findFirst()
                            .orElse(c.getAccounts().isEmpty() ? null : c.getAccounts().get(0));

                    if (targetAcc != null) {
                        suggestions.add(c.getFullName() + " - " + targetAcc.getClass().getSimpleName()
                                + " - " + targetAcc.getAccountNumber());
                    }
                }
            }

            // Update ComboBox safely
            receiverAccountDropdown.getItems().setAll(suggestions);
            if (!suggestions.isEmpty()) receiverAccountDropdown.show();
            else receiverAccountDropdown.hide();
        });


    }
    @FXML

    private void handleTransfer(ActionEvent event) {
        try {
            // --- Sender account ---
            String senderValue = senderAccountDropdown.getValue();
            if (senderValue == null || senderValue.isBlank() || !senderValue.contains(" - ")) {
                statusLabel.setText("Please select or enter a valid sender account.");
                return;
            }

            String senderAccNum = senderValue.split(" - ")[1].trim();
            Account senderAccount = accountDAO.getByAccountNumber(senderAccNum);
            if (senderAccount == null) {
                statusLabel.setText("Sender account not found.");
                return;
            }

            // --- Receiver account ---
            String receiverText = receiverAccountDropdown.getEditor().getText().trim();
            if (receiverText.isEmpty()) {
                statusLabel.setText("Please enter a recipient.");
                return;
            }

            List<Customer> allCustomers = customerDAO.getAllCustomers();
            Customer receiverCustomer = null;
            Account receiverAccount = null;

            outerLoop:
            for (Customer c : allCustomers) {
                for (Account a : c.getAccounts()) {
                    String display = c.getFullName() + " - " + a.getClass().getSimpleName() + " - " + a.getAccountNumber();
                    if (display.equalsIgnoreCase(receiverText)) {
                        receiverCustomer = c;
                        receiverAccount = a;
                        break outerLoop;
                    }
                }
            }

            if (receiverCustomer == null || receiverAccount == null) {
                statusLabel.setText("Recipient not found or has no accounts.");
                return;
            }

            // --- Amount validation ---
            double amount;
            try {
                amount = Double.parseDouble(senderAmount.getText().trim());
            } catch (NumberFormatException e) {
                statusLabel.setText("Invalid amount entered.");
                return;
            }

            if (amount <= 0) {
                statusLabel.setText("Amount must be greater than zero.");
                return;
            }

            if (senderAccount.getBalance() < amount) {
                statusLabel.setText("Insufficient funds.");
                return;
            }

            // --- Perform transfer ---
            senderAccount.withdraw(amount);
            receiverAccount.deposit(amount);

            accountDAO.update(senderAccount);
            accountDAO.update(receiverAccount);

            // --- Record transaction ---
            Transaction t = new Transaction(senderAccount.getAccountId(), "Transfer", amount, receiverAccount.getAccountId());
            transactionDAO.create(t);
            loggedInUser.addTransaction(t);

            // --- Confirmation ---
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Transfer Successful");
            alert.setHeaderText("Transfer completed");
            alert.setContentText("Sent " + amount + " to " + receiverCustomer.getFullName()
                    + " (" + receiverAccount.getClass().getSimpleName() + " - " + receiverAccount.getAccountNumber() + ")");
            alert.showAndWait();

            statusLabel.setText("Transfer successful ✅");

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
