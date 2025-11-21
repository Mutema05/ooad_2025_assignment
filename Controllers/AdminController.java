package Controllers;

import Core.*;
import DAO.*;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminController {

    // ===== UI Elements =====
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalAccountsLabel;

    @FXML private TextField firstNameField;
    @FXML private TextField surnameField;
    @FXML private TextField addressField;
    @FXML private TextField numberField; // Phone number field

    @FXML private TextField ChequePhoneNumberField;
    @FXML private TextField chequeBranchField;
    @FXML private TextField chequeBalanceField;
    @FXML private TextField employerNameField;
    @FXML private TextField employerAddressField;

    @FXML private TextField SavingsPhoneNumberField;
    @FXML private TextField savingsBranchField;
    @FXML private TextField savingsBalanceField;

    @FXML private TextField InvestmentPhoneNumberField;
    @FXML private TextField investBranchField;
    @FXML private TextField investBalanceField;

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> accountsColumn;

    @FXML private TextField searchField;

    // Action buttons
    @FXML private Button editSaveButton;
    @FXML private Button deleteCustomerButton;
    @FXML private Button deleteAccountButton;

    // ===== DAO and Connection =====
    private Connection connection;
    private CustomerDAO customerDAO;
    private AccountDAO accountDAO;

    private Customer selectedCustomer;
    private boolean isEditing = false;
    private FilteredList<Customer> filteredCustomers;

    // ===== Constructor =====
    public AdminController() {
        connection = DBConnection.getConnection();
        customerDAO = new CustomerDAOImpl(connection);
        accountDAO = new AccountDAOImpl(connection);
    }

    // ===== Initialization =====
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        accountsColumn.setCellValueFactory(new PropertyValueFactory<>("accountTypes"));

        loadCustomersToTable();
        setupSearch();
        setupSelectionListener();
        refreshTotals();
    }

    // ===== Refresh Totals =====
    private void refreshTotals() {
        List<Customer> customers = customerDAO.getAllCustomers();
        int totalCustomers = customers.size();
        int totalAccounts = 0;

        for (Customer c : customers) {
            totalAccounts += accountDAO.getAccountsByCustomerId(c.getCustomerId()).size();
        }

        totalCustomersLabel.setText(String.valueOf(totalCustomers));
        totalAccountsLabel.setText(String.valueOf(totalAccounts));
    }

    // ===== Apply Interest =====
    public void applyInterestToAllCustomers() {
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer c : customers) {
            List<Account> accounts = accountDAO.getAccountsByCustomerId(c.getCustomerId());
            for (Account a : accounts) {
                if (a instanceof SavingsAccount) {
                    ((SavingsAccount) a).applyMonthlyInterest();
                } else if (a instanceof InvestmentAccount) {
                    ((InvestmentAccount) a).applyMonthlyInterest();
                }
                accountDAO.update(a);
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "Success", "Interest applied to all eligible accounts.");
    }

    // ===== Register Customer =====
    @FXML
    private void registerCustomer(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String surname = surnameField.getText().trim();
        String address = addressField.getText().trim();
        String phoneNumber = numberField.getText().trim();

        if (firstName.isEmpty() || surname.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
            return;
        }

        try {
            Customer existing = ((CustomerDAOImpl) customerDAO).findByPhoneNumber(phoneNumber);
            if (existing != null) {
                showAlert(Alert.AlertType.ERROR, "Duplicate", "Customer with this phone number already exists.");
                return;
            }

            String password = UUID.randomUUID().toString().substring(0, 8);
            Customer newCustomer = new Customer(firstName, surname, address, password, phoneNumber);
            customerDAO.create(newCustomer);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer registered successfully!\nPassword: " + password);
            firstNameField.clear();
            surnameField.clear();
            addressField.clear();
            numberField.clear();
            refreshTotals();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register customer.");
            e.printStackTrace();
        }
    }

    // ===== Register Cheque Account =====
    @FXML
    private void registerChequeAccount(ActionEvent event) {
        try {
            String PhoneNumber = ChequePhoneNumberField.getText().trim();
            String branch = chequeBranchField.getText().trim();
            String employer = employerNameField.getText().trim();
            String empAddress = employerAddressField.getText().trim();
            double balance = Double.parseDouble(chequeBalanceField.getText().trim());

            if (PhoneNumber.isEmpty() || branch.isEmpty() || employer.isEmpty() || empAddress.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
                return;
            }

            Customer customer = ((CustomerDAOImpl) customerDAO).findByPhoneNumber(PhoneNumber);
            if (customer == null) {
                showAlert(Alert.AlertType.ERROR, "Customer Not Found", "No customer with phone: " + PhoneNumber);
                return;
            }

            if (((AccountDAOImpl) accountDAO).customerHasAccountType(customer.getCustomerId(), "ChequeAccount")) {
                showAlert(Alert.AlertType.ERROR, "Duplicate", "Customer already has a Cheque Account.");
                return;
            }

            ChequeAccount account = new ChequeAccount(
                    UUID.randomUUID().toString().substring(0, 6),
                    branch,
                    balance,
                    employer,
                    empAddress
            );
            account.setCustomerId(customer.getCustomerId());
            accountDAO.create(account);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Cheque Account registered for " + customer.getFirstName());
            refreshTotals();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register cheque account.");
            e.printStackTrace();
        }
    }

    // ===== Register Savings Account =====
    @FXML
    private void registerSavingsAccount(ActionEvent event) {
        try {
            String PhoneNumber = SavingsPhoneNumberField.getText().trim();
            String branch = savingsBranchField.getText().trim();
            double balance = Double.parseDouble(savingsBalanceField.getText().trim());

            if (PhoneNumber.isEmpty() || branch.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
                return;
            }

            Customer customer = ((CustomerDAOImpl) customerDAO).findByPhoneNumber(PhoneNumber);
            if (customer == null) {
                showAlert(Alert.AlertType.ERROR, "Customer Not Found", "No customer with phone: " + PhoneNumber);
                return;
            }

            if (((AccountDAOImpl) accountDAO).customerHasAccountType(customer.getCustomerId(), "SavingsAccount")) {
                showAlert(Alert.AlertType.ERROR, "Duplicate", "Customer already has a Savings Account.");
                return;
            }

            SavingsAccount account = new SavingsAccount(
                    UUID.randomUUID().toString().substring(0, 6),
                    branch,
                    balance
            );
            account.setCustomerId(customer.getCustomerId());
            accountDAO.create(account);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Savings Account registered for " + customer.getFirstName());
            refreshTotals();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register savings account.");
            e.printStackTrace();
        }
    }

    // ===== Register Investment Account =====
    @FXML
    private void registerInvestmentAccount(ActionEvent event) {
        try {
            String PhoneNumber = InvestmentPhoneNumberField.getText().trim();
            String branch = investBranchField.getText().trim();
            double balance = Double.parseDouble(investBalanceField.getText().trim());

            if (PhoneNumber.isEmpty() || branch.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
                return;
            }

            Customer customer = ((CustomerDAOImpl) customerDAO).findByPhoneNumber(PhoneNumber);
            if (customer == null) {
                showAlert(Alert.AlertType.ERROR, "Customer Not Found", "No customer with phone: " + PhoneNumber);
                return;
            }

            if (((AccountDAOImpl) accountDAO).customerHasAccountType(customer.getCustomerId(), "InvestmentAccount")) {
                showAlert(Alert.AlertType.ERROR, "Duplicate", "Customer already has an Investment Account.");
                return;
            }

            InvestmentAccount account = new InvestmentAccount(
                    UUID.randomUUID().toString().substring(0, 6),
                    branch,
                    balance
            );
            account.setCustomerId(customer.getCustomerId());
            accountDAO.create(account);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Investment Account registered for " + customer.getFirstName());
            refreshTotals();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register investment account.");
            e.printStackTrace();
        }
    }

    // ===== Load Customers =====
    private void loadCustomersToTable() {
        List<Customer> customers = customerDAO.getAllCustomers();
        filteredCustomers = new FilteredList<>(FXCollections.observableArrayList(customers), p -> true);
        customerTable.setItems(filteredCustomers);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filteredCustomers.setPredicate(customer -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return customer.getFullName().toLowerCase().contains(lower) ||
                        customer.getPhoneNumber().contains(lower);
            });
        });
    }

    private void setupSelectionListener() {
        customerTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) { // double-click row
                    Customer clickedCustomer = row.getItem();
                    openUserDetailsPage(clickedCustomer);
                }
            });
            return row;
        });
    }
    private void openUserDetailsPage(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UserDetails.fxml"));
            Parent root = loader.load();

            // Pass selected customer to UserDetailsController
            UserDetailsController controller = loader.getController();
            controller.initData(customer);

            Stage stage = (Stage) customerTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Details");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open User Details page.");
        }
    }



    private void displayCustomerDetails() {
        if (selectedCustomer == null) {
            firstNameField.clear();
            surnameField.clear();
            addressField.clear();
            numberField.clear();
            editSaveButton.setDisable(true);
            deleteCustomerButton.setDisable(true);
            deleteAccountButton.setDisable(true);
            return;
        }

        firstNameField.setText(selectedCustomer.getFirstName());
        surnameField.setText(selectedCustomer.getSurname());
        addressField.setText(selectedCustomer.getAddress());
        numberField.setText(selectedCustomer.getPhoneNumber());

        firstNameField.setEditable(false);
        surnameField.setEditable(false);
        addressField.setEditable(false);
        numberField.setEditable(false);

        editSaveButton.setText("Edit");
        isEditing = false;

        editSaveButton.setDisable(false);
        deleteCustomerButton.setDisable(false);
        deleteAccountButton.setDisable(false);
    }

    @FXML
    private void handleEditSave() {
        if (selectedCustomer == null) return;

        if (!isEditing) {
            firstNameField.setEditable(true);
            surnameField.setEditable(true);
            addressField.setEditable(true);
            numberField.setEditable(true);
            editSaveButton.setText("Save");
            isEditing = true;
        } else {
            try {
                selectedCustomer.setFirstName(firstNameField.getText().trim());
                selectedCustomer.setSurname(surnameField.getText().trim());
                selectedCustomer.setAddress(addressField.getText().trim());
                selectedCustomer.setPhoneNumber(numberField.getText().trim());

                customerDAO.update(selectedCustomer);
                displayCustomerDetails();
                loadCustomersToTable();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update customer.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteCustomer() {
        if (selectedCustomer == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "This will delete the customer and all associated accounts. Proceed?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                List<Account> accounts = accountDAO.getAccountsByCustomerId(selectedCustomer.getCustomerId());
                for (Account acc : accounts) {
                    accountDAO.delete(acc.getAccountId());
                }
                customerDAO.delete(selectedCustomer.getCustomerId());
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Customer and accounts deleted successfully.");
                loadCustomersToTable();
                displayCustomerDetails();
                refreshTotals();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete customer.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteAccount() {
        if (selectedCustomer == null) return;

        List<Account> accounts = accountDAO.getAccountsByCustomerId(selectedCustomer.getCustomerId());
        if (accounts.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Accounts", "Customer has no accounts.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(accounts.get(0).getAccountNumber(),
                accounts.stream().map(Account::getAccountNumber).collect(Collectors.toList()));
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
                    loadCustomersToTable();
                    displayCustomerDetails();
                    refreshTotals();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete account.");
                e.printStackTrace();
            }
        });
    }

    // ===== Logout =====
    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out?");
        alert.setHeaderText("Do you want to log out?");
        alert.setContentText("You will need to log in again.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            loadFXML(event, "/views/Login.fxml", "Login");
        }
    }

    private void loadFXML(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ===== Helper =====
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
