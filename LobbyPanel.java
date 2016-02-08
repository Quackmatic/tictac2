import javax.swing.ListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JFrame;
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
    private String localNickname;

    /**
     * Open a LobbyPanel representing the given lobby.
     *
     * @param nickname The nickname to use in the window title.
     * @param lobby The lobby to be displayed in the new LobbyPanel.
     */
    public static void openLobby(String nickname, Lobby lobby) {
        JFrame frame = new JFrame(nickname + " - tictac2");
        frame.add(new LobbyPanel(lobby, nickname));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 340);
        frame.setVisible(true);
    }

    /**
     * Create a new LobbyPanel displaying the given lobby.
     *
     * @param lobby The lobby to be displayed in this LobbyPanel.
     * @param localNickname The nickname of the local player.
     */
    public LobbyPanel(Lobby lobby, String localNickname) {
        super(new BorderLayout());
        this.lobby = lobby;
        this.localNickname = localNickname;

        this.playerTable = new JTable(new LobbyModel(this.playerTable, lobby));
        add(new JScrollPane(this.playerTable));
        this.playerTable.setFillsViewportHeight(true);
        this.playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.playerTable.getColumnModel().getColumn(1).setWidth(48);
        this.lobby.addObserver(this);

        add(statusLabel = new JLabel("Welcome to TicTac2!"),
                BorderLayout.NORTH);

        JButton challengeButton = new JButton("Send Challenge");
        challengeButton.addActionListener(e -> sendChallenge());
        add(challengeButton, BorderLayout.SOUTH);
    }

    /**
     * Send a game challenge to the currently-selected user.
     */
    private void sendChallenge() {
        int selectedRow = playerTable.getSelectedRow();
        if(selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a player before attempting to send a challenge.",
                    "Challenge",
                    JOptionPane.ERROR_MESSAGE
                    );
        } else {
            String recipientNickname = playerTable
                .getValueAt(selectedRow, 0)
                .toString();
            lobby.sendGameRequest(recipientNickname);
            setStatus(String.format(
                        "Game request sent to %s...",
                        recipientNickname
                        ));
        }
    }

    /**
     * Set the status label text.
     *
     * @param status The text to set the label text to.
     */
    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    @Override
    public void messageReceived(String message, String title, int messageType) {
        if(messageType == -1) {
            setStatus(message);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    title,
                    messageType
                    );
        }
    }

    @Override
    public void playerEnter(String nickname, int score) {
        setStatus(String.format(
                    "%s has joined the lobby.",
                    nickname
                    ));
    }

    @Override
    public void playerLeave(String nickname) {
        setStatus(String.format(
                    "%s has left the lobby.",
                    nickname
                    ));
    }

    @Override
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
        lobby.respondToGameRequest(gameID, result == JOptionPane.YES_OPTION);
    }

    @Override
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

    @Override
    public void gameStarted(Game game) {
        GamePanel.openGame(game);
    }
}
