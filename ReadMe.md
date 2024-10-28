# N-Way Chat Application G

A Java-based client-server chat application with graphical user interfaces for both client and server components.

## Team Members- Group 6
- Aimable (Team Leader) - Core Server Development
- Femi - Server GUI Development
- Javon - Client GUI Development
- Evan - Network Protocol Implementation
- Toni - Testing & Documentation

## Project Overview
This project implements a peer-to-peer (N-way) chat system where multiple clients can connect to a central server and exchange messages. The server relays messages to all connected clients except the sender.

### Key Features
- Multi-client support with real-time message relay
- Graphical user interfaces for both client and server
- Live client connection tracking
- Server broadcast capabilities
- Comprehensive error handling and logging
- TCP-based reliable communication

## Requirements
- Java Development Kit (JDK) 8 or higher
- Java Swing (included in JDK)

## Project Structure
```
server/
├── ChatServer.java (Original server implementation)
├── TestClient.java (Console-based test client)
├── ChatClientGUI.java (Graphical client implementation)
└── serverGUI/
    └── ServerGUI.java (Graphical server implementation)
```

## Setup & Compilation
1. Ensure Java is installed and configured:
```bash
java --version
```

2. Compile all source files:
```bash
javac server/*.java server/serverGUI/*.java
```

## Running the Application

### Starting the Server
```bash
java server.serverGUI.ServerGUI
```
The server GUI will appear with:
- Server status display
- Connected clients list
- Activity log
- Configuration panel (IP and port settings)

### Starting Clients
```bash
java server.ChatClientGUI
```
Each client window includes:
- Server connection settings
- Message display area
- Input field for sending messages
- Status/error messages
- Clear, Send, and Quit buttons

## Usage Instructions

### Server
1. Launch the server application
2. The server IP will be automatically populated
3. Default port is 5000 (can be modified if needed)
4. Click "Start Server" to begin accepting connections
5. Monitor connected clients in the client list panel
6. View all activity in the logs panel
7. Use "Stop Server" to shut down gracefully

### Client
1. Launch the client application
2. Enter server IP address (localhost/127.0.0.1 for local testing)
3. Enter your username when prompted
4. Send messages using the input field and Send button
5. View incoming messages in the message area
6. Use Clear to reset the message display
7. Click Quit or close window to disconnect

## Implementation Details

### Network Protocol
- Uses TCP (Transmission Control Protocol)
- Port 5000 by default
- Messages are plain text with newline delimiters
- Supports special commands (e.g., /quit for disconnection)

### Server Features
- Multi-threaded client handling
- Real-time client tracking
- Message broadcasting
- Connection management
- Activity logging
- Graceful shutdown capability

### Client Features
- Auto-reconnection attempts
- Real-time message display
- Error reporting
- Clean disconnection handling
- User-friendly interface

## Testing

To test the system:
1. Start the server application
2. Launch multiple client instances
3. Connect clients using different usernames
4. Send messages between clients
5. Verify message delivery to all other clients
6. Test disconnection and reconnection
7. Monitor server logs for activity
e
## Known Issues
- Evan's contribution pending for network protocol implementation
- Additional testing needed for high user loads
- User authentication not yet implemented

## Future Improvements
- Private messaging capability
- User authentication system
- File transfer support
- Emoji support
- Message history persistence
- Advanced server configuration options

## Project Status
- Basic functionality implemented and working
- GUI integration complete
- Testing in progress
- Documentation ongoing

## Contributing
Contact Aimable (Team Leader) for:
- Code review requests
- Feature suggestions
- Bug reports
- Documentation updates

## Assignment Requirements Checklist
- [x] GUI implementation for both client and server
- [x] TCP socket communication
- [x] Multi-client support
- [x] Message relay functionality
- [x] Client tracking
- [x] Error handling
- [ ] Wireshark analysis (pending)
- [ ] API documentation (pending)
- [ ] Final testing (in progress)

## Additional Notes
- Server IP and port configuration can be modified in the server GUI
- Clients can be run on different machines within the same network
- Server should be started before clients attempt to connect
- Keep the server and client code in their respective packages for proper functionality