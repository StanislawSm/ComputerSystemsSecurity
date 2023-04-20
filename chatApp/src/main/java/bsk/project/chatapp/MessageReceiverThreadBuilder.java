package bsk.project.chatapp;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageReceiverThreadBuilder implements Runnable{

    private String receiverType;
    private BufferedReader in;
    private MainWindowController mainWindowController;

    public MessageReceiverThreadBuilder(String receiverType, BufferedReader in, MainWindowController mainWindowController) {
        this.receiverType = receiverType;
        this.in = in;
        this.mainWindowController = mainWindowController;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = in.readLine();
                if (message != null) {
                    System.out.println(receiverType + ": " + message);
                    mainWindowController.onMessageReceived(message);
                }
            } catch (IOException e) {
                System.out.println("Error while receiving messages from the "+ receiverType + ": " + e.getMessage());
            }
        }
    }
}
