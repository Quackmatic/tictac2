import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A functional interface of the lambda-expressions being queued to call on the
 * data output stream. This must be used rather than a standard {@link java.util.function.Consumer}
 * as writing packets to a {@link java.io.DataOutputStream} has the potential to
 * throw a {@link java.io.IOException}.
 *
 * @author Tom Galvin
 */
public interface PacketWriter {
    /**
     * Write a packet to the given output stream.
     *
     * @param dataOutputStream The output stream to which to write the packet.
     */
    public void writePacket(DataOutputStream dataOutputStream) throws IOException;
}
