package messages;

import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PlayerRegisteredMessageTest {

    @Test
    public void encodesIntoExpected() throws IOException {
        Message.MessageType expectedMessageType = Message.MessageType.PLAYER_REGISTERED;
        UUID expectedUUID = UUID.randomUUID();
        short expectedPlayerId = 100;
        int expectedInitialCash = 1000;
        byte[] expectedMessageBytes = new Message.Encoder()
                .encodeMessageType(expectedMessageType)
                .encodeUUID(expectedUUID)
                .encodeShort(expectedPlayerId)
                .encodeInt(expectedInitialCash)
                .toByteArray();

        byte[] actualMessageBytes = new PlayerRegisteredMessage(expectedUUID, expectedPlayerId, expectedInitialCash).encode();

        assertArrayEquals(expectedMessageBytes, actualMessageBytes);
    }

    @Test
    public void decodesIntoExpected() throws IOException {
        Message.MessageType expectedMessageType = Message.MessageType.PLAYER_REGISTERED;
        UUID expectedUUID = UUID.randomUUID();
        short expectedPlayerId = 100;
        int expectedInitialCash = 1000;
        byte[] messageBytes = new Message.Encoder()
                .encodeMessageType(expectedMessageType)
                .encodeUUID(expectedUUID)
                .encodeShort(expectedPlayerId)
                .encodeInt(expectedInitialCash)
                .toByteArray();

        PlayerRegisteredMessage victim = PlayerRegisteredMessage.decode(messageBytes);

        assertEquals(expectedMessageType, victim.getMessageType());
        assertEquals(expectedPlayerId, victim.getPlayerId());
        assertEquals(expectedInitialCash, victim.getInitialCash());
    }
}