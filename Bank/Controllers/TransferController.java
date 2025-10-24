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

public class TransferController {


    @FXML
    private Customer loggedInUser;
    @FXML

    private ComboBox<String> senderAccountDropdown;

    @FXML
    private ComboBox<String> recieverAccountDropdown;

    @FXML
    private TextField senderAccountField;

    @FXML
    private TextField receiverAccountField;

    @FXML
    private TextField senderAmount;

    @FXML
    private Label statusLabel;

    public void initialize(Customer user){
        this.loggedInUser = user;
        // Clear previous items
        senderAccountDropdown.getItems().clear();
        recieverAccountDropdown.getItems().clear();

        // Populate with user's accounts
        for (Account acc : user.getAccounts()) {
            String displayName = acc.getClass().getSimpleName() + " - " + acc.getAccountNumber();
            senderAccountDropdown.getItems().add(displayName);
            recieverAccountDropdown.getItems().add(displayName);
        }

        // If the user has only one account, hide the dropdown and auto-select
        if (user.getAccounts().size() == 1) {
            senderAccountDropdown.setVisible(false);
            senderAccountDropdown.setManaged(false); // so layout adjusts
            recieverAccountDropdown.setVisible(false);
            recieverAccountDropdown.setManaged(false);
            System.out.println("Only one account found, skipping dropdown.");
        } else {
            senderAccountDropdown.setVisible(true);
            senderAccountDropdown.setManaged(true);
        }
    }

    @FXML
    private void handleTransfer(ActionEvent event) {
        try {
            // 1️⃣ Get sender account number
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

            Account senderAccount = loggedInUser.getAccounts().stream()
                    .filter(a -> a.getAccountNumber().equals(senderAccountNumber))
                    .findFirst()
                    .orElse(null);

            if (senderAccount == null) {
                statusLabel.setText("Sender account not found.");
                return;
            }

            // 2️⃣ Get receiver account number
            String receiverValue = recieverAccountDropdown.isVisible()
                    ? recieverAccountDropdown.getValue()
                    : receiverAccountField.getText();

            if (receiverValue == null || receiverValue.isEmpty()) {
                statusLabel.setText("Please select or enter a receiver account.");
                return;
            }

            String receiverAccountNumber = receiverValue.contains(" - ")
                    ? receiverValue.split(" - ")[1]
                    : receiverValue;

            Account receiverAccount = loggedInUser.getAccounts().stream()
                    .filter(a -> a.getAccountNumber().equals(receiverAccountNumber))
                    .findFirst()
                    .orElse(null);



            // 3️⃣ Parse and validate amount
            double amount = Double.parseDouble(senderAmount.getText());
            if (amount <= 0) {
                statusLabel.setText("Amount must be greater than zero.");
                return;
            }

            if (senderAccount.getBalance() < amount) {
                statusLabel.setText("Insufficient funds in sender account.");
                return;
            }

            // 4️⃣ Perform transfer
            if (receiverAccount == null) {
                senderAccount.withdraw(amount);
            }else {
                senderAccount.withdraw(amount);
                receiverAccount.deposit(amount);
            }
            // 5️⃣ Record transaction
            Transaction t = new Transaction(
                    UUID.randomUUID().toString(),
                    "Transfer",
                    amount,
                    senderAccount.getAccountNumber(),
                    receiverAccountNumber
            );
            loggedInUser.addTransaction(t);

            // 6️⃣ Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Transfer Successful");
            alert.setHeaderText("Transfer successful!");
            alert.setContentText(amount + " has been sent to " + receiverAccountNumber);
            alert.showAndWait();

            statusLabel.setText("Transfer Successful");

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
