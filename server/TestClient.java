package server;
import java.io.*;
import java.net.*;

public class TestClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader consoleReader;

    public TestClient() {
        consoleReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void start() {
        try {
            // Connect to server
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.print("Enter your name: ");
            String name = consoleReader.readLine();
            out.println(name);

            // Start message receiver thread
            new Thread(this::receiveMessages).start();

            // Main loop for sending messages
            String message;
            while ((message = consoleReader.readLine()) != null) {
                out.println(message);
                if (message.equals("/quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        }
    }

    private void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new TestClient().start();
    }
}