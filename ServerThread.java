import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerThread implements Runnable {
    private Server server;
    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private LinkedBlockingQueue<PacketWriter> sendQueue;
    private String nickname;
    private int score;

    public ServerThread(
            Server server,
            String nickname,
            int score,
            Socket client,
            DataInputStream inputStream,
            DataOutputStream outputStream) {
        this.server = server;
        this.nickname = nickname;
        this.score = score;
        this.client = client;
        this.in = inputStream;
        this.out = outputStream;
    }

    public String getNickname() {
        return nickname;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        server.playerUpdate(this);
    }
    
    @Override
    public void run() {
        try {
            server.playerUpdate(this);
            while(client.isConnected()) {
                int packetID = in.readInt();
                handlePacket(packetID);
            }
            server.playerLeave(this);
        } catch(IOException e) {
            System.out.println("IOException in ServerThread");
            e.printStackTrace();
        }
    }

    private void handlePacket(int packetID) throws IOException {
        switch(packetID) {
            case Packet.CLIENT_REQUEST_SEND: {
                String nickname = in.readUTF();
                ServerThread opponent = server.getClient(nickname);
                if(opponent != null) {
                    ServerGame newGame = server.createGame(this, opponent);
                    sendGameRequestSent(newGame, nickname);
                    opponent.sendGameRequestReceived(newGame, getNickname());
                } else {
                    sendGameRequestSent(null, nickname);
                }
                break;
            }
            case Packet.CLIENT_REQUEST_RESPOND: {
                int gameID = in.readInt();
                boolean accept = in.readBoolean();
                ServerGame game = server.getGame(gameID);
                if(game != null) {
                    if(accept) {
                        game.begin();
                    } else {
                        server.removeGame(game);
                    }
                } else {
                    sendMessage(null, "That game does not exist.", "Game", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
            case Packet.CLIENT_PLAYER_GET_LIST: {
                final ServerThread _this = this;
                server.doToAllClients(t -> {
                    _this.sendPlayerUpdate(t);
                });
                break;
            }
            case Packet.CLIENT_GAME_MOVE: {
                int gameID = in.readInt();
                int x = in.readInt();
                int y = in.readInt();
                ServerGame game = server.getGame(gameID);
                if(game != null) {
                    game.makeMove(this, x, y);
                } else {
                    sendMessage(null, "That game does not exist.", "Game", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
        }
    }

    public void sendMessage(ServerGame game, String message, String title, int messageType) {
        int gameID = game == null ? -1 : game.getGameID();
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_MESSAGE);
            o.writeInt(gameID);
            o.writeUTF(message);
            o.writeUTF(title);
            o.writeInt(messageType);
        });
    }

    public void sendGameBegin(ServerGame game, ServerThread opponent, int playingAs) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_GAME_BEGIN);
            o.writeUTF(opponent.getNickname());
            o.writeInt(playingAs);
        });
    }

    public void sendGameMove(ServerGame game, int x, int y, int tileType) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_GAME_MOVE);
            o.writeInt(game.getGameID());
            o.writeInt(x);
            o.writeInt(y);
            o.writeInt(tileType);
        });
    }

    public void sendGameUpdate(ServerGame game, boolean canMove, int gameState) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_GAME_UPDATE);
            o.writeInt(game.getGameID());
            o.writeBoolean(canMove);
            o.writeInt(gameState);
        });
    }

    public void sendGameRequestSent(ServerGame game, String nickname) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_REQUEST_SENT);
            o.writeInt(game == null ? -1 : game.getGameID());
            o.writeUTF(nickname);
        });
    }

    public void sendGameRequestReceived(ServerGame game, String nickname) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_REQUEST_RECEIVED);
            o.writeInt(game.getGameID());
            o.writeUTF(nickname);
        });
    }

    public void sendPlayerUpdate(ServerThread thread) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_PLAYER_UPDATE);
            o.writeUTF(thread.getNickname());
            o.writeInt(thread.getScore());
        });
    }

    public void sendPlayerLeave(ServerThread thread) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_PLAYER_LEAVE);
            o.writeUTF(thread.getNickname());
        });
    }

    private void runSendThread() {
        try {
            while(client.isConnected()) {
                PacketWriter writer = sendQueue.take();
                writer.writePacket(out);
            }
        } catch(IOException e) {
            System.out.println("IOException in Send Thread.");
            e.printStackTrace();
        } catch(InterruptedException e) {
            System.out.println("Interrupted in Send Thread.");
            System.exit(255);
        }
    }
}
