import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * The main class for the tictac2 server.
 */
public class Server implements Runnable {
    private int port;
    private ServerSocket server;
    private boolean running;
    private HashMap<String, ServerThread> clients;
    private HashMap<Integer, ServerGame> games;
    private int currentGameID;

    /**
     * Initialise a new server with the given port to run on.
     *
     * @param port The port on which to listen for clients.
     */
    public Server(int port) {
        this.port = port;
        this.clients = new HashMap<String, ServerThread>();
        this.games = new HashMap<Integer, ServerGame>();

        this.currentGameID = 0;
    }

    /**
     * Gets the {@link ServerThread} associated with the given nickname.
     *
     * @param nickname The nickname of the client.
     * @return The server thread associated with the given nickname, or {@code null}
     * if that client does not exist.
     */
    public ServerThread getClient(String nickname) {
        if(clients.containsKey(nickname)) {
            return clients.get(nickname);
        } else {
            return null;
        }
    }

    /**
     * Create a new game with the given initiating and opposing client, and add
     * it to the internal map of occurring games.
     *
     * @param initiator The client who initiated the game.
     * @param opponent The client who accepted the game request.
     */
    public ServerGame createGame(ServerThread initiator, ServerThread opponent) {
        ServerGame game = new ServerGame(this, currentGameID++, initiator, opponent);
        games.put(game.getGameID(), game);
        initiator.addToGame(game);
        opponent.addToGame(game);
        return game;
    }

    /**
     * Gets the game associated with the given game ID.
     *
     * @param gameID The ID of the game to get.
     * @return The server game info of the game with the given ID, or
     * {@code null} if no such game exists.
     */
    public ServerGame getGame(int gameID) {
        if(games.containsKey(gameID)) {
            return games.get(gameID);
        } else {
            return null;
        }
    }

    /**
     * Removes the game with the given ID from the server.
     * This also dissocates both participants from the game.
     *
     * @param game The game to remove.
     */
    public void removeGame(ServerGame game) {
        if(games.containsKey(game.getGameID())) {
            games.remove(game.getGameID());
            game.getNought().removeFromGame(game);
            game.getCross().removeFromGame(game);
        }
    }

    /**
     * Stop the server.
     */
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        try {
            System.out.println("Starting server...");
            server = new ServerSocket(port);
            System.out.println("Server listening on port " + port + ".");
            server.setSoTimeout(3000);

            while(running) {
                try {
                    Socket clientSocket = server.accept();
                    InetAddress address = clientSocket.getInetAddress();
                    System.out.println("New client inbound from " +
                            address.getHostAddress() + 
                            "(" + address.getHostName() + ").");
                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

                        // Check that the client sends the correct packet first
                        int connectPacketID = inputStream.readInt();
                        if(connectPacketID != Packet.CLIENT_CONNECT) {
                            System.out.println(connectPacketID);
                            System.out.println("Client did not send CLIENT_CONNECT packet, terminating connection.");
                            clientSocket.close();
                        } else {
                            // Check that there is no version mismatch between client and server
                            int clientProtocolVersion = inputStream.readInt();
                            if(clientProtocolVersion != Packet.PROTOCOL_VERSION) {
                                System.out.println("Client/server version mismatch.");
                                outputStream.writeInt(Packet.SERVER_STATUS);
                                outputStream.writeBoolean(false);
                                outputStream.writeUTF(String.format(
                                            "The server version is %s than the client version.",
                                            Packet.PROTOCOL_VERSION > clientProtocolVersion ? "newer" : "older"
                                            ));
                                clientSocket.close();
                            } else {
                                String nickname = inputStream.readUTF();
                                System.out.println("Client identifying as " + nickname + "...");
                                int extensions = inputStream.readInt(); // unused for now
                                
                                // If needed, append a number onto the end of
                                // the client's nickname to avoid uniqueness
                                // issues. The client will be made aware of
                                // this upon login.
                                String replacementNickname = nickname;
                                int attempts = 0;
                                while(clients.containsKey(replacementNickname)) {
                                    replacementNickname = String.format(
                                            "%s [%d]",
                                            nickname,
                                            ++attempts
                                            );
                                }
                                nickname = replacementNickname;
                                if(attempts > 0) {
                                    System.out.println("Client assigned replacement nickname " + nickname + ".");
                                }

                                outputStream.writeInt(Packet.SERVER_STATUS);
                                outputStream.writeBoolean(true);
                                outputStream.writeUTF(nickname);

                                ServerThread client = new ServerThread(
                                        this,
                                        nickname,
                                        0,
                                        clientSocket,
                                        inputStream,
                                        outputStream);
                                clients.put(nickname, client);
                                new Thread(client).start();
                            }
                        }
                    } catch(Exception e) {
                        System.out.println("Error during connection initialization.");
                        e.printStackTrace();
                    }
                } catch(SocketTimeoutException e) {
                    // connection timed out, this allows
                    // the server thread to be stopped
                }
            }
        } catch(IOException e) {
            System.out.println("IO Exception:");
            e.printStackTrace();
        } finally {
            running = false;
            try {
                server.close();
            } catch(IOException e) {
                System.out.println("Error closing server.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Perform a specific action on all clients connected to the server.
     *
     * @param action The action to perform, as a {@link ServerThread} consumer.
     */
    public void doToAllClients(Consumer<ServerThread> action) {
        for(ServerThread thread : clients.values()) {
            action.accept(thread);
        }
    }

    /**
     * Removes the given server thread from the server and notifies other
     * clients that the user has left.
     *
     * @param _thread The thread of the leaving player.
     */
    public void playerLeave(ServerThread _thread) {
        final ServerThread thread = _thread;
        if(clients.containsKey(_thread.getNickname())) {
            clients.remove(thread.getNickname());
        }

        doToAllClients(t -> {
            if(thread != t) {
                t.sendPlayerLeave(thread);
            }
        });
    }
    
    /**
     * Updates other clients about the current state of the client represented
     * by {@code _thread}.
     *
     * @param _thread The thread of the client to inform other users about.
     */
    public void playerUpdate(ServerThread _thread) {
        final ServerThread thread = _thread;
        doToAllClients(t -> {
            if(_thread != t) {
                t.sendPlayerUpdate(thread);
            }
        });

        if(!clients.containsKey(thread.getNickname())) {
            clients.put(thread.getNickname(), thread);
        }
    }
    
    public static void main(String... args) {
        if(args.length != 1) {
            System.out.println("Usage:");
            System.out.println();
            System.out.println("java Server <port>");
            System.out.println("port: The port to listen on for clients.");
        } else {
            new Server(Integer.valueOf(args[0])).run();
        }
    }
}
