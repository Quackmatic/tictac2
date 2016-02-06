import java.awt.event.WindowEvent;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A panel showing the state of a game.
 *
 * @author Tom Galvin
 */
public class GamePanel extends JPanel implements GameObserver {
    private Game game;
    private JButtonGrid buttons;
    private JLabel opponentNameLabel, gameStateLabel;
    private JFrame parentFrame;

    /**
     * Opens a {@link GamePanel} for the given game.
     *
     * @param game The game for which to open a new GamePanel.
     */
    public static void openGame(Game game) {
        JFrame frame = new JFrame(
                String.format(
                    "Game with %s",
                    game.getRemotePlayerNickname()
                    )
                );
        frame.setVisible(true);
        frame.add(new GamePanel(game, frame));
        frame.setSize(300, 340);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(game.getGameStatus() == Game.GAME_IN_PROGRESS) {
                    game.forfeit();
                }
            }
        });
    }

    /**
     * Create a new GamePanel for the given game, and inside the given parent
     * JFrame.
     *
     * @param game The game which this GamePanel will display.
     * @param parentFrame The frame in which this GamePanel will appear.
     */
    public GamePanel(Game game, JFrame parentFrame) {
        super(new BorderLayout());
        this.game = game;
        this.parentFrame = parentFrame;

        this.game.addGameObserver(this);

        add(opponentNameLabel = new JLabel(
                    String.format(
                        "You are playing against %s.",
                        game.getRemotePlayerNickname()
                        )
                    ), BorderLayout.NORTH);
        add(gameStateLabel = new JLabel(
                    "Waiting for game to begin..."
                    ), BorderLayout.SOUTH);
        add(buttons = new JButtonGrid(3, 3), BorderLayout.CENTER);
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                final int x = i, y = j;
                JButton button = buttons.getButton(x, y);
                button.setFont(button.getFont().deriveFont(24f));
                button.addActionListener(e -> {
                    attemptToMakeMove(x, y);
                });
            }
        }
    }

    /**
     * Attempt to make a move at the given co-ordinate.
     *
     * @param x The X co-ordinate (between 0 and 2).
     * @param y The Y co-ordinate (between 0 and 2).
     */
    public void attemptToMakeMove(int x, int y) {
        JButton button = buttons.getButton(x, y);
        int tileValue = game.getTileValue(x, y);
        if(tileValue == Game.TILE_SPACE) {
            button.setEnabled(false);
            game.makeMove(x, y);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "You cannot place a tile here.",
                    "Game",
                    JOptionPane.ERROR_MESSAGE
                    );
        }
    }

    @Override
    public void gameMessageReceived(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                messageType
                );
    }

    @Override
    public void gameTileChanged(int x, int y, int value) {
        String text = Game.getTileString(value);
        buttons.getButton(x, y).setText(text);
    }

    @Override
    public void gameStateChanged(int state, boolean canMove) {
        buttons.setEnabled(canMove);
        switch(state) {
            case Game.GAME_IN_PROGRESS:
                if(canMove) {
                    gameStateLabel.setText(
                            String.format(
                                "It is your turn, playing as %s.",
                                Game.getTileString(game.getLocalPlayer())
                                ));
                } else {
                    gameStateLabel.setText(
                            String.format(
                                "It is %s's turn, playing as %s.",
                                game.getRemotePlayerNickname(),
                                Game.getTileString(game.getRemotePlayer())
                                ));
                }
                break;
            case Game.GAME_DRAW:
                gameStateLabel.setText("You have tied.");
                JOptionPane.showMessageDialog(
                        this,
                        String.format(
                            "You have tied against %s.",
                            game.getRemotePlayerNickname()
                            ),
                        "Game Finished",
                        JOptionPane.INFORMATION_MESSAGE
                        );

                game.remove();
                tryToCloseWindow(parentFrame);
                break;
            case Game.GAME_LOST:
                gameStateLabel.setText("You have lost!");
                JOptionPane.showMessageDialog(
                        this,
                        String.format(
                            "You have lost against %s.",
                            game.getRemotePlayerNickname()
                            ),
                        "Game Finished",
                        JOptionPane.INFORMATION_MESSAGE
                        );

                game.remove();
                tryToCloseWindow(parentFrame);
                break;
            case Game.GAME_WON:
                gameStateLabel.setText("You have won!");
                JOptionPane.showMessageDialog(
                        this,
                        String.format(
                            "You have won against %s, well done!",
                            game.getRemotePlayerNickname()
                            ),
                        "Game Finished",
                        JOptionPane.INFORMATION_MESSAGE
                        );


                game.remove();
                tryToCloseWindow(parentFrame);
                break;
        }
    }

    /**
     * Try to close the window associated with this GamePanel.
     *
     * @param frame The frame associated with this GamePanel.
     */
    private void tryToCloseWindow(JFrame frame) {
        if(frame != null) {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
    }
}
