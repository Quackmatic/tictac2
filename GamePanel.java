import java.awt.event.WindowEvent;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;

/**
 * A panel showing the state of a game.
 *
 * @author Tom Galvin
 */
public class GamePanel extends JPanel implements GameListener {
    private Game game;
    private JButtonGrid buttons;
    private JLabel opponentNameLabel, gameStateLabel;
    private JFrame parentFrame;

    public static void openGame(Game game) {
        JFrame frame = new JFrame(
                String.format(
                    "Game with %s",
                    game.getRemotePlayerNickname()
                    )
                );
        frame.add(new GamePanel(game, frame));
        frame.setSize(300, 340);
        frame.setVisible(true);
    }

    public GamePanel(Game game, JFrame parentFrame) {
        super(new BorderLayout());
        this.game = game;
        this.parentFrame = parentFrame;

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
                JButton button = buttons.getButton(i, j);
                button.setFont(button.getFont().deriveFont(24f));
            }
        }
    }

    public void gameMessageReceived(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Server Message",
                JOptionPane.INFORMATION_MESSAGE
                );
    }

    public void gameTileChanged(int x, int y, int value) {
        String text = Game.getTileString(value);
        buttons.getButton(x, y).setText(text);
    }

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


                tryToCloseWindow(parentFrame);
                break;
        }
    }

    private void tryToCloseWindow(JFrame frame) {
        if(frame != null) {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
    }
}
