package bsk.project.chatapp;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageReceiverThreadBuilder implements Runnable{

    private String receiverType;
    private BufferedReader in;

    public MessageReceiverThreadBuilder(String receiverType, BufferedReader in) {
        this.receiverType = receiverType;
        this.in = in;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = in.readLine();
                if (message != null) {
                    System.out.println(receiverType + ": " + message);
                }
            } catch (IOException e) {
                System.out.println("Error while receiving messages from the "+ receiverType + ": " + e.getMessage());
            }
        }
    }
}
