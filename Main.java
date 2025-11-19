import Core.Account;
import Core.SavingsAccount;
import Core.InvestmentAccount;
import Core.Customer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;




public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("views/Login.fxml"));

        // Create a Scene with the loaded FXML
        Scene scene = new Scene(loader.load());

        // Set scene and show the stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Mutema Bank");
        primaryStage.show();
    }
    /*updates
    * whenthe is no account in the customer the transfer page and profile does not open
    * log out in some pages does not work
    * in admin view ,able to search customers names or surname or number
    * */
    public static void main(String[] args) {
        launch(args);
    }
}