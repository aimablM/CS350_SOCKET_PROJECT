package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ChatServer implements a multi-threaded chat server using TCP/IP connections.
 * It manages multiple client connections and broadcasts messages between clients.
 * This is the core server implementation without GUI components.
 */
public class ChatServer {
    // Server configuration
    private static final int PORT = 5000;        // Default server port
    private ServerSocket serverSocket;           // Socket for accepting client connections
    private boolean isRunning;                   // Server status flag
    private ExecutorService executorService;     // Thread pool for client handlers
    private Set<ClientHandler> clients;          // Collection of connected clients

    /**
     * Constructor initializes the server components
     * Uses CachedThreadPool for dynamic thread management and
     * synchronized HashSet for thread-safe client tracking
     */
    public ChatServer() {
        // Create thread-safe set for client handlers
        this.clients = Collections.synchronizedSet(new HashSet<>());
        // Initialize thread pool that creates new threads as needed
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Starts the server and begins accepting client connections
     * Runs in an infinite loop until server is stopped
     */
    public void start() {
        try {
            // Create server socket to accept client connections
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Server started on port " + PORT);

            // Main server loop - continuously accept new clients
            while (isRunning) {
                try {
                    // Accept new client connection
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + 
                        clientSocket.getInetAddress().getHostAddress());
                    
                    // Create and start a new client handler
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    executorService.execute(clientHandler);
                } catch (IOException e) {
                    // Only log error if server is still meant to be running
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
        }
    }

    /**
     * Stops the server and performs cleanup
     * Closes all client connections and shuts down the thread pool
     */
    public void stop() {
        isRunning = false;
        try {
            // Close all client connections
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
            
            // Shutdown the executor service
            executorService.shutdown();
            // Wait for tasks to complete, then force shutdown
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            
            // Close the server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Inner class to handle individual client connections
     * Each instance runs in its own thread and manages communication
     * with a single client
     */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;         // Client's socket connection
        private PrintWriter out;             // Output stream to client
        private BufferedReader in;           // Input stream from client
        private String clientName;           // Client's username

        /**
         * Constructor takes the client's socket connection
         */
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        /**
         * Main run method executed in separate thread
         * Handles the client's connection lifecycle
         */
        @Override
        public void run() {
            try {
                setupStreams();
                processClientMessages();
            } catch (IOException e) {
                System.err.println("Error in client handler: " + e.getMessage());
            } finally {
                close();
            }
        }

        /**
         * Sets up input/output streams and processes initial connection
         * First message from client is expected to be their username
         */
        private void setupStreams() throws IOException {
            // Initialize input/output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            // First message is client's username
            clientName = in.readLine();
            broadcastMessage("SERVER: " + clientName + " has joined the chat");
            sendClientList();
        }

        /**
         * Main message processing loop
         * Continuously reads and broadcasts client messages
         */
        private void processClientMessages() throws IOException {
            String message;
            // Read messages until client disconnects
            while ((message = in.readLine()) != null) {
                if (message.equals("/quit")) {
                    break;
                }
                broadcastMessage(clientName + ": " + message);
            }
        }

        /**
         * Broadcasts a message to all connected clients except the sender
         */
        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != this) {  // Don't send message back to sender
                        client.sendMessage(message);
                    }
                }
            }
        }

        /**
         * Sends the list of connected users to all clients
         */
        private void sendClientList() {
            StringBuilder userList = new StringBuilder("Connected users: ");
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    userList.append(client.clientName).append(", ");
                }
            }
            broadcastMessage(userList.toString());
        }

        /**
         * Sends a message to this specific client
         */
        public void sendMessage(String message) {
            out.println(message);
        }

        /**
         * Closes the client connection and performs cleanup
         */
        public void close() {
            try {
                clients.remove(this);
                if (clientName != null) {
                    broadcastMessage("SERVER: " + clientName + " has left the chat");
                }
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client handler: " + e.getMessage());
            }
        }
    }

    /**
     * Main method to start the server
     * Includes shutdown hook for graceful server shutdown
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        
        // Add shutdown hook for graceful shutdown on program termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));

        server.start();
    }
}