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
        //loading a scene from fxml file
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("mainWindowView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 480);
        //gathering controller Object from loaded scene
        MainWindowController controller = fxmlLoader.getController();

        //this latch will be used to synchronize two threads: one creating socket connection and other setting output stream for Main window controller
        CountDownLatch latch = new CountDownLatch(1);

        //Creating client handler with the controller gathered before and the latch
        ServerHandler serverHandler = new ServerHandler(latch, controller);
        Thread thread = new Thread(serverHandler);
        thread.start();

        //Here we have to wait for the clientHandler thread to establish a connection with other app, because we need output stream created in that thread
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //finally we have an output stream and we can use it in the main window controller
        controller.setOutStream(serverHandler.getOutStream());

        stage.setTitle("chatApp1.0 Server");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args){
        launch();
    }
}
