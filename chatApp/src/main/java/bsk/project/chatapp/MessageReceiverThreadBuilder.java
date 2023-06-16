package bsk.project.chatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;

public class MessageReceiverThreadBuilder implements Runnable{

    private String receiverType;
    private ObjectInputStream in;
    private MainWindowController mainWindowController;

    public MessageReceiverThreadBuilder(String receiverType, ObjectInputStream in, MainWindowController mainWindowController) {
        this.receiverType = receiverType;
        this.in = in;
        this.mainWindowController = mainWindowController;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message message = (Message)in.readObject();
                if (message != null) {
                    System.out.println(receiverType + ": " + message);
                    mainWindowController.onMessageReceived(message.getText());
                }
            } catch (IOException e) {
                System.out.println("Error while receiving messages from the "+ receiverType + ": " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
