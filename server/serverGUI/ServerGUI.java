package server.serverGUI;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ServerGUI provides a graphical interface for managing a chat server.
 * This class handles both the GUI components and the server networking functionality.
 * It allows monitoring of connected clients, server status, and message logs.
 */
public class ServerGUI extends JFrame {
    // Default port for the chat server
    private static final int DEFAULT_PORT = 5000;
    
    // Core server components
    private ServerSocket serverSocket;        // Handles incoming client connections
    private boolean isRunning;                // Server status flag
    private ExecutorService executorService;  // Thread pool for client handlers
    private Set<ClientHandler> clients;       // Set of connected clients
    
    // GUI Components
    private JLabel statusLabel;               // Displays server status (running/stopped)
    private JPanel clientListPanel;           // Shows list of connected clients
    private JTextArea logArea;                // Displays server events and messages
    private JTextField serverIPField;         // Shows server's IP address
    private JTextField portField;             // Shows/allows port configuration
    private JButton startStopButton;          // Controls server start/stop
    
    /**
     * Constructor initializes the server GUI and core components
     */
    public ServerGUI() {
        super("Socket Chat Server");
        // Initialize synchronized set for thread-safe client management
        clients = Collections.synchronizedSet(new HashSet<>());
        // Create thread pool that grows/shrinks based on demand
        executorService = Executors.newCachedThreadPool();
        
        setLayout(new BorderLayout());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        initComponents();     // Setup GUI components
        setupActions();       // Setup event handlers
        setVisible(true);
    }
    
    /**
     * Initializes and arranges all GUI components
     */
    private void initComponents() {
        // Server Status Panel - Shows current server state and control button
        JPanel serverStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverStatusPanel.setBorder(BorderFactory.createTitledBorder("Server Status"));
        statusLabel = new JLabel("Server is stopped");
        startStopButton = new JButton("Start Server");
        serverStatusPanel.add(statusLabel);
        serverStatusPanel.add(startStopButton);
        
        // Client List Panel - Shows connected clients
        clientListPanel = new JPanel();
        clientListPanel.setBorder(BorderFactory.createTitledBorder("Client List"));
        clientListPanel.setLayout(new BoxLayout(clientListPanel, BoxLayout.Y_AXIS));
        clientListPanel.setPreferredSize(new Dimension(200, 0));
        
        // Log Panel - Shows server events and message history
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Logs"));
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Configuration Panel - Shows server IP and port settings
        JPanel configPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
        
        // Get and display local IP address
        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();
            serverIPField = new JTextField(localIP);
        } catch (UnknownHostException e) {
            serverIPField = new JTextField("127.0.0.1");
        }
        serverIPField.setEditable(false);
        
        portField = new JTextField(String.valueOf(DEFAULT_PORT));
        
        configPanel.add(new JLabel("Server IP:"));
        configPanel.add(serverIPField);
        configPanel.add(new JLabel("Port:"));
        configPanel.add(portField);
        
        // Arrange panels in the frame
        add(serverStatusPanel, BorderLayout.NORTH);
        add(clientListPanel, BorderLayout.WEST);
        add(logPanel, BorderLayout.CENTER);
        add(configPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up event handlers for GUI components
     */
    private void setupActions() {
        // Toggle server start/stop when button is clicked
        startStopButton.addActionListener(e -> {
            if (!isRunning) {
                startServer();
            } else {
                stopServer();
            }
        });
        
        // Ensure clean shutdown when window is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopServer();
            }
        });
    }
    
    /**
     * Starts the server and begins accepting client connections
     */
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            serverSocket = new ServerSocket(port);
            isRunning = true;
            
            log("Server started on port " + port);
            statusLabel.setText("Server is running");
            startStopButton.setText("Stop Server");
            portField.setEnabled(false);
            
            // Accept client connections in a separate thread
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String clientIP = clientSocket.getInetAddress().getHostAddress();
                        log("New client connected: " + clientIP);
                        
                        // Create and start new client handler
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clients.add(clientHandler);
                        executorService.execute(clientHandler);
                    } catch (IOException e) {
                        if (isRunning) {
                            log("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }
            }).start();
            
        } catch (NumberFormatException e) {
            log("Invalid port number");
        } catch (IOException e) {
            log("Could not start server: " + e.getMessage());
        }
    }
    
    /**
     * Stops the server and closes all client connections
     */
    private void stopServer() {
        isRunning = false;
        try {
            // Close all client connections
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
            updateClientList();
            
            // Shutdown thread pool
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            
            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            log("Server stopped");
            statusLabel.setText("Server is stopped");
            startStopButton.setText("Start Server");
            portField.setEnabled(true);
            
        } catch (IOException | InterruptedException e) {
            log("Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Updates the GUI client list panel
     */
    private void updateClientList() {
        SwingUtilities.invokeLater(() -> {
            clientListPanel.removeAll();
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    clientListPanel.add(new JLabel(client.getClientInfo()));
                }
            }
            clientListPanel.revalidate();
            clientListPanel.repaint();
        });
    }
    
    /**
     * Adds a message to the log area
     */
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Inner class that handles individual client connections
     */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;          // Client's socket connection
        private PrintWriter out;              // Output stream to client
        private BufferedReader in;            // Input stream from client
        private String clientName;            // Client's username
        private String clientIP;              // Client's IP address
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.clientIP = socket.getInetAddress().getHostAddress();
        }
        
        public String getClientInfo() {
            return clientName + " (" + clientIP + ")";
        }
        
        @Override
        public void run() {
            try {
                setupStreams();
                processClientMessages();
            } catch (IOException e) {
                log("Error in client handler: " + e.getMessage());
            } finally {
                close();
            }
        }
        
        /**
         * Sets up input/output streams and processes initial client connection
         */
        private void setupStreams() throws IOException {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            // First message from client is their username
            clientName = in.readLine();
            String joinMessage = "SERVER: " + clientName + " has joined the chat";
            broadcastMessage(joinMessage);
            updateClientList();
            sendClientList();
        }
        
        /**
         * Main message processing loop
         */
        private void processClientMessages() throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equals("/quit")) {
                    break;
                }
                String fullMessage = clientName + ": " + message;
                broadcastMessage(fullMessage);
                log(fullMessage);
            }
        }
        
        /**
         * Sends a message to all connected clients except the sender
         */
        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != this) {
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
         * Sends a message to this client
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
                updateClientList();
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                log("Error closing client handler: " + e.getMessage());
            }
        }
    }
    
    /**
     * Main method to start the server application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}