import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import javax.swing.JTable;

/**
 * The model class required by a {@code JTable} to represent the
 * data and notify the JTable of changes to the data. In this case
 * the data is the people in a lobby.
 */
public class LobbyModel extends AbstractTableModel implements LobbyObserver {
    private Lobby lobby;
    private ArrayList<String> nicknames;
    private JTable table;

    /**
     * Create a new LobbyModel associated with the given table and 
     * representing the given lobby.
     *
     * @param table The table which this model is associated to.
     * @param lobby The lobby which this model represents.
     */
    public LobbyModel(JTable table, Lobby lobby) {
        this.table = table;
        this.lobby = lobby;
        this.nicknames = new ArrayList<String>();

        lobby.addObserver(this);
        String[] nicknamesArray = lobby.getPlayers();
        for(int i = 0; i < nicknamesArray.length; i++) {
            nicknames.add(nicknamesArray[i]);
        }
    }

    @Override
    public int getRowCount() {
        return nicknames.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int row, int column) {
        String name = nicknames.get(row);
        if(name == null) {
            return "?";
        }
        if(column == 0) {
            return name;
        } else if(column == 1) {
            return Integer.toString(lobby.getPlayerScore(name));
        } else {
            throw new IllegalArgumentException(String.format(
                        "Invalid column number %d.",
                        column));
        }
    }

    @Override
    public void messageReceived(String message, String title, int messageType) {
        // nothing
    }

    @Override
    public void playerEnter(String nickname, int score) {
        if(nicknames.contains(nickname)) {
            int row = nicknames.indexOf(nickname);
            fireTableCellUpdated(row, 1);
        } else {
            fireTableRowsInserted(nicknames.size(), nicknames.size());
            nicknames.add(nickname);
        }

        fireTableDataChanged();
    }

    @Override
    public void playerLeave(String nickname) {
        int row = nicknames.indexOf(nickname);
        fireTableRowsDeleted(row, row);
        nicknames.remove(row);
    }
    
    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0: return "Nickname";
            case 1: return "Score";
            default:
                throw new IllegalArgumentException(
                    String.format(
                        "Only columns 0 and 1 are used in a lobby display, so column %d doesn't exist.",
                        column
                    )
                );
        }
    }

    @Override
    public void gameRequestReceived(int gameID, String sender) {
        // nothing
    }

    @Override
    public void gameRequestSent(int gameID, String receiver) {
        // nothing
    }
     
    @Override
    public void gameStarted(Game game) {
        // nothing
    }
}
