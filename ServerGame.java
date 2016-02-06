import javax.swing.JOptionPane;

/**
 * Holds information on the state of a game on the server.
 */
public class ServerGame {
    private Server server;
    private ServerThread nought;
    private ServerThread cross;
    private ServerThread currentPlayer = null;
    private int gameID;
    private int[][] gameBoard;

    /**
     * Creates a new ServerGame.
     *
     * @param server The Server on which the game is being played.
     * @param gameID The game ID for this game.
     * @param nought The player who is playing as nought.
     * @param cross The player who is playing as cross.
     */
    public ServerGame(
            Server server,
            int gameID,
            ServerThread nought,
            ServerThread cross) {
        this.server = server;
        this.gameID = gameID;
        this.nought = nought;
        this.cross = cross;
        
        this.gameBoard = new int[3][3];
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                gameBoard[i][j] = Game.TILE_SPACE;
            }
        }
    }

    /**
     * Get the ID of this game.
     *
     * @return The ID of this game.
     */
    public int getGameID() {
        return gameID;
    }

    /**
     * Get the ServerThread for the player playing as nought.
     *
     * @return The ServerThread for the player playing as nought.
     */
    public ServerThread getNought() {
        return nought;
    }

    /**
     * Get the ServerThread for the player playing as cross.
     *
     * @return The ServerThread for the player playing as cross.
     */
    public ServerThread getCross() {
        return cross;
    }

    /**
     * Switch the current player from whoever is currently taking a
     * move, to the one who is currently waiting.
     */
    private void switchCurrentPlayer() {
        if(currentPlayer == nought) {
            currentPlayer = cross;
        } else {
            currentPlayer = nought;
        }
    }

    /**
     * Determines if this game is in progress or not.
     *
     * @return {@code true} if the game has started, but not finished.
     */
    public boolean isInProgress() {
        return currentPlayer != null;
    }

    /**
     * Sets this game into motion, sending any relevant packets.
     */
    public void begin() {
        if(!isInProgress()) {
            currentPlayer = cross;
            cross.sendGameBegin(
                    this,
                    nought,
                    Game.TILE_CROSS
                    );
            nought.sendGameBegin(
                    this,
                    cross,
                    Game.TILE_NOUGHT
                    );
            sendGameUpdate();
        }
    }

    /**
     * Determines whether the game board is in a winning state.
     * Checking this every move is sufficient to determine whether the
     * previous move is a winner, and hence this can be used to determine
     * the player of the winning move, simplifying the implementation of this
     * function.
     *
     * @return Whether the game board is in a winning state.
     */
    public boolean isGameWon() {
        // check vert. rows
        for(int x = 0; x < 3; x++) {
            if(gameBoard[x][0] != Game.TILE_SPACE &&
                    gameBoard[x][0] == gameBoard[x][1] && gameBoard[x][1] == gameBoard[x][2]) {
                return true;
            }
        }
        // check hoz. rows
        for(int y = 0; y < 3; y++) {
            if(gameBoard[0][y] != Game.TILE_SPACE &&
                    gameBoard[0][y] == gameBoard[1][y] && gameBoard[1][y] == gameBoard[2][y]) {
                return true;
            }
        }
        // check diagonals
        if(gameBoard[0][0] != Game.TILE_SPACE &&
           gameBoard[0][0] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][2] ||
           gameBoard[0][2] != Game.TILE_SPACE &&
           gameBoard[0][2] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][0]) {
            return true;
        }
        return false;
    }

    /**
     * Check if every space on the game board is occupied by either a nought
     * or a cross.
     *
     * @return Whether the board is full of symbols or not.
     */
    private boolean isBoardFull() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if (gameBoard[i][j] == Game.TILE_SPACE) return false;
            }
        }

        return true;
    }

    /**
     * Sends an update with information on the current game to both
     * clients.
     */
    private void sendGameUpdate() {
        cross.sendGameUpdate(this, currentPlayer == cross, Game.GAME_IN_PROGRESS);
        nought.sendGameUpdate(this, currentPlayer == nought, Game.GAME_IN_PROGRESS);
    }

    /**
     * Terminates the current game, with the given user-at-fault and reason.
     *
     * @param leaver The user who left in order to cause this game to be
     * terminated.
     * @param reason The reason for this game being terminated. This will be
     * presented to the other participant of this game.
     */
    public void terminateGame(ServerThread leaver, String reason) {
        ServerThread[] players = { nought, cross };
        for(ServerThread player : players) {
            if(player != leaver) {
                player.sendMessage(
                        this,
                        "This game has terminated early because:\n" + reason,
                        "Game Terminated",
                        JOptionPane.ERROR_MESSAGE
                        );
                player.sendGameUpdate(
                        this,
                        false,
                        Game.GAME_DRAW
                        );
                server.removeGame(this);
            }
        }
    }

    /**
     * Make a move on the game state on behalf of the given player's client.
     *
     * @param player The player who made the move.
     * @param x The X co-ordinate on the board (between 0 and 2).
     * @param y The Y co-ordinate on the board (between 0 and 2).
     */
    public void makeMove(ServerThread player, int x, int y) {
        if(player != currentPlayer) {
            player.sendMessage(
                    this,
                    "You cannot make a move right now.",
                    "Game",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            if(gameBoard[x][y] == Game.TILE_SPACE) {
                int tileValue = gameBoard[x][y] = player == nought ?
                                      Game.TILE_NOUGHT :
                                      Game.TILE_CROSS;
                nought.sendGameMove(this, x, y, tileValue);
                cross.sendGameMove(this, x, y, tileValue);

                if(isGameWon()) {
                    // If the game has won, terminate the game, remove it from
                    // the server's memory, and inform the clients.
                    cross.sendGameUpdate(
                            this,
                            false,
                            currentPlayer == cross ? Game.GAME_WON : Game.GAME_LOST
                            );
                    nought.sendGameUpdate(
                            this,
                            false,
                            currentPlayer == nought ? Game.GAME_WON : Game.GAME_LOST
                            );
                    currentPlayer.setScore(currentPlayer.getScore() + 1);
                    currentPlayer = null;
                    server.removeGame(this);
                } else if(isBoardFull()) {
                    // If no-one has won yet, but the board is full, then the game
                    // is a draw.
                    cross.sendGameUpdate(
                            this,
                            false,
                            Game.GAME_DRAW
                            );
                    nought.sendGameUpdate(
                            this,
                            false,
                            Game.GAME_DRAW
                            );
                    currentPlayer = null;
                    server.removeGame(this);
                } else {
                    // Otherwise, switch the player and update the clients.
                    switchCurrentPlayer();
                    sendGameUpdate();
                }
            } else {
                player.sendMessage(
                        this,
                        "You cannot make a move at this location.",
                        "Game",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
