package bsk.project.chatapp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ClientHandler implements Runnable{

    private ObjectOutputStream outStream;
    private MainWindowController mainWindowController;
    private CountDownLatch latch;
    public ClientHandler(CountDownLatch latch, MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
        this.latch = latch;
    }

    public ObjectOutputStream  getOutStream() {
        return outStream;
    }

    @Override
    public void run(){
        try {
            // Creating a connection to the server
            String serverAddress = "localhost"; // Server address
            int serverPort = 1234; // Server port
            Socket socket = new Socket(serverAddress, serverPort);

            // Creating streams for communication with the server
            OutputStream outputStream = socket.getOutputStream();
            this.outStream = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream in = new ObjectInputStream(inputStream);

            //in that line in and output streams are ready, so we can release stopped thread
            latch.countDown();

            // Receiving messages from the server in a separate thread
            Thread thread = new Thread(new MessageReceiverThreadBuilder("Server", in, mainWindowController));
            thread.start();

            // Sending messages to the server
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String message = console.readLine();
                outStream.writeObject(new Message(message));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
