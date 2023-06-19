package bsk.project.chatapp.receiver;

import bsk.project.chatapp.message.Message;
import bsk.project.chatapp.windowsControllers.MainWindowController;

import java.io.FileOutputStream;
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
                    switch (message.getMessageType()){
                        case TEXT:
                            System.out.println(receiverType + ": " + message);
                            mainWindowController.onMessageReceived(message.getText());
                            break;
                        case FILE_READY:
                            System.out.println(receiverType + ": file ready");
                            receiveFile("./" + message.getText());
                            break;
                        case ENCRYPTED_SECRET:
                            System.out.println("Received encrypted secret: " + message.getText());
                            var encryptedSessionKey = message.getText();
                            // var decryptedSessionKey =
                            // mainWindowController.setSessionKey(decryptedSessionKey);
                            break;
                        default:
                            break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Error while receiving messages from the "+ receiverType + ": " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void receiveFile(String fileName) throws Exception{
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long size = in.readLong();     // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read up to file size
        }
        fileOutputStream.close();
    }
}
