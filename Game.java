import java.util.ArrayList;

/**
 * A class which controls a tic-tac-toe game played over a network.
 *
 * @author Tom Galvin
 */
public class Game {
    public static final int TILE_SPACE  = 0;
    public static final int TILE_NOUGHT = 1;
    public static final int TILE_CROSS  = 2;

    public static final int GAME_IN_PROGRESS = 0;
    public static final int GAME_WON         = 1;
    public static final int GAME_LOST        = 2;
    public static final int GAME_DRAW        = 3;

    private ArrayList<GameObserver> observers;
    private GameProvider provider;

    private int localPlayer;
    private boolean canMove;

    private String remotePlayerNickname;
    private int gameID;

    private int[][] gameBoard;
    private int gameStatus;

    /**
     * Gets the string visually representing a game tile.
     *
     * @param tile The state of the tile, as a TILE_* constant.
     */
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
     * Creates a new network-based tic-tac-toe game.
     *
     * @param provider The {@link GameProvider} allowing the client to send
     * update the state of the game.
     * @param remotePlayerNickname The nickname of the remote player currently
     * being opposed.
     * @param localPlayer The tile (nought or cross) that the local player is
     * @param gameID The ID used to identify this game over the network.
     * currently playing.
     */
    public Game(
            GameProvider provider,
            String remotePlayerNickname,
            int localPlayer,
            int gameID) {
        this.provider = provider;
        this.remotePlayerNickname = remotePlayerNickname;
        this.localPlayer = localPlayer;
        this.gameID = gameID;

        this.canMove = false;
        this.gameStatus = Game.GAME_IN_PROGRESS;
        this.observers = new ArrayList<GameObserver>();
        this.gameBoard = new int[3][3];

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                gameBoard[x][y] = Game.TILE_SPACE;
            }
        }
    }

    /**
     * Gets the game ID of this game, for identification over the
     * network.
     *
     * @return The game ID of the game represented by this object.
     */
    protected int getGameID() {
        return gameID;
    }

    /**
     * Notify every observer of this game that the specified message has been
     * received by the server.
     * This is also used internally to display message boxes in the game window
     * to avoid re-typing similar JOptionPane code.
     *
     * @param message The message received by the server.
     * @param title The title of the message.
     * @param messageType The type of message used by {@link javax.swing.JOptionPane}
     * to change the way the message is displayed.
     */
    public void gameMessageReceived(String message, String title, int messageType) {
        for(GameObserver observer : observers) {
            observer.gameMessageReceived(message, title, messageType);
        }
    }

    /**
     * Add a new {@link GameObserver} to this game.
     *
     * @param listener The new observer to be told when a game event occurs.
     */
    public void addGameObserver(GameObserver observer) {
        observers.add(observer);
    }

    /**
     * Gets whether the current player can move.
     *
     * @return Whether the local player can move, ie. it its the local player's turn.
     */
    public boolean getCanMove() {
        return canMove;
    }

    /**
     * Sets whether the local player can move at the given time.
     *
     * @param canMove Whether the local player can currently move.
     */
    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
        for(GameObserver observer : observers) {
            observer.gameStateChanged(gameStatus, canMove);
        }
    }

    /**
     * Get the current status of the game.
     *
     * @return The status of the game. This will be one of the GAME_* constants.
     */
    public int getGameStatus() {
        return gameStatus;
    }

    /**
     * Sets the status of the game.
     *
     * @param status The status of the game. This will be a GAME_* constant.
     */
    public void setGameStatus(int status) {
        if(status == Game.GAME_IN_PROGRESS ||
           status == Game.GAME_WON ||
           status == Game.GAME_LOST ||
           status == Game.GAME_DRAW) {
            gameStatus = status;
            for(GameObserver observer : observers) {
                observer.gameStateChanged(gameStatus, canMove);
            }
        } else {
            throw new IllegalArgumentException(
                    String.format(
                        "The given game status (%d) is not valid.",
                        status
                        )
                    );
        }
    }

    /**
     * Get the value of a given tile.
     *
     * @param x The X co-ordinate (between 0 and 2).
     * @param y The Y co-ordinate (between 0 and 2).
     * @return The value of the tile. This will be one of the TILE_* constants.
     */
    public int getTileValue(int x, int y) {
        if(x >= 0 && x < 3 &&
           y >= 0 && y < 3) {
            return gameBoard[x][y];
        } else {
            throw new IllegalArgumentException(
                    String.format(
                        "The given tile co-ordinate (%d, %d) is not " +
                        "within the boundaries of the game board (both " +
                        "indices must be between 0 and 2 inclusive).",
                        x, y
                        )
                    );
        }
    }
    
    /**
     * Sets the value of the given tile on the board.
     *
     * @param x The X co-ordinate (between 0 and 2).
     * @param y The Y co-ordinate (between 0 and 2).
     * @param value The value of the tile. This will be one of the TILE_* constants.
     */
    public void setTileValue(int x, int y, int value) {
        if(x >= 0 && x < 3 &&
           y >= 0 && y < 3) {
            if(value == Game.TILE_SPACE ||
               value == Game.TILE_NOUGHT ||
               value == Game.TILE_CROSS) {
                gameBoard[x][y] = value;
                for(GameObserver observer : observers) {
                    observer.gameTileChanged(x, y, value);
                }
            } else {
                throw new IllegalArgumentException(
                        String.format(
                            "The given tile value (%d) is not valid.",
                            value
                            )
                        );
            }
        } else {
            throw new IllegalArgumentException(
                    String.format(
                        "The given tile co-ordinate (%d, %d) is not " +
                        "within the boundaries of the game board (both " +
                        "indices must be between 0 and 2 inclusive).",
                        x, y
                        )
                    );
        }
    }

    /**
     * Makes a move by the player at the specified location.
     *
     * @param x The X co-ordinate (between 0 and 2).
     * @param y The Y co-ordinate (between 0 and 2).
     */
    public void makeMove(int x, int y) {
        provider.makeMove(this, x, y);
    }

    /**
     * Forfeit this game.
     */
    public void forfeit() {
        if(getGameStatus() == Game.GAME_IN_PROGRESS) {
            provider.forfeit(this);
            remove();
        }
    }

    /**
     * Removes this game from the client's internal game map.
     */
    public void remove() {
        provider.remove(this);
    }

    /**
     * Get which symbol the local player is playing as.
     *
     * @return The symbol the local player is playing as. This will either be TILE_NOUGHT or TILE_CROSS.
     */
    public int getLocalPlayer() {
        return localPlayer;
    }

    /**
     * Get which symbol the remote player is playing as.
     * The default implementation checks the value of {@code getLocalPlayer()} and returns the opposite symbol.
     *
     * @return The symbol the remote player is playing as. This will either be TILE_NOUGHT or TILE_CROSS.
     */
    public int getRemotePlayer() {
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
    public String getRemotePlayerNickname() {
        return remotePlayerNickname;
    }
}
