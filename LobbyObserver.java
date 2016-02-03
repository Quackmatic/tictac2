/**
 * The observer interface for receiving events when the state of the lobby changes.
 *
 * @author Tom Galvin
 */
public interface LobbyObserver {
    /**
     * Invoked when either a new player enters the lobby, or the score of an existing lobby changes.
     *
     * @param nickname The nickname of the player.
     * @param score The score of said playe.r
     */
    public void playerEnter(String nickname, int score);

    /**
     * Invoked when a player leaves the lobby.
     *
     * @param nickname The nickname of the leaving player.
     */
    public void playerLeave(String nickname);

    /**
     * Invoked when the local player receives a game request from another player.
     *
     * @param gameID The ID of the game request.
     * @param sender The nickname of the player who sent the request to the local player.
     */
    public void gameRequestReceived(int gameID, String sender);

    /**
     * Invoked when the local player has sent a game request to another player.
     *
     * @param gameID The ID of the game request, or {@code -1} if the specified player did not exist.
     * @param receiver The nickname of the player who shall receive the request to the local player.
     */
    public void gameRequestSent(int gameID, String receiver);

    /**
     * Invoked when a game, in which the client is a player, begins.
     *
     * @param game The {@link Game} object representing the game that was started.
     */
    public void gameStarted(Game game);
}
