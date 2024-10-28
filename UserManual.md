# Chat Application User Manual
## Table of Contents
1. System Requirements
2. Setup Instructions
3. Running the Application
4. Testing Procedures
5. Common Issues & Troubleshooting

## 1. System Requirements
- Java Runtime Environment (JRE) 8 or higher
- Network connectivity between client and server machines
- Wireshark installed (for packet capture testing)

## 2. Setup Instructions

### 2.1 Project Structure
Ensure you have the following Java files in your project:
```
src/
├── server/
│   ├── ChatServer.java
│   └── serverGUI/
│       └── ServerGUI.java
└── client/
    └── clientGUI/
        ├── ChatClient.java
        └── ChatClientGUI.java
```

### 2.2 Compilation
1. Open a terminal/command prompt
2. Navigate to the project's source directory
3. Compile the files:
```bash
javac server/serverGUI/ServerGUI.java
javac client/clientGUI/ChatClientGUI.java
```

## 3. Running the Application

### 3.1 Starting the Server
1. Open a terminal/command prompt
2. Navigate to the compiled classes directory
3. Run the server:
```bash
java server.serverGUI.ServerGUI
```
4. The server GUI will appear showing:
   - Server IP address (automatically detected)
   - Port number (default: 5000)
   - Start/Stop button
   - Client list panel
   - Log area

5. Click "Start Server" to begin accepting connections

### 3.2 Starting Clients
1. Open a new terminal/command prompt
2. Navigate to the compiled classes directory
3. Run the client:
```bash
java client.clientGUI.ChatClientGUI
```
4. In the client GUI:
   - Enter the server's IP address (use "localhost" if running on same machine)
   - Enter a username
   - Press Enter or click Connect
   - The client will connect to the server

## 4. Testing Procedures

### 4.1 Basic Functionality Testing
1. **Server Connection Test**
   - Start the server
   - Verify the server status shows "Server is running"
   - Check the log area for startup message

2. **Client Connection Test**
   - Start 2-3 client instances
   - Enter different usernames for each
   - Verify each client appears in the server's client list
   - Check server logs for connection messages

3. **Message Broadcasting Test**
   - Send a message from Client A
   - Verify the message appears in other clients' chat windows
   - Verify the message format: "Username: Message"
   - Check server logs for message relay

4. **Disconnection Test**
   - Close a client using the Quit button
   - Verify the client is removed from server's client list
   - Check other clients receive "[Username] has left the chat"
   - Verify server logs show disconnection

### 4.2 Wireshark Capture Testing

1. **Setup Capture**
   - Open Wireshark
   - Select network interface
   - Apply filter: `tcp.port == 5000`
   - Start capture

2. **Connection Sequence**
   - Start server
   - Connect client
   - Observe TCP handshake packets
   - Note the initial username transmission

3. **Message Exchange**
   - Send test messages between clients
   - Observe TCP packets containing message data
   - Verify message format in packet payload

4. **Disconnection Sequence**
   - Disconnect client
   - Observe TCP connection termination
   - Save the capture file

### 4.3 Error Testing
1. **Invalid Server Address**
   - Enter invalid IP in client
   - Verify error message
   - Check connection retry behavior

2. **Server Shutdown**
   - Connect multiple clients
   - Stop server
   - Verify clients show disconnection message
   - Check cleanup of resources

3. **Network Interruption**
   - Simulate network disconnection
   - Verify error handling
   - Test reconnection capabilities

## 5. Common Issues & Troubleshooting

### 5.1 Connection Issues
- **Problem**: Client can't connect to server
  - Verify server is running
  - Check IP address is correct
  - Ensure port 5000 is not blocked
  - Try "localhost" for same-machine testing

- **Problem**: "Address already in use" error
  - Stop any running instances of the server
  - Wait 30 seconds for port release
  - Try different port if needed

### 5.2 Runtime Issues
- **Problem**: Client freezes when sending message
  - Check server is still running
  - Verify network connection
  - Restart client application

- **Problem**: Messages not appearing
  - Check client connection status
  - Verify server is relaying messages
  - Review server logs for errors

### 5.3 GUI Issues
- **Problem**: GUI elements unresponsive
  - Check connection status
  - Verify application state
  - Restart affected component

### 5.4 Logging
For debugging purposes, important events are logged:
- Server start/stop
- Client connections/disconnections
- Error conditions
- Message relay events

Check both server logs and client error messages when troubleshooting issues.