package Controllers;

import Core.Account;
import Core.Customer;
import Core.Transaction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

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

    public void initialize(Customer user){
        this.loggedInUser = user;
        // Clear previous items
        senderAccountDropdown.getItems().clear();


        // Populate with user's accounts
        for (Account acc : user.getAccounts()) {
            String className = acc.getClass().getSimpleName(); // "SavingsAccount", "ChequeAccount", etc.
            String displayName = className + " - " + acc.getAccountNumber();

            if (className.equals("SavingsAccount")) {
                continue; // skip SavingsAccount
            } else {
                senderAccountDropdown.getItems().add(displayName);
            }
        }


        // If the user has only one account, hide the dropdown and auto-select
        if (user.getAccounts().size() == 1) {
            senderAccountDropdown.setVisible(false);
            senderAccountDropdown.setManaged(false); // so layout adjusts

            System.out.println("Only one account found, skipping dropdown.");
        } else {
            senderAccountDropdown.setVisible(true);
            senderAccountDropdown.setManaged(true);
        }
    }
    @FXML
    private void handleWithdraw(ActionEvent event) {
        try {
            // 1️⃣ Get sender account number
            String senderValue = senderAccountDropdown.isVisible()
                    ? senderAccountDropdown.getValue()
                    : senderAccountField.getText();

            if (senderValue == null || senderValue.isEmpty()) {
                statusLabel.setText("Please select or enter a sender account.");
                return;
            }

            // Extract account number from string if dropdown is used
            String senderAccountNumber = senderAccountDropdown.isVisible()
                    ? senderValue.split(" - ")[1]  // "SavingsAccount - 12345" -> "12345"
                    : senderValue;

            // 2️⃣ Lookup Account object
            Account senderAccount = loggedInUser.getAccounts().stream()
                    .filter(a -> a.getAccountNumber().equals(senderAccountNumber))
                    .findFirst()
                    .orElse(null);

            if (senderAccount == null) {
                statusLabel.setText("Sender account not found.");
                return;
            }



            // 4️⃣ Parse and validate amount
            double amount = Double.parseDouble(senderAmount.getText());
            if (amount <= 0) {
                statusLabel.setText("Amount must be greater than zero.");
                return;
            }

            if (senderAccount.getBalance() < amount) {
                statusLabel.setText("Insufficient funds in sender account.");
                return;
            }

            // 5️⃣ Perform transfer
            senderAccount.withdraw(amount);

            Alert alert = new Alert(Alert.AlertType.INFORMATION); // Use INFORMATION for success
            alert.setTitle("Transfer Successful");               // Optional: set title
            alert.setHeaderText("Withdraw successful!");        // Main header
            alert.setContentText(amount + " has been sent to " + loggedInUser.getNumber());
            alert.showAndWait();
            Transaction t = new Transaction(
                    UUID.randomUUID().toString(),
                    "Withdraw",
                    amount,
                    senderAccount.getAccountNumber(),
                    loggedInUser.getNumber()
            );

// ✅ Add transaction to the user's transaction history
            loggedInUser.addTransaction(t);

// (Optional) Print to confirm
            System.out.println("Transaction added: " + t);


        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount entered.");
        } catch (Exception e) {
            statusLabel.setText("Error during transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            // Get controller for Transfer.fxml
            DashboardController transferController = loader.getController();

            // Pass the logged-in user to it
            transferController.initialize(loggedInUser);

            // Switch scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openTransaction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Transaction.fxml"));
            Parent root = loader.load();

            // Get controller for Transfer.fxml
            TransactionController transferController = loader.getController();

            // Pass the logged-in user to it
            transferController.initialize(loggedInUser);

            // Switch scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Transaction");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openCustomer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Profile.fxml"));
            Parent root = loader.load();

            // Get controller for Transfer.fxml
            ProfileController transferController = loader.getController();

            // Pass the logged-in user to it
            transferController.initialize(loggedInUser);

            // Switch scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profile");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openTransfer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Transfer.fxml"));
            Parent root = loader.load();

            // Get controller for Transfer.fxml
            TransferController transferController = loader.getController();

            // Pass the logged-in user to it
            transferController.initialize(loggedInUser);

            // Switch scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Transfer");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openWithdraw(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Withdraw.fxml"));
            Parent root = loader.load();

            // Get controller for Transfer.fxml
            WithdrawController transferController = loader.getController();

            // Pass the logged-in user to it
            transferController.initialize(loggedInUser);

            // Switch scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Withdraw");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out?");
        alert.setHeaderText("Do you want to log out?");
        alert.setContentText("This will log you out and you will need to login again");

        // Show the dialog and wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed
            loadFXML(event, "/views/Login.fxml", "Login");
        }
        // Otherwise, do nothing (user cancelled)
    }



    private void loadFXML(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();


            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
