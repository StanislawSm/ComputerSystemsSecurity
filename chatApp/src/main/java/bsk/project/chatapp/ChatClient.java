package bsk.project.chatapp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatClient extends Application{

    @Override
    public void start(Stage stage) throws IOException {

        ClientHandler clientHandler = new ClientHandler();
        Thread thread = new Thread(clientHandler);
        thread.start();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("mainWindowView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 480);
        MainWindowController controller = fxmlLoader.getController();
        controller.setOutStream(clientHandler.getOutStream());

        stage.setTitle("chatApp1.0 Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args){
        launch();
    }
}
