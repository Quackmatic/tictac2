/**
 * The provider interface for receiving events when the local client intends to change the state of the lobby.
 *
 * @author Tom Galvin
 */
public interface LobbyProvider {
    public void sendGameRequest(String nickname);
    public void acceptGameRequest(int gameID);
    public void getInitialPlayers();
}
