package bsk.project.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatServer extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        //loading a scene from fxml file
        FXMLLoader loginFxmlLoader = new FXMLLoader(ChatServer.class.getResource("loginServerWindowView.fxml"));
        Scene loginScene = new Scene(loginFxmlLoader.load(), 480, 480);

        stage.setTitle("chatApp1.0 Server");
        stage.setScene(loginScene);
        stage.show();
    }

    public static void main(String[] args){
        launch();
    }
}
