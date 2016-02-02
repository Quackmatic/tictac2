/**
 * The listener interface for receiving updates when the state of a game changes.
 *
 * @author Tom Galvin
 */
public interface GameListener {
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
}
