// TCPClient.java
import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        String serverName = "localhost"; // Server address
        int port = 6789;
        
        try (Socket clientSocket = new Socket(serverName, port)) {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Connected to server. Type messages (type 'exit' to close connection).");

            String message;
            while (true) {
                System.out.print("Client: ");
                message = userInput.readLine();
                outToServer.writeBytes(message + "\n");

                String response = inFromServer.readLine();
                System.out.println("Server: " + response);
                
                if (response.equals("END")) {
                    System.out.println("Connection closed by server.");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
