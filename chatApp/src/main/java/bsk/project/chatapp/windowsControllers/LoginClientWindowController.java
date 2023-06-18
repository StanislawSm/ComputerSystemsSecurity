package bsk.project.chatapp.windowsControllers;

import bsk.project.chatapp.ChatClient;
import bsk.project.chatapp.handlers.ClientHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class LoginClientWindowController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label label;

    @FXML
    protected void onLoginButtonClick() throws IOException {
        Stage stage = (Stage) passwordField.getScene().getWindow();

        //loading a scene from fxml file
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClient.class.getResource("mainWindowView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 480);
        //gathering controller Object from loaded scene
        MainWindowController controller = fxmlLoader.getController();

        //this latch will be used to synchronize two threads: one creating socket connection and other setting output stream for Main window controller
        CountDownLatch latch = new CountDownLatch(1);

        //Creating client handler with the controller gathered before and the latch
        ClientHandler clientHandler = new ClientHandler(latch, controller);
        Thread thread = new Thread(clientHandler);
        thread.start();

        //Here we have to wait for the clientHandler thread to establish a connection with other app, because we need output stream created in that thread
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //finally we have an output stream, and we can use it in the main window controller
        controller.setOutStream(clientHandler.getOutStream());

        stage.setTitle("chatApp1.0 Client");
        stage.setScene(scene);
        stage.show();
    }
}