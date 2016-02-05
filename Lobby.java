import java.util.ArrayList;
import java.util.HashMap;

public class Lobby {
    private HashMap<String, Integer> players;
    private LobbyProvider provider;
    private ArrayList<LobbyObserver> observers;
    private int lobbyID;

    public Lobby(int lobbyID, LobbyProvider provider) {
        this.provider = provider;
        this.lobbyID = lobbyID;

        players = new HashMap<String, Integer>();
        observers = new ArrayList<LobbyObserver>();

        this.provider.getInitialPlayers(this);
    }

    protected int getLobbyID() {
        return lobbyID;
    }

    public void addObserver(LobbyObserver observer) {
        observers.add(observer);
    }

    public String[] getPlayers() {
        return players.keySet().toArray(new String[0]);
    }

    public void messageReceived(String message, String title, int messageType) {
        for(LobbyObserver observer : observers) {
            observer.messageReceived(message, title, messageType);
        }
    }

    public void addPlayer(String nickname, int score) {
        players.put(nickname, score);
        for(LobbyObserver observer : observers) {
            observer.playerEnter(nickname, score);
        }
    }

    public void removePlayer(String nickname) {
        players.remove(nickname);
        for(LobbyObserver observer : observers) {
            observer.playerLeave(nickname);
        }
    }

    public int getPlayerScore(String nickname) {
        return players.get(nickname);
    }

    public void sendGameRequest(String recipient) {
        provider.sendGameRequest(this, recipient);
    }

    public void gameRequestSent(String recipient, int gameID) {
        for(LobbyObserver observer : observers) {
            observer.gameRequestSent(gameID, recipient);
        }
    }
    
    public void acceptGameRequest(int gameID) {
        provider.acceptGameRequest(this, gameID);
    }

    public void gameRequestReceived(String sender, int gameID) {
        for(LobbyObserver observer : observers) {
            observer.gameRequestReceived(gameID, sender);
        }
    }
}
