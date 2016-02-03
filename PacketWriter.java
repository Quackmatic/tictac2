import java.io.DataOutputStream;
import java.io.IOException;

public interface PacketWriter {
    public void writePacket(DataOutputStream dataOutputStream) throws IOException;
}
