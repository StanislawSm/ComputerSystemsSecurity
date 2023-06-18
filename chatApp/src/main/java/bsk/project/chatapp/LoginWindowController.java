package bsk.project.chatapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.CountDownLatch;

public class LoginWindowController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label label;
    Stage stage;

    private CountDownLatch loginLatch;

    public void setStage(Stage stage){
        this.stage = stage;
    }
    public void setLoginLatch(CountDownLatch loginLatch) {
        this.loginLatch = loginLatch;
    }

    @FXML
    protected void onLoginButtonClick() throws IOException {
        //compare passwords etc.
        loginLatch.countDown();
    }

}