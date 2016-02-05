import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.function.Consumer;

public class Server implements Runnable {
    private int port;
    private ServerSocket server;
    private boolean running;
    private HashMap<String, ServerThread> clients;
    private HashMap<Integer, ServerGame> games;
    private int currentGameID;

    public Server(int port) {
        this.port = port;
        this.clients = new HashMap<String, ServerThread>();
        this.games = new HashMap<Integer, ServerGame>();

        this.currentGameID = 0;
    }

    public ServerThread getClient(String nickname) {
        if(clients.containsKey(nickname)) {
            return clients.get(nickname);
        } else {
            return null;
        }
    }

    public ServerGame createGame(ServerThread initiator, ServerThread opponent) {
        ServerGame game = new ServerGame(this, currentGameID++, initiator, opponent);
        games.put(game.getGameID(), game);
        initiator.addToGame(game);
        opponent.addToGame(game);
        return game;
    }

    public ServerGame getGame(int gameID) {
        if(games.containsKey(gameID)) {
            return games.get(gameID);
        } else {
            return null;
        }
    }

    public void removeGame(ServerGame game) {
        if(games.containsKey(game.getGameID())) {
            games.remove(game.getGameID());
            game.getNought().removeFromGame(game);
            game.getCross().removeFromGame(game);
        }
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(3000);

            while(running) {
                try {
                    Socket clientSocket = server.accept();

                    try {
                        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

                        int connectPacketID = inputStream.readInt();
                        if(connectPacketID != Packet.CLIENT_CONNECT) {
                            System.out.println(connectPacketID);
                            System.out.println("Client did not send CLIENT_CONNECT packet, terminating connection.");
                            clientSocket.close();
                        } else {
                            int clientProtocolVersion = inputStream.readInt();
                            if(clientProtocolVersion != Packet.PROTOCOL_VERSION) {
                                outputStream.writeInt(Packet.SERVER_STATUS);
                                outputStream.writeBoolean(false);
                                outputStream.writeUTF(String.format(
                                            "The server version is %s than the client version.",
                                            Packet.PROTOCOL_VERSION > clientProtocolVersion ? "newer" : "older"
                                            ));
                                clientSocket.close();
                            } else {
                                String nickname = inputStream.readUTF();
                                int extensions = inputStream.readInt(); // unused for now
                                
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

    public void doToAllClients(Consumer<ServerThread> action) {
        for(ServerThread thread : clients.values()) {
            action.accept(thread);
        }
    }

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
    
    public void playerUpdate(ServerThread _thread) {
        final ServerThread thread = _thread;
        doToAllClients(t -> {
            if(thread != t) {
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
