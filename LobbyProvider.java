/**
 * The provider interface for receiving events when the local client intends to change the state of the lobby.
 *
 * @author Tom Galvin
 */
public interface LobbyProvider {
    public void sendGameRequest(Lobby lobby, String nickname);
    public void respondToGameRequest(Lobby lobby, int gameID, boolean accept);
    public void getInitialPlayers(Lobby lobby);
}
