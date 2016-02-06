/**
 * The provider interface for receiving events when the local client intends to change the state of the game.
 *
 * @author Tom Galvin
 */
public interface GameProvider {
    /**
     * Invoked when the local player intends to make a move at the given
     * position on the game board.
     *
     * @param game The game in which the move is being made.
     * @param x The X co-ordinate (between 0 and 2).
     * @param y The Y co-ordinate (between 0 and 2).
     */
    public void makeMove(Game game, int x, int y);

    /**
     * Invoked when the local player forfeits the game.
     *
     * @param game The game which the local player is forfeiting.
     */
    public void forfeit(Game game);

    /**
     * Invoked when the game recognises that it has terminated, and that the
     * game may be removed from any internal game maps.
     *
     * @param game The game which is to be removed.
     */
    public void remove(Game game);
}
