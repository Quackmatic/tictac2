/**
 * An abstract interface for a game of tic-tac-toe.
 */
public interface Game {
    public static final int TILE_SPACE  = 0;
    public static final int TILE_NOUGHT = 1;
    public static final int TILE_CROSS  = 2;

    public static final int GAME_IN_PROGRESS = 0;
    public static final int GAME_WON         = 1;
    public static final int GAME_LOST        = 2;

    public static String getTileString(int tile) {
        switch(tile) {
            case TILE_NOUGHT: return "O";
            case TILE_CROSS:  return "X";
            case TILE_SPACE:  return "";
            default:
                throw new IllegalArgumentException(
                        String.format(
                            "Invalid tile type: %d",
                            tile
                            )
                        );
        }
    }

    /**
     * Add a new {@link GameListener} to this game.
     *
     * @param listener The new listener to be told when a game event occurs.
     */
    public void addGameListener(GameListener listener);

    /**
     * Gets whether the current player can move.
     *
     * @return Whether the local player can move, ie. it its the local player's turn.
     */
    public boolean getCanMove();

    /**
     * Get the current status of the game.
     *
     * @return The status of the game. This will be one of the GAME_* constants.
     */
    public int getGameStatus();

    /**
     * Get the value of a given tile.
     *
     * @param x The X co-ordinate (between 0 and 2).
     * @param y The Y co-ordinate (between 0 and 2).
     * @return The value of the tile. This will be one of the TILE_* constants.
     */
    public int getTileValue(int x, int y);

    /**
     * Get which symbol the local player is playing as.
     *
     * @return The symbol the local player is playing as. This will either be TILE_NOUGHT or TILE_CROSS.
     */
    public int getLocalPlayer();

    /**
     * Get which symbol the remote player is playing as.
     * The default implementation checks the value of {@code getLocalPlayer()} and returns the opposite symbol.
     *
     * @return The symbol the remote player is playing as. This will either be TILE_NOUGHT or TILE_CROSS.
     */
    public default int getRemotePlayer() {
        int localPlayer = getLocalPlayer();

        switch(localPlayer) {
            case TILE_NOUGHT: return TILE_CROSS;
            case TILE_CROSS:  return TILE_NOUGHT;
            default:
                throw new IllegalArgumentException(
                        String.format(
                            "getLocalPlayer() should always return TILE_NOUGHT or " +
                            "TILE_CROSS, but instead it returned %s.",
                            localPlayer == TILE_SPACE ?
                                "TILE_SPACE" :
                                String.valueOf(localPlayer)
                            )
                        );
        }
    }

    /**
     * Get the nickname of the remote player.
     *
     * @return This will return the nickname that the local player is playing against.
     */
    public String getRemotePlayerNickname();
}
