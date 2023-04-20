package bsk.project.chatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ServerHandler implements Runnable {

    private PrintWriter outStream;
    private CountDownLatch latch;

    public ServerHandler(CountDownLatch latch) {
        this.latch = latch;
    }

    public PrintWriter getOutStream() {
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

            // Creating streams for communication with the client
            outStream = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            latch.countDown();

            // Receiving messages from the client in a separate thread
            Thread thread = new Thread(new MessageReceiverThreadBuilder("Client", in));
            thread.start();

            // Sending messages to the client
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String message = console.readLine();
                outStream.println(message);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
