package server;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService executorService;
    private Set<ClientHandler> clients;

    public ChatServer() {
        this.clients = Collections.synchronizedSet(new HashSet<>());
        // Create a thread pool that can handle multiple clients
        this.executorService = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Server started on port " + PORT);

            // Main server loop
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                    
                    // Create and start a new client handler
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    executorService.execute(clientHandler);
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
        }
    }

    public void stop() {
        isRunning = false;
        try {
            // Close all client connections
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
            
            // Shutdown the executor and server socket
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    // Handler for each client connection
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

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

        private void setupStreams() throws IOException {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            // Get client's name
            clientName = in.readLine();
            broadcastMessage("SERVER: " + clientName + " has joined the chat");
            sendClientList();
        }

        private void processClientMessages() throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equals("/quit")) {
                    break;
                }
                broadcastMessage(clientName + ": " + message);
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != this) {  // Don't send message back to sender
                        client.sendMessage(message);
                    }
                }
            }
        }

        private void sendClientList() {
            StringBuilder userList = new StringBuilder("Connected users: ");
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    userList.append(client.clientName).append(", ");
                }
            }
            broadcastMessage(userList.toString());
        }

        public void sendMessage(String message) {
            out.println(message);
        }

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

    // Main method to start the server
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));

        server.start();
    }
}