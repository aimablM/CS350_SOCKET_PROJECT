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
   - Enter a username(the name for that given client)
   - Press Enter or click Connect
   - The client will connect to the server
