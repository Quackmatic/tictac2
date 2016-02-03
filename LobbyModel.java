import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class LobbyModel extends AbstractTableModel implements LobbyObserver {
    private Lobby lobby;
    private ArrayList<String> nicknames;

    public LobbyModel(Lobby lobby) {
        this.lobby = lobby;
        this.nicknames = new ArrayList<String>();

        lobby.addObserver(this);
        String[] nicknamesArray = lobby.getPlayers();
        for(int i = 0; i < nicknamesArray.length; i++) {
            nicknames.add(nicknamesArray[i]);
        }
    }

    public int getRowCount() {
        return nicknames.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int row, int column) {
        String name = nicknames.get(row);
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

    public void playerEnter(String nickname, int score) {
        if(!nicknames.contains(nickname)) {
            int row = nicknames.indexOf(nickname);
            fireTableCellUpdated(row, 1);
        } else {
            nicknames.add(nickname);
            fireTableRowsInserted(nicknames.size(), nicknames.size());
        }
    }

    public void playerLeave(String nickname) {
        int row = nicknames.indexOf(nickname);
        fireTableRowsDeleted(row, row);
    }
    
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

    public void gameRequestReceived(int gameID, String sender) {
        // nothing
    }

    public void gameRequestSent(int gameID, String receiver) {
        // nothing
    }
     
    public void gameStarted(Game game) {
        // nothing
    }
}
