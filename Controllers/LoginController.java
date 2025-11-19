package Controllers;

import Core.Customer;
import Core.DBConnection;
import java.sql.Connection;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import DAO.CustomerDAO;
import DAO.CustomerDAOImpl;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if ("admin".equals(username) && "admin".equals(password)) {
            openAdmin(event); // admin login
        } else {
            Customer user = authenticate(username, password);
            if (user != null) {
                openDashboard(event, user); // customer login
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Login Failed");
                alert.setContentText("Invalid username or password. Try again or register at a Mutema Bank branch.");
                alert.showAndWait();
            }
        }
    }

    public static Customer authenticate(String username, String password) {
        Connection connection = DBConnection.getConnection(); // Get DB connection
        CustomerDAO customerDAO = new CustomerDAOImpl(connection);
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer c : customers) {
            if (c.getUsername().equals(username) && c.getPassword().equals(password)) {
                return c;
            }
        }
        return null;
    }

    private void openDashboard(ActionEvent event, Customer user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
            Parent root = loader.load();

            // Pass the user AND DB connection to Dashboard
            DashboardController dashboardController = loader.getController();
            dashboardController.initialize(user, DBConnection.getConnection());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Admin.fxml"));
            Parent root = loader.load();
            AdminController adminController = loader.getController();
            adminController.initialize();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
