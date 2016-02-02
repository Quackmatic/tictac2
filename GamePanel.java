import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.BorderLayout;

public class GamePanel extends JPanel implements GameListener {
    private Game game;
    private JButtonGrid buttons;
    private JLabel opponentNameLabel, gameStateLabel;

    public GamePanel(Game game) {
        super(new BorderLayout());
        this.game = game;

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
                // TODO Game loss message, close window
                break;
            case Game.GAME_WON:
                gameStateLabel.setText("You have won!");
                // TODO Game win message, close window
                break;
        }
    }
}
