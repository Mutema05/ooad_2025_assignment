package Controllers;

import Core.*;
import Models.BankData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AdminController {


    @FXML private Label totalCustomersLabel;
    @FXML private Label totalAccountsLabel;


    @FXML private TextField firstNameField;
    @FXML private TextField surnameField;
    @FXML private TextField addressField;
    @FXML private TextField numberField;


    @FXML private TextField accountCustomerNumberField;
    @FXML private TextField accountTypeField; //
    @FXML private TextField accountIdField;
    @FXML private TextField accountBranchField;
    @FXML private TextField accountBalanceField;

    @FXML private TextField ChequeUserNameField;
    @FXML private TextField chequeBranchField;
    @FXML private TextField chequeBalanceField;
    @FXML private TextField employerNameField;
    @FXML private TextField employerAddressField;


    @FXML private TextField SavingsUserNameField;
    @FXML private TextField savingsBranchField;
    @FXML private TextField savingsBalanceField;


    @FXML private TextField InvestmentUserNameField;
    @FXML private TextField investBranchField;
    @FXML private TextField investBalanceField;


    public void initialize() {
        System.out.println("AdminController initialized");
        refreshTotals();
    }


    private void refreshTotals() {
        int totalCustomers = BankData.getCustomers().size();
        int totalAccounts = BankData.getCustomers().stream()
                .mapToInt(c -> c.getAccounts().size())
                .sum();

        if (totalCustomersLabel != null)
            totalCustomersLabel.setText(String.valueOf(totalCustomers));

        if (totalAccountsLabel != null)
            totalAccountsLabel.setText(String.valueOf(totalAccounts));
    }
    public void applyInterestToAllCustomers() {
        for (Customer c : BankData.getCustomers()) {
            for (Account a : c.getAccounts()) {
                if (a instanceof SavingsAccount) {
                    ((SavingsAccount) a).applyMonthlyInterest();

                } else if (a instanceof InvestmentAccount) {
                    ((InvestmentAccount) a).applyMonthlyInterest();

                }
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "Success", "Interest rate has been added to all eligable  accounts for all customers");
    }


    // --- Registration ---
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

        List<Customer> customers = BankData.getCustomers();


        for (Customer c : customers) {
            if (c.getNumber().equals(phoneNumber)) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Customer", "Customer with this phone number already exists.");
                return;
            }
        }

        String password = UUID.randomUUID().toString().substring(0, 8);
        Customer newCustomer = new Customer(firstName, surname, address, password, phoneNumber);
        customers.add(newCustomer);

        showAlert(Alert.AlertType.INFORMATION, "Success", "Customer registered successfully!\nPassword: " + password);

        firstNameField.clear();
        surnameField.clear();
        addressField.clear();
        numberField.clear();

        refreshTotals();
    }

    @FXML
    private void registerChequeAccount(ActionEvent event) {
        try {
            String username = ChequeUserNameField.getText().trim();
            String branch = chequeBranchField.getText().trim();
            String employer = employerNameField.getText().trim();
            String empAddress = employerAddressField.getText().trim();
            double balance = Double.parseDouble(chequeBalanceField.getText().trim());

            if (username.isEmpty() || branch.isEmpty() || employer.isEmpty() || empAddress.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
                return;
            }


            Customer customer = BankData.getCustomers().stream()
                    .filter(c -> c.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);

            if (customer == null) {
                showAlert(Alert.AlertType.ERROR, "Customer Not Found", "No customer with username/phone: " + username);
                return;
            }


            boolean hasCheque = customer.getAccounts().stream()
                    .anyMatch(a -> a instanceof ChequeAccount);

            if (hasCheque) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Account", "This customer already has a Cheque Account.");
                return;
            }


            String accId = UUID.randomUUID().toString().substring(0, 6);
            ChequeAccount account = new ChequeAccount(accId, branch, balance, employer, empAddress);
            customer.addAccount(account);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Cheque Account registered for " + customer.getFirstName());
            refreshTotals();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Balance must be a number.");
        }
    }


    // ------------------------------------------------------------
// Savings Account Registration
// ------------------------------------------------------------
    @FXML
    private void registerSavingsAccount(ActionEvent event) {
        try {
            String username = SavingsUserNameField.getText().trim();
            String branch = savingsBranchField.getText().trim();
            double balance = Double.parseDouble(savingsBalanceField.getText().trim());

            if (username.isEmpty() || branch.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
                return;
            }

            // Find customer by username
            Customer customer = BankData.getCustomers().stream()
                    .filter(c -> c.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);

            if (customer == null) {
                showAlert(Alert.AlertType.ERROR, "Customer Not Found", "No customer with username/phone: " + username);
                return;
            }

            // Check if customer already has a SavingsAccount
            boolean hasSavings = customer.getAccounts().stream()
                    .anyMatch(a -> a instanceof SavingsAccount);

            if (hasSavings) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Account", "This customer already has a Savings Account.");
                return;
            }

            // Create new account
            String accId = UUID.randomUUID().toString().substring(0, 6);
            SavingsAccount account = new SavingsAccount(accId, branch, balance);
            customer.addAccount(account);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Savings Account registered for " + customer.getFirstName());
            refreshTotals();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Balance must be a number.");
        }
    }


    // ------------------------------------------------------------
// Investment Account Registration
// ------------------------------------------------------------
    @FXML
    private void registerInvestmentAccount(ActionEvent event) {
        try {
            String username = InvestmentUserNameField.getText().trim();
            String branch = investBranchField.getText().trim();
            double balance = Double.parseDouble(investBalanceField.getText().trim());

            if (username.isEmpty() || branch.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
                return;
            }

            // Find customer by username
            Customer customer = BankData.getCustomers().stream()
                    .filter(c -> c.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);

            if (customer == null) {
                showAlert(Alert.AlertType.ERROR, "Customer Not Found", "No customer with username/phone: " + username);
                return;
            }

            // Check if customer already has an InvestmentAccount
            boolean hasInvestment = customer.getAccounts().stream()
                    .anyMatch(a -> a instanceof InvestmentAccount);

            if (hasInvestment) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Account", "This customer already has an Investment Account.");
                return;
            }

            // Create new account (will throw exception if deposit < 500)
            String accId = UUID.randomUUID().toString().substring(0, 6);
            InvestmentAccount account = new InvestmentAccount(accId, branch, balance);
            customer.addAccount(account);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Investment Account registered for " + customer.getFirstName());
            refreshTotals();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Balance must be a number.");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Deposit", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out?");
        alert.setHeaderText("Do you want to log out?");
        alert.setContentText("This will log you out and you will need to login again");

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
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load " + fxmlPath + "\n" + e.getMessage());
        }
    }
}
