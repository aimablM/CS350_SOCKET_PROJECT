package client.clientGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientGUI extends JFrame { // Changed class name to match convention (uppercase)

    // Components for the GUI
    private JTextField serverIpField; // Text field for the server IP address
    private JTextField clientIpField; // Text field for the client IP address (display only)
    private JTextArea receivedMessageArea; // Text area to display received messages
    private JTextField sendMessageField; // Text field for the message to send
    private JLabel errorMessageLabel; // Label to display error messages

    // Socket-related variables
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientGUI() {
        super("Client Socket Application"); // Updated title
        setSize(400, 300); // Updated size for a more complex GUI
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Initialize components
        serverIpField = new JTextField(15);
        clientIpField = new JTextField(15);
        clientIpField.setEditable(false);
        receivedMessageArea = new JTextArea(5, 20);
        receivedMessageArea.setEditable(false);
        sendMessageField = new JTextField(15);
        errorMessageLabel = new JLabel(" ");

        JButton clearButton = new JButton("Clear");
        JButton sendButton = new JButton("Send");
        JButton quitButton = new JButton("Quit");

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Server IP Address:"));
        panel.add(serverIpField);
        panel.add(new JLabel("Client IP Address:"));
        panel.add(clientIpField);
        panel.add(new JLabel("Received Message:"));
        panel.add(new JScrollPane(receivedMessageArea));
        panel.add(new JLabel("Message to Send:"));
        panel.add(sendMessageField);
        panel.add(new JLabel("Error Message:"));
        panel.add(errorMessageLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(quitButton);

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageField.setText("");
                errorMessageLabel.setText(" ");
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = sendMessageField.getText();
                if (message.isEmpty()) {
                    errorMessageLabel.setText("Message cannot be empty.");
                } else {
                    sendMessageToServer(message); // Highlighted part starts here
                }
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeConnection();
                System.exit(0);
            }
        });

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        initializeConnection(); // Initialize socket connection
    }

    // Socket initialization and communication methods

    private void initializeConnection() {
        try {
            String serverIp = serverIpField.getText().isEmpty() ? "127.0.0.1" : serverIpField.getText();
            int port = 5000;
            socket = new Socket(serverIp, port);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            clientIpField.setText(socket.getLocalAddress().getHostAddress());
            errorMessageLabel.setText("Connected to the server.");

            String clientName = JOptionPane.showInputDialog(this, "Enter your name:");
            out.println(clientName);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    listenForServerMessages();
                }
            }).start();

        } catch (Exception e) {
            errorMessageLabel.setText("Connection failed: " + e.getMessage());
        }
    }

    private void sendMessageToServer(String message) { // Highlighted part
        if (socket != null && out != null) {
            out.println(message);
            receivedMessageArea.append("You: " + message + "\n");
            errorMessageLabel.setText("Message sent.");
        } else {
            errorMessageLabel.setText("Not connected to the server.");
        }
    }

    private void listenForServerMessages() { // Highlighted part
        String response;
        try {
            while ((response = in.readLine()) != null) {
                receivedMessageArea.append(response + "\n");
            }
        } catch (Exception e) {
            errorMessageLabel.setText("Error reading from server: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            errorMessageLabel.setText("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ClientGUI gui = new ClientGUI();
                gui.setVisible(true);
            }
        });
    }
}
