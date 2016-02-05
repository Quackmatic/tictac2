import javax.swing.JOptionPane;

public class ServerGame {
    private Server server;
    private ServerThread nought;
    private ServerThread cross;
    private ServerThread currentPlayer = null;
    private int gameID;
    private int[][] gameBoard;

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

    public int getGameID() {
        return gameID;
    }

    public ServerThread getNought() {
        return nought;
    }

    public ServerThread getCross() {
        return cross;
    }

    private void switchCurrentPlayer() {
        if(currentPlayer == nought) {
            currentPlayer = cross;
        } else {
            currentPlayer = nought;
        }
    }

    public boolean isInProgress() {
        return currentPlayer != null;
    }

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

    public boolean isGameWon() {
        // check vert. rows
        for(int x = 0; x < 3; x++) {
            if(gameBoard[x][0] == gameBoard[x][1] && gameBoard[x][1] == gameBoard[x][2]) {
                return true;
            }
        }
        // check hoz. rows
        for(int y = 0; y < 3; y++) {
            if(gameBoard[0][y] == gameBoard[1][y] && gameBoard[1][y] == gameBoard[2][y]) {
                return true;
            }
        }
        // check diagonals
        if(gameBoard[0][0] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][2] ||
           gameBoard[0][2] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][0]) {
            return true;
        }
        return false;
    }

    private void sendGameUpdate() {
        cross.sendGameUpdate(this, currentPlayer == cross, Game.GAME_IN_PROGRESS);
        nought.sendGameUpdate(this, currentPlayer == nought, Game.GAME_IN_PROGRESS);
    }

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
                    currentPlayer = null;
                    server.removeGame(this);
                } else {
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
