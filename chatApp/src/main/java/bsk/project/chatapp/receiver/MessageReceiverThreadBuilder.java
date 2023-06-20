package bsk.project.chatapp.receiver;

import bsk.project.chatapp.message.Message;
import bsk.project.chatapp.windowsControllers.MainWindowController;

import java.io.IOException;
import java.io.ObjectInputStream;

public class MessageReceiverThreadBuilder implements Runnable {

    private final String receiverType;
    private final ObjectInputStream in;
    private final MainWindowController mainWindowController;

    public MessageReceiverThreadBuilder(String receiverType, ObjectInputStream in, MainWindowController mainWindowController) {
        this.receiverType = receiverType;
        this.in = in;
        this.mainWindowController = mainWindowController;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message message = (Message) in.readObject();
                if (message != null) {
                    switch (message.getMessageType()) {
                        case TEXT -> mainWindowController.onMessageReceived(message);
                        case FILE_READY -> mainWindowController.onFileReadyMessageReceived(message, in);
                        case ENCRYPTED_SECRET -> mainWindowController.onEncryptedSessionKeyReceived(message);
                        case CYPHER_MODE_CHANGED -> mainWindowController.onCipherModeChange(message);
                        default -> {
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error while receiving messages from the " + receiverType + ": " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
