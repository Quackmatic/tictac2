import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;

/**
 * A panel showing the state of the server's game lobby.
 *
 * @author Tom Galvin
 */
public class LobbyPanel extends JPanel implements LobbyObserver {
    private JTable playerTable;
    private JLabel statusLabel;
    private Lobby lobby;

    public LobbyPanel(Lobby lobby) {
        super(new BorderLayout());
        this.lobby = lobby;

        this.playerTable = new JTable(new LobbyModel(lobby));
        add(new JScrollPane(this.playerTable));
        this.playerTable.setFillsViewportHeight(true);
        this.lobby.addObserver(this);

        add(statusLabel = new JLabel("Welcome to TicTac2!"));
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void playerEnter(String nickname, int score) {
        setStatus(String.format(
                    "%s has joined the lobby.",
                    nickname
                    ));
    }

    public void playerLeave(String nickname) {
        setStatus(String.format(
                    "%s has left the lobby.",
                    nickname
                    ));
    }

    public void gameRequestReceived(int gameID, String sender) {
        int result = JOptionPane.showConfirmDialog(
                this,
                String.format(
                    "%s has just challenged you to a game. Do you accept?",
                    sender
                    ),
                "Game Request",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
                );
        if(result == JOptionPane.YES_OPTION) {
            lobby.acceptGameRequest(gameID);
        }
    }

    public void gameRequestSent(int gameID, String receiver) {
        if(gameID == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    String.format(
                        "The player %s does not exist.",
                        receiver),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                    );
        } else {
            setStatus(String.format(
                        "%s has received your request.",
                        receiver
                        ));
        }
    }

    public void gameStarted(Game game) {
        GamePanel.openGame(game);
    }
}
