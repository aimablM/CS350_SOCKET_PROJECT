package server.serverGUI;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerGUI extends JFrame {
    private static final int DEFAULT_PORT = 5000;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService executorService;
    private Set<ClientHandler> clients;
    
    // GUI Components
    private JLabel statusLabel;
    private JPanel clientListPanel;
    private JTextArea logArea;
    private JTextField serverIPField;
    private JTextField portField;
    private JButton startStopButton;
    
    public ServerGUI() {
        super("Server GUI Development");
        clients = Collections.synchronizedSet(new HashSet<>());
        executorService = Executors.newCachedThreadPool();
        
        setLayout(new BorderLayout());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        initComponents();
        setupActions();
        setVisible(true);
    }
    
    private void initComponents() {
        // Server Status Panel
        JPanel serverStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverStatusPanel.setBorder(BorderFactory.createTitledBorder("Server Status"));
        statusLabel = new JLabel("Server is stopped");
        startStopButton = new JButton("Start Server");
        serverStatusPanel.add(statusLabel);
        serverStatusPanel.add(startStopButton);
        
        // Client List Panel
        clientListPanel = new JPanel();
        clientListPanel.setBorder(BorderFactory.createTitledBorder("Client List"));
        clientListPanel.setLayout(new BoxLayout(clientListPanel, BoxLayout.Y_AXIS));
        clientListPanel.setPreferredSize(new Dimension(200, 0));
        
        // Log Panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Logs"));
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Configuration Panel
        JPanel configPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
        
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
        
        // Add all panels to frame
        add(serverStatusPanel, BorderLayout.NORTH);
        add(clientListPanel, BorderLayout.WEST);
        add(logPanel, BorderLayout.CENTER);
        add(configPanel, BorderLayout.SOUTH);
    }
    
    private void setupActions() {
        startStopButton.addActionListener(e -> {
            if (!isRunning) {
                startServer();
            } else {
                stopServer();
            }
        });
        
        // Window closing event
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                stopServer();
            }
        });
    }
    
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            serverSocket = new ServerSocket(port);
            isRunning = true;
            
            log("Server started on port " + port);
            statusLabel.setText("Server is running");
            startStopButton.setText("Stop Server");
            portField.setEnabled(false);
            
            // Start accepting clients in a separate thread
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String clientIP = clientSocket.getInetAddress().getHostAddress();
                        log("New client connected: " + clientIP);
                        
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
    
    private void stopServer() {
        isRunning = false;
        try {
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
            updateClientList();
            
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            
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
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;
        private String clientIP;
        
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
        
        private void setupStreams() throws IOException {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            clientName = in.readLine();
            String joinMessage = "SERVER: " + clientName + " has joined the chat";
            broadcastMessage(joinMessage);
            updateClientList();
            sendClientList();
        }
        
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
        
        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != this) {
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
                updateClientList();
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                log("Error closing client handler: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}