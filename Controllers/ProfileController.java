package Controllers;

import Core.Account;
import Core.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class ProfileController {

    private Customer loggedInUser;
    private Connection connection;

    @FXML private VBox accountsContainer;
    @FXML private Label messageLabel;

    @FXML private TextField firstNameField;
    @FXML private TextField surnameField;
    @FXML private TextField addressField;
    @FXML private TextField numberField;

    @FXML private Button editButton;

    private boolean isEditing = false;

    public void initialize(Customer user, Connection con) {
        this.loggedInUser = user;
        this.connection = con;

        // Clear container
        accountsContainer.getChildren().clear();

        // Initialize profile fields
        firstNameField.setText(user.getFirstName());
        surnameField.setText(user.getSurname());
        addressField.setText(user.getAddress());
        numberField.setText(user.getPhoneNumber());

        // Disable editing initially
        setEditableFields(false);

        // Display accounts
        displayAccounts();
    }

    // -------------------------
    //  DISPLAY ACCOUNTS
    // -------------------------
    private void displayAccounts() {
        if (loggedInUser.getAccounts().isEmpty()) {
            messageLabel.setText("You currently have no active accounts.");
            return;
        }

        for (Account acc : loggedInUser.getAccounts()) {
            AnchorPane card = new AnchorPane();
            card.setPrefSize(160, 100);
            card.getStyleClass().add("card");

            Label typeLabel = new Label(acc.getClass().getSimpleName() + " Account");
            typeLabel.setLayoutX(10);
            typeLabel.setLayoutY(10);
            typeLabel.getStyleClass().add("card-title");

            Label balanceLabel = new Label("Balance: $" + String.format("%.2f", acc.getBalance()));
            balanceLabel.setLayoutX(10);
            balanceLabel.setLayoutY(40);
            balanceLabel.getStyleClass().add("card-balance");

            ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/Resources/logo.png")));
            img.setFitWidth(60);
            img.setFitHeight(60);
            img.setLayoutX(90);
            img.setLayoutY(20);
            img.setPreserveRatio(true);

            card.getChildren().addAll(typeLabel, balanceLabel, img);
            accountsContainer.getChildren().add(card);
        }
    }

    // -------------------------
    //  ENABLE/DISABLE FIELDS
    // -------------------------
    private void setEditableFields(boolean editable) {
        firstNameField.setEditable(editable);
        surnameField.setEditable(editable);
        addressField.setEditable(editable);
        numberField.setEditable(editable);
    }

    // -------------------------
    //  EDIT / SAVE PROFILE
    // -------------------------
    @FXML
    private void handleEdit() {
        if (!isEditing) {
            setEditableFields(true);
            editButton.setText("Save");
            isEditing = true;
            messageLabel.setText("You can now edit your profile fields.");
        } else {
            saveProfile();
        }
    }

    private void saveProfile() {
        try {
            String sql = "UPDATE Customer SET first_name=?, surname=?, address=?, phone_number=? WHERE customer_id=?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, firstNameField.getText());
            stmt.setString(2, surnameField.getText());
            stmt.setString(3, addressField.getText());
            stmt.setString(4, numberField.getText());
            stmt.setInt(5, loggedInUser.getCustomerId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                messageLabel.setText("Profile updated successfully ✅");

                // Update session user
                loggedInUser.setFirstName(firstNameField.getText());
                loggedInUser.setSurname(surnameField.getText());
                loggedInUser.setAddress(addressField.getText());
                loggedInUser.setPhoneNumber(numberField.getText());
            } else {
                messageLabel.setText("No changes were made ❌");
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error: " + e.getMessage());
        } finally {
            setEditableFields(false);
            editButton.setText("Edit");
            isEditing = false;
        }
    }

    // -------------------------
    //  NAVIGATION
    // -------------------------
    @FXML private void openDashboard(ActionEvent event) { loadFXMLWithConnection(event, "/views/Dashboard.fxml", "Dashboard"); }
    @FXML private void openTransaction(ActionEvent event) { loadFXMLWithConnection(event, "/views/Transaction.fxml", "Transaction"); }
    @FXML private void openTransfer(ActionEvent event) { loadFXMLWithConnection(event, "/views/Transfer.fxml", "Transfer"); }
    @FXML private void openWithdraw(ActionEvent event) { loadFXMLWithConnection(event, "/views/Withdraw.fxml", "Withdraw"); }
    @FXML private void openProfile(ActionEvent event) { loadFXMLWithConnection(event, "/views/Profile.fxml", "Profile"); }

    // -------------------------
    //  LOGOUT
    // -------------------------
    @FXML
    private void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "You will need to log in again.", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Log Out");
        alert.setHeaderText("Do you want to log out?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            loadFXML(event, "/views/Login.fxml", "Login");
        }
    }

    // -------------------------
    //  LOAD FXML
    // -------------------------
    private void loadFXML(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showError("Failed to load page: " + title);
            e.printStackTrace();
        }
    }

    private void loadFXMLWithConnection(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController) ((DashboardController) controller).initialize(loggedInUser, connection);
            else if (controller instanceof TransferController) ((TransferController) controller).initialize(loggedInUser, connection);
            else if (controller instanceof TransactionController) ((TransactionController) controller).initialize(loggedInUser, connection);
            else if (controller instanceof WithdrawController) ((WithdrawController) controller).initialize(loggedInUser, connection);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            showError("Failed to load page: " + title);
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
