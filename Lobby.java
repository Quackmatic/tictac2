import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a lobby in which players wait and can send game requests.
 */
public class Lobby {
    private HashMap<String, Integer> players;
    private LobbyProvider provider;
    private ArrayList<LobbyObserver> observers;
    private int lobbyID;

    /**
     * Initialize a new Lobby with the given lobby ID and lobby provider.
     *
     * @param lobbyID The ID of the lobby to create.
     * @param provider The provider of the network communication.
     */
    public Lobby(int lobbyID, LobbyProvider provider) {
        this.provider = provider;
        this.lobbyID = lobbyID;

        players = new HashMap<String, Integer>();
        observers = new ArrayList<LobbyObserver>();
    }

    /**
     * Get the ID of this lobby, for identification over the network.
     * (unused)
     *
     * @return the ID of this lobby.
     */
    protected int getLobbyID() {
        return lobbyID;
    }

    /**
     * Add an observer to this lobby, that shall receive notifications when
     * an event occurs in this lobby.
     *
     * @param observer The observer to add.
     */
    public void addObserver(LobbyObserver observer) {
        observers.add(observer);
    }

    /**
     * Get the nicknames of all players in this lobby.
     *
     * @return An array of all nicknames of the players in this lobby.
     */
    public String[] getPlayers() {
        return players.keySet().toArray(new String[0]);
    }

    /**
     * Notify the observers of this lobby that a message has been received
     * by the client which is not specifically associated with a game.
     *
     * @param message The received message body.
     * @param title The received message title.
     * @param messageType The type of the message to use with JOptionPane.
     */
    public void messageReceived(String message, String title, int messageType) {
        for(LobbyObserver observer : observers) {
            observer.messageReceived(message, title, messageType);
        }
    }

    /**
     * Add a player to this lobby.
     *
     * @param nickname The nickname of the player to add to the lobby.
     * @param score The score of the player to add to the lobby.
     */
    public void addPlayer(String nickname, int score) {
        players.put(nickname, score);
        for(LobbyObserver observer : observers) {
            observer.playerEnter(nickname, score);
        }
    }

    /**
     * Remove a player from this lobby.
     *
     * @param nickname The nickname of the player to remove from the lobby.
     */
    public void removePlayer(String nickname) {
        players.remove(nickname);
        for(LobbyObserver observer : observers) {
            observer.playerLeave(nickname);
        }
    }

    /**
     * Get the score of the player with the given nickname.
     *
     * @param nickname The nickname of the player whose score to obtain.
     * @return The score of the player with the specified nickname.
     */
    public int getPlayerScore(String nickname) {
        return players.get(nickname);
    }

    /**
     * Send a game request to the player with the given nickname.
     *
     * @param recipient The nickname of the recipient of the request.
     */
    public void sendGameRequest(String recipient) {
        provider.sendGameRequest(this, recipient);
    }

    /**
     * Notify every observer of this lobby that a game request has been
     * sent and delivered to the recipient, or otherwise.
     *
     * @param recipient The receipient of the request as given by the client.
     * @param gameID The ID of the game request if {@code recipient} exists,
     * {@code -1} otherwise.
     */
    public void gameRequestSent(String recipient, int gameID) {
        for(LobbyObserver observer : observers) {
            observer.gameRequestSent(gameID, recipient);
        }
    }
    
    /**
     * Respond to a request to play a game with the given game ID.
     *
     * @param gameID The ID of the game request to respond to.
     * @param accept {@code true} to accept the request; {@code false} otherwise.
     */
    public void respondToGameRequest(int gameID, boolean accept) {
        provider.respondToGameRequest(this, gameID, accept);
    }

    /**
     * Notify every observer of this lobby that a game request, sent by the
     * client with nickname {@code sender}, with the given game ID.
     *
     * @param sender The nickname of the client who sent the request.
     * @param gameID The ID of the game request received.
     */
    public void gameRequestReceived(String sender, int gameID) {
        for(LobbyObserver observer : observers) {
            observer.gameRequestReceived(gameID, sender);
        }
    }
}
