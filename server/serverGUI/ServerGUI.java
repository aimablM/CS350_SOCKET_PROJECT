package server.serverGUI;

import javax.swing.*;
import java.awt.*;

//Main Class for GUI Application
public class ServerGUI extends JFrame {

    public ServerGUI() {
        super("Server GUI Development");
        setLayout(new BorderLayout());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel serverStatusPanel = new JPanel();
        serverStatusPanel.setBorder(BorderFactory.createTitledBorder("Server Status"));
        serverStatusPanel.add(new JLabel("Server is running..."));
        
        JPanel clientListPanel = new JPanel();
        clientListPanel.setBorder(BorderFactory.createTitledBorder("Client List"));
        clientListPanel.setLayout(new BoxLayout(clientListPanel, BoxLayout.Y_AXIS));
        clientListPanel.add(new JLabel("Client 1"));
        clientListPanel.add(new JLabel("Client 2"));

        JPanel logPanel = new JPanel();
        logPanel.setBorder(BorderFactory.createTitledBorder("Logs"));
        JTextArea logArea = new JTextArea(10, 30);
        logArea.setText("Log messages will appear here...");
        logPanel.add(new JScrollPane(logArea));

        JPanel configPanel = new JPanel();
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
        configPanel.setLayout(new GridLayout(2, 2));
        configPanel.add(new JLabel("Server IP:"));
        configPanel.add(new JTextField("127.0.0.1"));
        configPanel.add(new JLabel("Port:"));
        configPanel.add(new JTextField("8080"));

        add(serverStatusPanel, BorderLayout.NORTH);
        add(clientListPanel, BorderLayout.WEST);
        add(logPanel, BorderLayout.CENTER);
        add(configPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}