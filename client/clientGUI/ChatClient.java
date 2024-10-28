package client.clientGUI;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * ChatClient handles all the networking and message handling logic for the chat application.
 * This class manages the connection to the server, sending messages, and receiving messages.
 * It uses callbacks to communicate with the GUI layer.
 */
public class ChatClient {
    private static final int DEFAULT_PORT = 5000;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean isConnected;
    
    // Callback handlers for GUI updates
    private final Consumer<String> messageHandler;
    private final Consumer<String> errorHandler;
    private final Consumer<Boolean> connectionHandler;
    
    /**
     * Constructor for ChatClient
     * 
     * @param messageHandler Callback for handling incoming messages
     * @param errorHandler Callback for handling errors
     * @param connectionHandler Callback for handling connection state changes
     */
    public ChatClient(Consumer<String> messageHandler, 
                     Consumer<String> errorHandler,
                     Consumer<Boolean> connectionHandler) {
        this.messageHandler = messageHandler;
        this.errorHandler = errorHandler;
        this.connectionHandler = connectionHandler;
        this.isConnected = false;
    }
    
    /**
     * Attempts to connect to the chat server
     * 
     * @param serverIP The IP address of the server
     * @param username The username for the chat session
     * @throws IOException If connection fails
     */
    public void connect(String serverIP, String username) throws IOException {
        if (isConnected) {
            return;
        }
        
        try {
            this.username = username;
            socket = new Socket(serverIP, DEFAULT_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send username to server as first message
            out.println(username);
            
            // Start message listener in separate thread
            new Thread(new MessageListener()).start();
            
            isConnected = true;
            connectionHandler.accept(true);
            
        } catch (IOException e) {
            disconnect();
            throw e;
        }
    }
    
    /**
     * Disconnects from the chat server
     */
    public void disconnect() {
        if (!isConnected) {
            return;
        }
        
        try {
            isConnected = false;
            
            if (out != null) {
                out.println("/quit");
            }
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            if (in != null) {
                in.close();
            }
            
        } catch (IOException e) {
            errorHandler.accept("Error during disconnect: " + e.getMessage());
        } finally {
            socket = null;
            in = null;
            out = null;
            connectionHandler.accept(false);
        }
    }
    
    /**
     * Sends a message to the server
     * 
     * @param message The message to send
     * @return true if message was sent successfully, false otherwise
     */
    public boolean sendMessage(String message) {
        if (!isConnected || message == null || message.trim().isEmpty()) {
            return false;
        }
        
        out.println(message);
        return !out.checkError();
    }
    
    /**
     * @return true if connected to server, false otherwise
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * @return the current username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Inner class that handles receiving messages from the server
     */
    private class MessageListener implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while (isConnected && (message = in.readLine()) != null) {
                    messageHandler.accept(message);
                }
            } catch (IOException e) {
                if (isConnected) {
                    errorHandler.accept("Lost connection to server: " + e.getMessage());
                    disconnect();
                }
            }
        }
    }
}