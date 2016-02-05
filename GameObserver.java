/**
 * The observer interface for receiving events when the state of a game changes.
 *
 * @author Tom Galvin
 */
public interface GameObserver {
    /**
     * Invoked when a tile on the game board changes.
     *
     * @param x The X co-ordinate (between 0 and 2).
     * @param y The Y co-ordinate (between 0 and 2).
     * @param value The new value of this tile. This will be one of the TILE_* constants in {@link Game}.
     */
    public void gameTileChanged(int x, int y, int value);

    /**
     * Invoked when the state of the game changes.
     *
     * @param state The new state of the game. This will be one of the GAME_* constants in {@link Game}.
     * @param canMove Whether the local player can now make a move or not.
     */
    public void gameStateChanged(int state, boolean canMove);

    /**
     * Invoked when the client receives a game-related message from the server.
     *
     * @param message The received message.
     * @param title The title of the message.
     * @param messageType A {@code *_MESSAGE} constant from {@link javax.swing.JOptionPane} indicating the type of the message.
     */
    public void gameMessageReceived(String message, String title, int messageType);
}
