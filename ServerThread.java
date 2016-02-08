import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents one user connected to the server, handling most network-related
 * events.
 */
public class ServerThread implements Runnable {
    private Server server;
    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private LinkedBlockingQueue<PacketWriter> sendQueue;
    private String nickname;
    private int score;
    private ArrayList<ServerGame> currentGames;

    /**
     * Create a new server thread.
     *
     * @param server The server that this client is connected to.
     * @param nickname The chosen (possibly adjusted) nickname that 
     * this client is using.
     * @param score The current score of this client, usually starting at 0.
     * @param client The socket this client is using for communication.
     * @param inputStream The data input stream being used.
     * @param outputstream The data output stream being used.
     */
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

        this.currentGames = new ArrayList<ServerGame>();
        this.sendQueue = new LinkedBlockingQueue<PacketWriter>();
    }

    /**
     * Prints a message to the server's standard output, prefixed with this
     * client's nickname.
     *
     * @param s The message to print to the standard output.
     */
    private void print(String s) {
        System.out.println(String.format(
                    "[%s] %s",
                    nickname,
                    s
                    ));
    }

    /**
     * Add this client to the given game.
     *
     * @param game The game in which this client is participating.
     */
    public void addToGame(ServerGame game) {
        currentGames.add(game);
    }

    /**
     * Remove this client from the given game.
     *
     * @param game The game in which this client is no longer participating.
     */
    public void removeFromGame(ServerGame game) {
        currentGames.remove(game);
    }

    /**
     * Get the nickname being used by this client.
     *
     * @return The nickname being used by this client.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Gets the current score of this client.
     *
     * @return The score of this client.
     */
    public int getScore() {
        return score;
    }

    /**
     * Updates the score of this client and informs other
     * connected users.
     *
     * @param score The score that this client now has.
     */
    public void setScore(int score) {
        this.score = score;
        server.playerUpdate(this);
    }
    
    @Override
    public void run() {
        try {
            new Thread(() -> runSendThread()).start();
            server.playerUpdate(this);
            while(client.isConnected()) {
                int packetID = in.readInt();
                handlePacket(packetID);
            }
            server.playerLeave(this);
        } catch(EOFException e) {
            print("Client quit. (EOF)");
            server.playerLeave(this);
        } catch(IOException e) {
            print("Client disconnected. (IOException: " + e.getMessage() + ")");
            e.printStackTrace();
        }
        for(ServerGame game : currentGames) {
            game.terminateGame(this, getNickname() + " disconnected.");
        }
    }

    /**
     * Handles the packet with the given packet ID.
     * It does this by reading the remainder of the packet components
     * from the {@link java.io.DataInputStream} passed to the server
     * thread upon construction.
     *
     * @param packetID The ID of the packet to handle.
     */
    private void handlePacket(int packetID) throws IOException {
        switch(packetID) {
            case Packet.CLIENT_REQUEST_SEND: {
                String nickname = in.readUTF();
                ServerThread opponent = server.getClient(nickname);
                if(opponent != null) {
                    print("Sent a game request to " + nickname + ".");
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
                    print("Responded to game ID " + gameID + ".");
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
                server.doToAllClients(t -> {
                    if(this != t) {
                        this.sendPlayerUpdate(t);
                    }
                });
                break;
            }
            case Packet.CLIENT_GAME_MOVE: {
                int gameID = in.readInt();
                int x = in.readInt();
                int y = in.readInt();
                ServerGame game = server.getGame(gameID);
                if(game != null) {
                    print("Placed symbol at (" + x + ", " + y + ") in game " + gameID + ".");
                    game.makeMove(this, x, y);
                } else {
                    sendMessage(null, "That game does not exist.", "Game", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
            case Packet.CLIENT_GAME_FORFEIT: {
                int gameID = in.readInt();
                ServerGame game = server.getGame(gameID);
                if(game != null) {
                    print("Forfeited from game " + gameID + ".");
                    game.terminateGame(this, getNickname() + " forfeit.");
                } else {
                    sendMessage(null, "That game does not exist.", "Game", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
        }
    }

    /**
     * Send a message packet to the client.
     *
     * @param game The game to associate the message with, if any. This just
     * determines which GUI window the dialog is modal to on the client side.
     * @param message The message to present to the client.
     * @param title The title of the message to present to the client.
     * @param messageType The numeric identifier of the type of message that
     * this message represents. This affects the way the message is presented
     * visually. If this is equal to {@code -1}, then the server will display
     * this as a status string rather than a message box.
     */
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

    /**
     * Send a packet to the client indicating that {@code game} has just started.
     *
     * @param game the game which has started.
     * @param opponent The player who this client is opposing.
     * @param playingAs The tile that this player will place onto the board.
     */
    public void sendGameBegin(ServerGame game, ServerThread opponent, int playingAs) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_GAME_BEGIN);
            o.writeInt(game.getGameID());
            o.writeUTF(opponent.getNickname());
            o.writeInt(playingAs);
        });
    }

    /**
     * Send a packet to the client indicating that either this, or the opposing
     * client, has successfully made a move on the game board.
     *
     * @param game The game in which the move took place.
     * @param x The X co-ordinate of the location on the board (between 0 and 2).
     * @param y The Y co-ordinate of the location on the board (between 0 and 2).
     * @param tileType The type of tile that was placed on the game board.
     */
    public void sendGameMove(ServerGame game, int x, int y, int tileType) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_GAME_MOVE);
            o.writeInt(game.getGameID());
            o.writeInt(x);
            o.writeInt(y);
            o.writeInt(tileType);
        });
    }

    /**
     * Sends a packet to the client updating them on the state of {@code game}.
     *
     * @param game The game whose state has changed.
     * @param canMove Whether this client is now able to make a move (or false if
     * it is the opponent's turn).
     * @param gameState The state of the game as a GAME_* constant.
     */
    public void sendGameUpdate(ServerGame game, boolean canMove, int gameState) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_GAME_UPDATE);
            o.writeInt(game.getGameID());
            o.writeBoolean(canMove);
            o.writeInt(gameState);
        });
    }

    /**
     * Sends a packet to the client indicating that their request to
     * initiate a game has either been successfully delivered, or that the
     * client with the given {@code nickname} does not exist.
     *
     * @param game The server game object tentatively representing this game
     * if the request was successful, or {@code null} if the user with the
     * given nickname does not exist.
     * @param nickname The nickname, as specified by the client, of the user
     * to oppose.
     */
    public void sendGameRequestSent(ServerGame game, String nickname) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_REQUEST_SENT);
            o.writeInt(game == null ? -1 : game.getGameID());
            o.writeUTF(nickname);
        });
    }

    /**
     * Send a packet to the client informing them that they have received a
     * request to play a game.
     *
     * @param game The game tentatively representing the game which may be
     * played (or declined).
     * @param nickname The nickname of the client who sent the request.
     */
    public void sendGameRequestReceived(ServerGame game, String nickname) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_REQUEST_RECEIVED);
            o.writeInt(game.getGameID());
            o.writeUTF(nickname);
        });
    }

    /**
     * Send a packet updating the client on the state of a user in the lobby.
     *
     * @param thread The thread of the user whose info must be presented to
     * this client.
     */
    public void sendPlayerUpdate(ServerThread thread) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_PLAYER_UPDATE);
            o.writeUTF(thread.getNickname());
            o.writeInt(thread.getScore());
        });
    }

    /**
     * Send a packet informing the client that the user represented by the given
     * thread has left the server.
     *
     * @param thread The thread of the user who left the server.
     */
    public void sendPlayerLeave(ServerThread thread) {
        sendQueue.add(o -> {
            o.writeInt(Packet.SERVER_PLAYER_LEAVE);
            o.writeUTF(thread.getNickname());
        });
    }

    /**
     * The main body of the thread which is used to send data to the client.
     * Taking this approach (calling this method from a lambda-expression,
     * rather than using a separate {@link Runnable} object, is mainly to
     * keep similar concerns together in the same class.
     */
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
