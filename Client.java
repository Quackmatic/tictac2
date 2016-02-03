import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Client implements Runnable, LobbyProvider, GameProvider {
    private Socket socket;
    private Lobby lobby;
    private HashMap<Integer, Game> games;

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

            new Client(machineName, port).run();
        }
    }

    public Client(String hostName, int port) {
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
                sendThread.run();

                while(socket.isConnected() && running) {
                    int packetID = inputStream.readInt();
                    handlePacket(inputStream, packetID);
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
        }
    }

    public void handlePacket(DataInputStream i, int packetID) throws IOException {
        switch(packetID) {
            case Packet.SERVER_WELCOME: {
                throw new UnsupportedOperationException(
                        "Only one SERVER_WELCOME packet should be received " +
                        "per connection.");
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
            case Packet.SERVER_MESSAGE: {
                int gameID = i.readInt();
                String message = i.readUTF();
                
                if(gameID == -1 || !games.containsKey(gameID)) {
                    lobby.messageReceived(message);
                } else {
                    games.get(gameID).gameMessageReceived(message);
                }
                break;
            }
        }
    }

    public void runSendThread() {
        try {
            while(running && socket.isConnected()) {
                PacketWriter writer = sendQueue.take();
                writer.writePacket(outputStream);
            }
        } catch(IOException e) {
            System.out.println("IOException in Send Thread.");
            e.printStackTrace();
            System.exit(255);
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

    public void acceptGameRequest(Lobby lobby, int gameID) {
        sendQueue.add(o -> {
            o.writeInt(Packet.CLIENT_REQUEST_ACCEPT);
            o.writeInt(gameID);
        });
    }

    public void getInitialPlayers(Lobby lobby) {
        sendQueue.add(o -> {
            o.writeInt(Packet.CLIENT_PLAYER_GET_LIST);
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
}
