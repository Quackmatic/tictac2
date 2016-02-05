import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JOptionPane;

public class Client implements Runnable, LobbyProvider, GameProvider {
    private Socket socket;
    private Lobby lobby;
    private HashMap<Integer, Game> games;

    private String localNickname;
    private String hostName;
    private int port;

    private LinkedBlockingQueue<PacketWriter> sendQueue;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private Thread sendThread;

    private boolean running = true;

    public void disconnect() {
        running = false;
    }

    public static void main(String... args) {
        if(args.length != 3) {
            System.out.println("Usage:");
            System.out.println();
            System.out.println("java Client <nickname> <port> <machine-name>");
            System.out.println("nickname: The nickname you wish to play as.");
            System.out.println("port: The port used on the server machine.");
            System.out.println("machine-name: The name of the server machine.");
            System.exit(1);
        } else {
            String nickname = args[0];
            int port = Integer.valueOf(args[1]);
            String machineName = args[2];

            new Client(nickname, machineName, port).run();
        }
    }

    public Client(String localNickname, String hostName, int port) {
        this.localNickname = localNickname;
        this.hostName = hostName;
        this.port = port;

        this.games = new HashMap<Integer, Game>();

        this.sendQueue = new LinkedBlockingQueue<PacketWriter>();
    }

    public void run() {
        try {
            socket = new Socket(hostName, port);
            try(DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                this.outputStream = outputStream;

                lobby = new Lobby(0, this);
                LobbyPanel.openLobby(lobby);

                sendThread = new Thread(() -> runSendThread());
                sendThread.start();

                sendInitialConnectionData(localNickname);

                int welcomePacketID = inputStream.readInt();
                if(welcomePacketID != Packet.SERVER_STATUS) {
                    lobby.messageReceived(
                            "Invalid welcome packet from server.",
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                } else {
                    handlePacket(inputStream, welcomePacketID);
                }

                getInitialPlayers(lobby);

                while(socket.isConnected() && running) {
                    int packetID = inputStream.readInt();
                    handlePacket(inputStream, packetID);
                }

                if(running) {
                    lobby.messageReceived(
                            "Server closed the connection.",
                            "Server",
                            JOptionPane.ERROR_MESSAGE
                            );
                }
            } finally {
                running = false;
            }
        } catch(UnknownHostException e) {
            System.out.println(String.format(
                    "Unknown host: %s",
                    hostName));
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePacket(DataInputStream i, int packetID) throws IOException {
        switch(packetID) {
            case Packet.SERVER_STATUS: {
                boolean successfulConnection = i.readBoolean();
                if(!successfulConnection) {
                    String serverErrorMessage = i.readUTF();
                    lobby.messageReceived(
                            String.format("Server says:\n%s", serverErrorMessage),
                            "Server Error",
                            JOptionPane.ERROR_MESSAGE
                            );
                    System.exit(1);
                }
                String serverNickname = i.readUTF();
                if(!serverNickname.equals(localNickname)) {
                    lobby.messageReceived(
                            String.format("The nickname %s is taken or not allowed, so " +
                                          "the server changed your nickname to %s.",
                                          localNickname,
                                          serverNickname),
                            "Nickname Changed",
                            JOptionPane.WARNING_MESSAGE);
                    localNickname = serverNickname;
                }
                break;
            }
            case Packet.SERVER_REQUEST_SENT: {
                int gameID = i.readInt();
                String opponentNickname = i.readUTF();
                lobby.gameRequestSent(opponentNickname, gameID);
                break;
            }
            case Packet.SERVER_REQUEST_RECEIVED: {
                int gameID = i.readInt();
                String opponentNickname = i.readUTF();
                lobby.gameRequestReceived(opponentNickname, gameID);
                break;
            }
            case Packet.SERVER_PLAYER_UPDATE: {
                String nickname = i.readUTF();
                int score = i.readInt();
                lobby.addPlayer(nickname, score);
                break;
            }
            case Packet.SERVER_PLAYER_LEAVE: {
                String nickname = i.readUTF();
                lobby.removePlayer(nickname);
                break;
            }
            case Packet.SERVER_GAME_BEGIN: {
                int gameID = i.readInt();
                String opponentNickname = i.readUTF();
                int playingAs = i.readInt();

                Game game = new Game(
                        this,
                        opponentNickname,
                        playingAs,
                        gameID
                        );
                games.put(gameID, game);
                GamePanel.openGame(game);
                break;
            }
            case Packet.SERVER_GAME_UPDATE: {
                int gameID = i.readInt();
                boolean canMove = i.readBoolean();
                int state = i.readInt();
                if(games.containsKey(gameID)) {
                    Game game = games.get(gameID);
                    game.setCanMove(canMove);
                    game.setGameStatus(state);
                }
                break;
            }
            case Packet.SERVER_GAME_MOVE: {
                int gameID = i.readInt();
                int x = i.readInt();
                int y = i.readInt();
                int tileValue = i.readInt();
                if(games.containsKey(gameID)) {
                    games.get(gameID).setTileValue(x, y, tileValue);
                }
                break;
            }
            case Packet.SERVER_MESSAGE: {
                int gameID = i.readInt();
                String message = i.readUTF();
                String title = i.readUTF();
                int messageType = i.readInt();
                
                if(gameID == -1 || !games.containsKey(gameID)) {
                    lobby.messageReceived(message, title, messageType);
                } else {
                    games.get(gameID).gameMessageReceived(message, title, messageType);
                }
                break;
            }
        }
    }

    private void runSendThread() {
        try {
            while(running && socket.isConnected()) {
                PacketWriter writer = sendQueue.take();
                writer.writePacket(outputStream);
            }
        } catch(IOException e) {
            System.out.println("IOException in Send Thread.");
            e.printStackTrace();
        } catch(InterruptedException e) {
            System.out.println("Interrupted in Send Thread.");
            System.exit(255);
        }
    }

    public void sendGameRequest(Lobby lobby, String nickname) {
        sendQueue.add(o -> {
            o.writeInt(Packet.CLIENT_REQUEST_SEND);
            o.writeUTF(nickname);
        });
    }

    public void respondToGameRequest(Lobby lobby, int gameID, boolean accept) {
        sendQueue.add(o -> {
            o.writeInt(Packet.CLIENT_REQUEST_RESPOND);
            o.writeInt(gameID);
            o.writeBoolean(accept);
        });
    }

    public void getInitialPlayers(Lobby lobby) {
        sendQueue.add(o -> {
            o.writeInt(Packet.CLIENT_PLAYER_GET_LIST);
        });
    }

    public void sendInitialConnectionData(String nickname) {
        sendQueue.add(o -> {
            o.writeInt(Packet.CLIENT_CONNECT);
            o.writeInt(Packet.PROTOCOL_VERSION); // protocol identifier
            o.writeUTF(nickname);
            o.writeInt(0); // reserved
        });
    }

    public void makeMove(Game game, int x, int y) {
        sendQueue.add(o -> {
            o.writeInt(Packet.CLIENT_GAME_MOVE);
            o.writeInt(game.getGameID());
            o.writeInt(x);
            o.writeInt(y);
        });
    }

    public void forfeit(Game game) {
        sendQueue.add(o -> {
            o.writeInt(Packet.CLIENT_GAME_FORFEIT);
            o.writeInt(game.getGameID());
        });
    }
}
