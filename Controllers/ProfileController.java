package Controllers;

import Core.Account;
import Core.Customer;
import Core.DBConnection;
import java.sql.Connection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class ProfileController {

    private Customer loggedInUser;
    private Connection connection; // store DB connection

    @FXML
    private VBox accountsContainer;
    @FXML
    private Label firstNameField;
    @FXML
    private Label surnameField;
    @FXML
    private Label addressField;
    @FXML
    private Label numberField;

    /**
     * Initialize the controller with user info and DB connection
     */
    public void initialize(Customer user, Connection con) {
        this.loggedInUser = user;
        this.connection = con;
        accountsContainer.getChildren().clear();

        firstNameField.setText(user.getFirstName());
        surnameField.setText(user.getSurname());
        addressField.setText(user.getAddress());
        numberField.setText(user.getPhoneNumber());

        for (Account acc : user.getAccounts()) {
            AnchorPane card = new AnchorPane();
            card.setPrefSize(78, 92);
            card.getStyleClass().add("card");

            // Account type label
            Label accountTypeLabel = new Label(acc.getClass().getSimpleName());
            accountTypeLabel.setLayoutX(14);
            accountTypeLabel.setLayoutY(10);
            accountTypeLabel.getStyleClass().add("credit-card-holder");

            // Balance label
            Label balanceLabel = new Label("Balance: $" + String.format("%.2f", acc.getBalance()));
            balanceLabel.setLayoutX(14);
            balanceLabel.setLayoutY(40);
            balanceLabel.getStyleClass().add("credit-card-number");

            // Card image/logo
            ImageView cardImage = new ImageView(new Image(getClass().getResourceAsStream("/Resources/logo.png")));
            cardImage.setFitWidth(57);
            cardImage.setFitHeight(67);
            cardImage.setLayoutX(124.0);
            cardImage.setLayoutY(20);
            cardImage.setPreserveRatio(true);
            cardImage.setPickOnBounds(true);

            card.getChildren().addAll(accountTypeLabel, balanceLabel, cardImage);
            accountsContainer.getChildren().add(card);
        }
    }

    // Navigation methods
    @FXML
    private void openDashboard(ActionEvent event) {
        loadFXMLWithConnection(event, "/views/Dashboard.fxml", "Dashboard");
    }

    @FXML
    private void openTransaction(ActionEvent event) {
        loadFXMLWithConnection(event, "/views/Transaction.fxml", "Transaction");
    }

    @FXML
    private void openTransfer(ActionEvent event) {
        loadFXMLWithConnection(event, "/views/Transfer.fxml", "Transfer");
    }

    @FXML
    private void openWithdraw(ActionEvent event) {
        loadFXMLWithConnection(event, "/views/Withdraw.fxml", "Withdraw");
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

    // Generic method for loading FXML without DB connection
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
        }
    }

    // Generic method for loading FXML and passing DB connection and user
    private void loadFXMLWithConnection(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Pass logged-in user and DB connection
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).initialize(loggedInUser, connection);
            } else if (controller instanceof TransferController) {
                ((TransferController) controller).initialize(loggedInUser, connection);
            } else if (controller instanceof TransactionController) {
                ((TransactionController) controller).initialize(loggedInUser, connection);
            } else if (controller instanceof WithdrawController) {
                ((WithdrawController) controller).initialize(loggedInUser, connection);
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
