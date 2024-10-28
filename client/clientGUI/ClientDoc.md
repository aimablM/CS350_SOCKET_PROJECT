By Javon Jackson

Client-Side Socket Application Documentation

Overview 

The ClientGUI class implements a client-side graphical user interface (GUI) for socket communication in a simple chat application. It allows the client to connect to a server, send messages, and receive messages from other clients via the server. 
Components and Methods 

1. GUI Components 
The graphical interface is built using Java Swing components: 
- JTextField serverIpField: Text field where the user can enter the server's IP address. - JTextField clientIpField: Displays the client's IP address (read-only). 
- JTextArea receivedMessageArea: Text area for displaying messages received from the server. - JTextField sendMessageField: Text field for entering the message to be sent to the server. - JLabel errorMessageLabel: Label for displaying error messages or connection status. 

2. Socket-Related Variables 
- Socket socket: The socket used for client-server communication. 
- PrintWriter out: Used to send messages to the server.
- BufferedReader in: Used to receive messages from the server. 

Methods 
3. public ClientGUI() 
The constructor sets up the GUI and initializes the connection: 
- Configures the main window (JFrame), including size, layout, and title. - Adds the GUI components (text fields, buttons, labels, etc.). 
- Sets up action listeners for the buttons: 
 - Clear Button: Clears the input fields and resets the error message.  - Send Button: Sends the message typed in sendMessageField to the server.  - Quit Button: Closes the connection and exits the application. 
- Calls initializeConnection() to establish the socket connection. 

4. private void initializeConnection() 
Establishes the connection to the server using a socket: 
- Steps: 
 1. Gets the server IP address from serverIpField (defaults to 127.0.0.1 if empty).  2. Attempts to connect to the server on port 5000. 
 3. Initializes the input (BufferedReader) and output (PrintWriter) streams.  4. Sets the client's IP address in clientIpField. 
 5. Prompts the user to enter their name, which is sent to the server as the first message.
 6. Starts a new thread to listen for incoming messages from the server using listenForServerMessages(). 

5. private void sendMessageToServer(String message) 
Sends a message to the server: 
- Checks if the socket and output stream are initialized. 
- Sends the message to the server using PrintWriter. 
- Displays the sent message in receivedMessageArea with a "You: " prefix. 
- Updates errorMessageLabel to indicate that the message was sent. 
- If the socket is not connected, shows an error message in errorMessageLabel. 

6. private void listenForServerMessages() 
Listens for incoming messages from the server in a separate thread: 
- Reads messages from the server using BufferedReader. 
- Appends each received message to receivedMessageArea. 
- Continues listening as long as the server sends messages. 
- If an error occurs while reading from the server, displays the error in errorMessageLabel. 

7. private void closeConnection() 
Closes the socket connection: 
- Attempts to close the socket if it is not null. 
- Catches any exceptions that may occur during the closing process.
- Updates errorMessageLabel if an error occurs while closing the connection. 

8. public static void main(String[] args) 
Launches the client application: 
- Uses SwingUtilities.invokeLater() to ensure that the GUI is created on the Event Dispatch Thread for thread safety. 
- Creates an instance of ClientGUI and makes the window visible. 

Usage Instructions 
1. Run the ClientGUI Application: 
 - Launch the ClientGUI class to start the client application. 
2. Enter the Server IP Address: 
 - Type the IP address of the server you wish to connect to. 
3. Connect to the Server: 
 - If the server is running, the client will connect, and the status will be displayed in the errorMessageLabel. 
4. Enter Your Name: 
 - When prompted, enter your name. This will be the identifier used for chat messages. 5. Send Messages: 
 - Type a message in the "Message to Send" field and click the "Send" button.  - Messages will be displayed in the chat area with your identifier. 
6. Quit the Application:
 - Click the "Quit" button to close the connection and exit the program. 

Error Handling 
- Connection Issues: 
 - If the server is not reachable, an error message is displayed in the errorMessageLabel. - Empty Messages: 
 - If an attempt is made to send an empty message, the errorMessageLabel will indicate that the message cannot be empty. 
- Disconnection: 
 - If the client is disconnected unexpectedly, the error message will reflect this. 
 
Additional Documentation (for README.md) 
Include information about how to compile and run the Java program, any dependencies needed, and instructions for testing the client-server communication with other group members' components.
