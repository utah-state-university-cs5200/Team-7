package messages;

import java.io.IOException;
import java.util.UUID;

public class TopOfBookRequestMessage extends Message {

    public TopOfBookRequestMessage() {
        super(MessageType.TOP_OF_BOOK_REQUEST);
    }

    public TopOfBookRequestMessage(UUID uuid) {
        super(MessageType.TOP_OF_BOOK_REQUEST, uuid);
    }

    @Override
    public byte[] encode() throws IOException {
        return new Encoder()
                .encodeMessageType(this.messageType)
                .encodeUUID(conversationId)
                .toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static TopOfBookRequestMessage decode(byte[] messageBytes) {
        Decoder decoder = new Decoder(messageBytes);

        if (decoder.decodeMessageType() != MessageType.TOP_OF_BOOK_REQUEST) {
            throw new IllegalArgumentException();
        }

        return new TopOfBookRequestMessage(decoder.decodeUUID());
    }


}
