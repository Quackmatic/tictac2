import java.io.*;
import java.net.*;

public class ServerClient implements Runnable {
    private Socket client;

    public ServerClient(Socket client) {
        this.client = client;
    }
    
    @Override
    public void run() {

    }
}
