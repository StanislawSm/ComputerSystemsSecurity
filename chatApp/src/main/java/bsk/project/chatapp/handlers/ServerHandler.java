package bsk.project.chatapp.handlers;

import bsk.project.chatapp.message.Message;
import bsk.project.chatapp.receiver.MessageReceiverThreadBuilder;
import bsk.project.chatapp.windowsControllers.MainWindowController;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ServerHandler implements Runnable {

    private ObjectOutputStream outStream;
    private CountDownLatch latch;
    private MainWindowController mainWindowController;


    public ServerHandler(CountDownLatch latch, MainWindowController mainWindowController) {
        this.latch = latch;
        this.mainWindowController = mainWindowController;
    }

    public ObjectOutputStream getOutStream() {
        return outStream;
    }

    @Override
    public void run(){
        try {
            // Creating the server
            ServerSocket serverSocket = new ServerSocket(1234);
            // Waiting for a client to connect
            System.out.println("Waiting for a client to connect...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connected with the client!");

            // Creating streams for communication with the server
            OutputStream outputStream = clientSocket.getOutputStream();
            this.outStream = new ObjectOutputStream(outputStream);
            InputStream inputStream = clientSocket.getInputStream();
            ObjectInputStream in = new ObjectInputStream(inputStream);

            //in that line in and output streams are ready, so we can release stopped thread
            latch.countDown();

            // Receiving messages from the client in a separate thread
            Thread thread = new Thread(new MessageReceiverThreadBuilder("Client", in, mainWindowController));
            thread.start();

            // Sending messages to the client
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String message = console.readLine();
                // TODO MS
                //  outStream.writeObject(new Message(message));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
