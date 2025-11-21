package Controllers;

import Core.Account;
import Core.Customer;
import DAO.AccountDAO;
import DAO.CustomerDAO;
import Core.DBConnection;
import DAO.CustomerDAOImpl;
import DAO.AccountDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class UserDetailsController {

    @FXML private TextField firstNameField;
    @FXML private TextField surnameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField passwordField;

    @FXML private Button editSaveButton;
    @FXML private Button deleteAccountButton;
    @FXML private Button deleteUserButton;
    @FXML private TableView<Account> accountTable;
    @FXML private TableColumn<Account, String> accountNumberColumn;
    @FXML private TableColumn<Account, String> accountTypeColumn;
    @FXML private TableColumn<Account, Double> balanceColumn;

    private Customer selectedCustomer;
    private boolean isEditing = false;

    private Connection connection;
    private CustomerDAO customerDAO;
    private AccountDAO accountDAO;

    public void initData(Customer customer) {
        this.selectedCustomer = customer;
        connection = DBConnection.getConnection();
        customerDAO = new CustomerDAOImpl(connection);
        accountDAO = new AccountDAOImpl(connection);

        displayCustomerDetails();
        loadAccounts();
    }

    private void displayCustomerDetails() {
        if (selectedCustomer == null) return;

        firstNameField.setText(selectedCustomer.getFirstName());
        surnameField.setText(selectedCustomer.getSurname());
        addressField.setText(selectedCustomer.getAddress());
        phoneField.setText(selectedCustomer.getPhoneNumber());
        passwordField.setText(selectedCustomer.getPassword());

        firstNameField.setEditable(false);
        surnameField.setEditable(false);
        addressField.setEditable(false);
        phoneField.setEditable(false);
        passwordField.setEditable(false);

        editSaveButton.setText("Edit");
        isEditing = false;
    }

    @FXML
    private void handleEditSave() {
        if (!isEditing) {
            firstNameField.setEditable(true);
            surnameField.setEditable(true);
            addressField.setEditable(true);
            phoneField.setEditable(true);
            passwordField.setEditable(true);
            editSaveButton.setText("Save");
            isEditing = true;
        } else {
            selectedCustomer.setFirstName(firstNameField.getText().trim());
            selectedCustomer.setSurname(surnameField.getText().trim());
            selectedCustomer.setAddress(addressField.getText().trim());
            selectedCustomer.setPhoneNumber(phoneField.getText().trim());
            selectedCustomer.setPassword(passwordField.getText().trim());

            try {
                customerDAO.update(selectedCustomer);
                displayCustomerDetails();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update customer.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteUser() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "This will delete the user and all accounts. Proceed?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                List<Account> accounts = accountDAO.getAccountsByCustomerId(selectedCustomer.getCustomerId());
                for (Account acc : accounts) accountDAO.delete(acc.getAccountId());
                customerDAO.delete(selectedCustomer.getCustomerId());
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "User and accounts deleted successfully.");
                handleBack();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteAccount() {
        try {
            List<Account> accounts = accountDAO.getAccountsByCustomerId(selectedCustomer.getCustomerId());
            if (accounts.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Accounts", "User has no accounts.");
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(accounts.get(0).getAccountNumber(),
                    accounts.stream().map(Account::getAccountNumber).toList());
            dialog.setTitle("Delete Account");
            dialog.setHeaderText("Select an account to delete");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(accNum -> {
                try {
                    Account accToDelete = accounts.stream()
                            .filter(a -> a.getAccountNumber().equals(accNum))
                            .findFirst()
                            .orElse(null);
                    if (accToDelete != null) {
                        accountDAO.delete(accToDelete.getAccountId());
                        showAlert(Alert.AlertType.INFORMATION, "Deleted", "Account deleted successfully.");
                        loadAccounts();
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete account.");
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Admin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadAccounts() {
        List<Account> accounts = accountDAO.getAccountsByCustomerId(selectedCustomer.getCustomerId());
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        accountTypeColumn.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        // Format balance nicely
        balanceColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                setText(empty || balance == null ? null : String.format("BWP %.2f", balance));
            }
        });

        accountTable.setItems(FXCollections.observableArrayList(accounts));
        deleteAccountButton.setDisable(accounts.isEmpty());
    }
}
