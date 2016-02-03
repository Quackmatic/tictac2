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
     * @param x The X co-ordinate (between 0 and 2).
     * @param y The Y co-ordinate (between 0 and 2).
     */
    public void makeMove(int x, int y);
}
