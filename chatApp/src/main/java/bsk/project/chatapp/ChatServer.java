package bsk.project.chatapp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatServer extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        CountDownLatch latch = new CountDownLatch(1);

        ServerHandler serverHandler = new ServerHandler(latch);
        Thread thread = new Thread(serverHandler);
        thread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("mainWindowView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 480);
        MainWindowController controller = fxmlLoader.getController();

        controller.setOutStream(serverHandler.getOutStream());

        stage.setTitle("chatApp1.0 Server");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args){
        launch();
    }
}
