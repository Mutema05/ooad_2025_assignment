package Controllers;

import Core.Account;
import Core.Customer;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ProfileController {

    @FXML
    private Customer loggedInUser;
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


    public void initialize(Customer user){
        this.loggedInUser = user;
        accountsContainer.getChildren().clear();
        firstNameField.setText( user.getFirstName() );
        surnameField.setText( user.getSurname() );
        addressField.setText( user.getAddress() );
        numberField.setText( user.getNumber() );
        for (Account acc : user.getAccounts()) {
            // Create a new AnchorPane as a card
            AnchorPane card = new AnchorPane();
            card.setPrefSize(78, 92);
            card.getStyleClass().add("card"); // same style as your FXML

            // Account Type Label
            Label balanceLabel = new Label("Balance: $" + String.format("%.2f", acc.getBalance()));
            balanceLabel.setLayoutX(14);
            balanceLabel.setLayoutY(40);
            balanceLabel.getStyleClass().add("credit-card-number");


            Label account_type_text = new Label(acc.getClass().getSimpleName());
            account_type_text.setLayoutX(14);
            account_type_text.setLayoutY(10);
            account_type_text.getStyleClass().add("credit-card-holder");

            // Create an ImageView for the card
            ImageView cardImage = new ImageView();
            cardImage.setFitWidth(57);   // adjust size to fit your design
            cardImage.setFitHeight(67);
            cardImage.setLayoutX(124.0);    // position inside card
            cardImage.setLayoutY(20);
            cardImage.setPreserveRatio(true);
            cardImage.setPickOnBounds(true);
            cardImage.setImage(new Image(getClass().getResourceAsStream("/Resources/logo.png"))); // replace with your image path

            // Add labels and image to card
            card.getChildren().addAll(balanceLabel,account_type_text, cardImage);

            // Add card to container
            accountsContainer.getChildren().add(card);



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
