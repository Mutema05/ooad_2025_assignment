package Controllers;

import Core.Account;
import Core.Customer;
import Core.Transaction;
import DAO.AccountDAO;
import DAO.AccountDAOImpl;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.util.Optional;

public class DashboardController {

    private Customer loggedInUser;
    private Connection connection;

    @FXML private Label userLabel;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> idColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> senderColumn;
    @FXML private TableColumn<Transaction, String> receiverColumn;
    @FXML private HBox accountsContainer;

    public void initialize(Customer user, Connection con) {
        this.loggedInUser = user;
        this.connection = con;

        userLabel.setText("Welcome, " + user.getFullName() + "!");
        accountsContainer.getChildren().clear();

        for (Account acc : user.getAccounts()) {
            AnchorPane card = new AnchorPane();
            card.setPrefSize(78, 92);
            card.getStyleClass().add("card");

            Label accountTypeLabel = new Label(acc.getClass().getSimpleName());
            accountTypeLabel.setLayoutX(14);
            accountTypeLabel.setLayoutY(10);
            accountTypeLabel.getStyleClass().add("credit-card-holder");

            ImageView cardImage = new ImageView(new Image(getClass().getResourceAsStream("/Resources/logo.png")));
            cardImage.setFitWidth(57);
            cardImage.setFitHeight(67);
            cardImage.setLayoutX(124.0);
            cardImage.setLayoutY(20);
            cardImage.setPreserveRatio(true);

            card.getChildren().addAll(accountTypeLabel, cardImage);
            accountsContainer.getChildren().add(card);
        }

        // Transaction table setup
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getTransactionId())));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionType()));
        amountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        senderColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getAccountId())));
        receiverColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getTargetAccountId() != null ? String.valueOf(cellData.getValue().getTargetAccountId()) : ""
        ));

        transactionTable.getItems().setAll(loggedInUser.getTransactions());
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title, Object controllerUser, Connection con) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Pass user + connection if controller has initialize(Customer, Connection)
            Object controller = loader.getController();
            try {
                controller.getClass().getMethod("initialize", Customer.class, Connection.class)
                        .invoke(controller, controllerUser, con);
            } catch (NoSuchMethodException nsme) {
                // fallback: try initialize(Customer)
                controller.getClass().getMethod("initialize", Customer.class)
                        .invoke(controller, controllerUser);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void openDashboard(ActionEvent event) { switchScene(event, "/views/Dashboard.fxml", "Dashboard", loggedInUser, connection); }
    @FXML private void openTransaction(ActionEvent event) { switchScene(event, "/views/Transaction.fxml", "Transaction", loggedInUser, connection); }
    @FXML private void openCustomer(ActionEvent event) { switchScene(event, "/views/Profile.fxml", "Profile", loggedInUser, connection); }
    @FXML private void openTransfer(ActionEvent event) { switchScene(event, "/views/Transfer.fxml", "Transfer", loggedInUser, connection); }
    @FXML private void openWithdraw(ActionEvent event) { switchScene(event, "/views/Withdraw.fxml", "Withdraw", loggedInUser, connection); }

    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out?");
        alert.setHeaderText("Do you want to log out?");
        alert.setContentText("This will log you out and you will need to login again");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            switchScene(event, "/views/Login.fxml", "Login", null, null);
        }
    }
}
