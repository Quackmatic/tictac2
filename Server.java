import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Server implements Runnable {
    private int port;
    private ServerSocket server;
    private boolean running;
    private HashMap<String, ServerClient> clients = new HashMap<String, ServerClient>();

    public Server(int port) {
        this.port = port;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(3000);

            while(running) {
                try {
                    Socket clientSocket = server.accept();
                    ServerClient client = new ServerClient(clientSocket);

                    try(DataInputStream inputStream = new DataInputStream(client.getInputStream());
                        DataOutputStream outputStream = new DataOutputStream(client.getOutputStream())) {
                        int connectPacketID = inputStream.readInt();
                        if(connectPacketID != Packet.CLIENT_CONNECT) {
                            System.out.println("Client did not send CLIENT_CONNECT packet, terminating connection.");
                            client.close();
                        } else {
                            int clientProtocolVersion = inputStream.readInt();
                            if(clientProtocolVersion != Packet.PROTOCOL_VERSION) {
                                outputStream.writeInt(Packet.)
                            }
                        }
                    } catch(Exception e) {
                        System.out.println("Error during connection initialization.");
                        e.printStackTrace();
                    } finally {
                        running = false;
                    }
                } catch(SocketTimeoutException e) {
                    // connection timed out, this allows
                    // the server thread to be stopped
                }
            }
        } catch(IOException e) {
            System.out.println("IO Exception:");
            e.printStackTrace();
        }
    }
    
    public static void main(String... args) {
        if(args.length != 1) {
            System.out.println("Usage:");
            System.out.println();
            System.out.println("java Server <port>");
            System.out.println("port: The port to listen on for clients.");
        } else {
            new Server(Integer.valueOf(args[0])).run();
        }
    }
}
