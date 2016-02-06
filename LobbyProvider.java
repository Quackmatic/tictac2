/**
 * The provider interface for receiving events when the local client intends to change the state of the lobby.
 *
 * @author Tom Galvin
 */
public interface LobbyProvider {
    /**
     * Send a game request to another client.
     *
     * @param lobby The lobby in which the other client is in.
     * @param nickname The nickname of the recipient.
     */
    public void sendGameRequest(Lobby lobby, String nickname);

    /**
     * Respond to a game request from another client.
     * 
     * @param lobby The lobby from which the request originated.
     * @param gameID The ID of the game request.
     * @param accept {@code true} to accept the request; {@code false} otherwise.
     */
    public void respondToGameRequest(Lobby lobby, int gameID, boolean accept);

    /**
     * Request the server to send information on all currently present clients
     * in the given lobby.
     *
     * @param lobby The lobby to obtain player information from.
     */
    public void getInitialPlayers(Lobby lobby);
}
