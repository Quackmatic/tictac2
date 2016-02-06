/**
 * A class to represent packet ID numbers for all
 * packets in the tictac2 protocol.
 */
public abstract class Packet {
    /**
     * The current protocol version in use by tictac2. This mainly used to
     * detect when two mismatched, incompatible versions of tictac2 attempt to
     * connect.
     */
    public static final int PROTOCOL_VERSION = 1;

    public static final int
    /**
     * A packet sent by the client upon initial connection (containing nickname
     * data, version data, etc.)
     */
        CLIENT_CONNECT = 0,
    /**
     * A packet sent by the client to send a game request.
     */
        CLIENT_REQUEST_SEND = 100,
    /**
     * A packet sent by the client to respond to a game request.
     */
        CLIENT_REQUEST_RESPOND = 101,
    /**
     * A packet sent by the client to get info on all connected players.
     */
        CLIENT_PLAYER_GET_LIST = 200,
    /**
     * A packet sent by the client to make a move at a given location in a game.
     */
        CLIENT_GAME_MOVE = 300,
    /**
     * A packet sent by the client to forfeit a game.
     */
        CLIENT_GAME_FORFEIT = 301;

    public static final int
    /**
     * A packet sent by the server to give important server and connection
     * status to the client, including whether their chosen nickname was not
     * available, whether the connection failed or not (such as when a version
     * mismatch occurs), and any additional required info.
     */
        SERVER_STATUS = 0,
    /**
     * A message sent to the client, which may optionally be tied to a specific
     * game (in which case, the message box is modal to that game window,
     * rather than the lobby window). This is delivered as a message body, a
     * title string, and the numeric value for the type of message (which will
     * alter the message icon in the {@link javax.swing.JOptionPane} used.
     */
        SERVER_MESSAGE = 1,
    /**
     * A message sent to the client, indicating that their game request was
     * either successfully delivered to the recipient, or if the request
     * failed (if the user with the given nickname does not exist).
     */
        SERVER_REQUEST_SENT = 100,
    /**
     * A message sent to the client, indicating that they have received a
     * request to play against another user.
     */
        SERVER_REQUEST_RECEIVED = 101,
    /**
     * A message sent to the client, providing information on a logged-in user,
     * including their nickname and their score.
     */
        SERVER_PLAYER_UPDATE = 200,
    /**
     * A message sent to the client, indicating that the user with the given
     * nickname logged out of the server.
     */
        SERVER_PLAYER_LEAVE = 201,
    /**
     * A message sent to the client, indicating that a game in which they are
     * a participant has started.
     */
        SERVER_GAME_BEGIN = 300,
    /**
     * A message sent to the client, indicating that their opponent has made a
     * move in a game they are participating in.
     */
        SERVER_GAME_MOVE = 301,
    /**
     * A message sent to the client, indicating that the state of a game in
     * which they are participating in has changed. This includes whether the
     * game is in progress/won/lost/tied, and (if necessary) whose turn it is
     * to move.
     */
        SERVER_GAME_UPDATE = 302;
}
