import java.util.ArrayList;

/**
 * A class which controls a tic-tac-toe game played over a network.
 */
public class NetworkGame implements Game {
    private ArrayList<GameListener> listeners;
    private String remotePlayerNickname;
    private int localPlayer;
    private int gameStatus;
    private int[][] gameBoard;
    private boolean canMove;

    public NetworkGame(String remotePlayerNickname, int localPlayer, boolean canMove) {
        this.remotePlayerNickname = remotePlayerNickname;
        this.localPlayer = localPlayer;
        this.listeners = new ArrayList<GameListener>();
        this.gameStatus = Game.GAME_IN_PROGRESS;
        this.gameBoard = new int[3][3];
        this.canMove = canMove;

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                gameBoard[x][y] = Game.TILE_SPACE;
            }
        }
    }

    public void addGameListener(GameListener listener) {
        listeners.add(listener);
    }

    public boolean getCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
        for(GameListener listener : listeners) {
            listener.gameStateChanged(gameStatus, canMove);
        }
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(int status) {
        if(status == Game.GAME_IN_PROGRESS ||
           status == Game.GAME_WON ||
           status == Game.GAME_LOST) {
            gameStatus = status;
            for(GameListener listener : listeners) {
                listener.gameStateChanged(gameStatus, canMove);
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
    
    public void setTileValue(int x, int y, int value) {
        if(x >= 0 && x < 3 &&
           y >= 0 && y < 3) {
            if(value == Game.TILE_SPACE ||
               value == Game.TILE_NOUGHT ||
               value == Game.TILE_CROSS) {
                gameBoard[x][y] = value;
                for(GameListener listener : listeners) {
                    listener.gameTileChanged(x, y, value);
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

    public int getLocalPlayer() {
        return localPlayer;
    }

    public String getRemotePlayerNickname() {
        return remotePlayerNickname;
    }
}
