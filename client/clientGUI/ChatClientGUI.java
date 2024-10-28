package client.clientGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * ChatClientGUI provides the user interface for the chat application.
 * This class handles all UI components and user interactions, delegating
 * network operations to the ChatClient class.
 */
public class ChatClientGUI extends JFrame {
    // UI Components
    private JTextField serverIPField;
    private JTextField usernameField;
    private JLabel connectedLabel;
    private JButton disconnectButton;
    private JTextArea messagesArea;
    private JTextField messageField;
    private JButton sendButton;
    
    // Chat client instance
    private final ChatClient chatClient;
    
    /**
     * Constructor sets up the GUI and initializes the ChatClient
     */
    public ChatClientGUI() {
        // Initialize chat client with callback handlers
        chatClient = new ChatClient(
            this::handleMessage,      // Message handler
            this::handleError,        // Error handler
            this::handleConnection    // Connection handler
        );
        
        initializeGUI();
        setupListeners();
    }
    
    /**
     * Initializes all GUI components and layouts
     */
    private void initializeGUI() {
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Connection Panel
        JPanel connectionPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        
        // Server IP input
        JPanel serverIPPanel = new JPanel(new BorderLayout());
        JLabel serverIPLabel = new JLabel("Server IP:");
        serverIPLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        serverIPField = new JTextField("localhost");
        serverIPField.setFont(new Font("Arial", Font.PLAIN, 20));
        serverIPPanel.add(serverIPLabel, BorderLayout.WEST);
        serverIPPanel.add(serverIPField, BorderLayout.CENTER);
        
        // Username input
        JPanel usernamePanel = new JPanel(new BorderLayout());
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 20));
        usernamePanel.add(usernameLabel, BorderLayout.WEST);
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        
        connectionPanel.add(serverIPPanel);
        connectionPanel.add(usernamePanel);
        
        // Status Panel
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        connectedLabel = new JLabel("Connected to: ");
        connectedLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setFont(new Font("Arial", Font.PLAIN, 20));
        statusPanel.add(connectedLabel, BorderLayout.CENTER);
        statusPanel.add(disconnectButton, BorderLayout.EAST);
        disconnectButton.setEnabled(false);
        
        // Messages Area
        JPanel messagesPanel = new JPanel(new BorderLayout());
        JLabel messagesLabel = new JLabel("Messages:");
        messagesLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        messagesArea = new JTextArea();
        messagesArea.setFont(new Font("Arial", Font.PLAIN, 16));
        messagesArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messagesArea);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        messagesPanel.add(messagesLabel, BorderLayout.NORTH);
        messagesPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Message Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 20));
        messageField.setEnabled(false);
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.PLAIN, 20));
        sendButton.setEnabled(false);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Add components to main panel
        mainPanel.add(connectionPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(statusPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(messagesPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(inputPanel);
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Sets up event listeners for all interactive components
     */
    private void setupListeners() {
        // Connect when username is entered
        usernameField.addActionListener(e -> connect());
        
        disconnectButton.addActionListener(e -> chatClient.disconnect());
        
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chatClient.disconnect();
            }
        });
    }
    
    /**
     * Attempts to establish connection to the server
     */
    private void connect() {
        String username = usernameField.getText().trim();
        String serverIP = serverIPField.getText().trim();
        
        if (username.isEmpty()) {
            handleError("Username cannot be empty");
            return;
        }
        
        try {
            chatClient.connect(serverIP, username);
        } catch (IOException e) {
            handleError("Connection failed: " + e.getMessage());
        }
    }
    
    /**
     * Sends the current message to the server
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && chatClient.isConnected()) {
            if (chatClient.sendMessage(message)) {
                messageField.setText("");
            } else {
                handleError("Failed to send message");
            }
        }
    }
    
    /**
     * Handles incoming messages from the server
     */
    private void handleMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messagesArea.append(message + "\n");
            messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
        });
    }
    
    /**
     * Handles error messages
     */
    private void handleError(String error) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * Handles connection state changes
     */
    private void handleConnection(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            serverIPField.setEnabled(!connected);
            usernameField.setEnabled(!connected);
            disconnectButton.setEnabled(connected);
            messageField.setEnabled(connected);
            sendButton.setEnabled(connected);
            
            if (connected) {
                connectedLabel.setText("Connected to: " + serverIPField.getText().trim());
            } else {
                connectedLabel.setText("Connected to: ");
            }
        });
    }
    
    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI().setVisible(true);
        });
    }
}