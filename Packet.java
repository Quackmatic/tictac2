public abstract class Packet {
    public static final int
        CLIENT_CONNECT = 0,
        CLIENT_REQUEST_SEND = 100,
        CLIENT_REQUEST_ACCEPT = 101,
        CLIENT_PLAYER_GET_LIST = 200,
        CLIENT_GAME_MOVE = 300;

    public static final int
        SERVER_WELCOME = 0,
        SERVER_MESSAGE = 1,
        SERVER_REQUEST_SENT = 100,
        SERVER_REQUEST_RECEIVED = 101,
        SERVER_PLAYER_UPDATE = 200,
        SERVER_PLAYER_LEAVE = 201,
        SERVER_GAME_BEGIN = 300;
}
