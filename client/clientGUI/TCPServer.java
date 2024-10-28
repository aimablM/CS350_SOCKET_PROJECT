// TCPServer.java
import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        int port = 6789;
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started and waiting for a client...");
            Socket connectionSocket = serverSocket.accept();
            System.out.println("Client connected.");

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String clientMessage;
            while ((clientMessage = inFromClient.readLine()) != null) {
                System.out.println("Received: " + clientMessage);
                
                // Protocol for message handling
                if (clientMessage.equalsIgnoreCase("exit")) {
                    outToClient.writeBytes("END\n");
                    System.out.println("Connection closed by client request.");
                    break;
                } else {
                    outToClient.writeBytes("ACK\n");
                }
            }

            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}