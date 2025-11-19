package Controllers;

import Core.*;
import DAO.*;


import javafx.collections.FXCollections;
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

public class AdminController {

    // ===== UI Elements =====
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalAccountsLabel;

    @FXML private TextField firstNameField;
    @FXML private TextField surnameField;
    @FXML private TextField addressField;
    @FXML private TextField numberField;

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

    // ===== DAO and Connection =====
    private Connection connection;
    private CustomerDAO customerDAO;
    private AccountDAO accountDAO;

    // ===== Constructor =====
    public AdminController() {
        connection = DBConnection.getConnection();
        customerDAO = new CustomerDAOImpl(connection);
        accountDAO = new AccountDAOImpl(connection);
    }

    // ===== Initialization =====
    public void initialize() {
        System.out.println("AdminController initialized");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        accountsColumn.setCellValueFactory(new PropertyValueFactory<>("accountTypes"));

        loadCustomersToTable();
        refreshTotals();
    }
    private void loadCustomersToTable() {
        List<Customer> customers = customerDAO.getAllCustomers();
        customerTable.setItems(FXCollections.observableArrayList(customers));
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


    @FXML
    private List<Customer> getCustomer() {
        return customerDAO.getAllCustomers();
    }

    // ===== Helper =====
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
