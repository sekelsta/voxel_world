package sekelsta.test.engine;

import java.nio.ByteBuffer;

import shadowfox.network.ByteVector;
import shadowfox.network.INetworked;
import shadowfox.network.Message;
import shadowfox.network.NetworkDirection;

class ByteMessage extends Message {
    byte[] bytes;

    public ByteMessage() {}

    public ByteMessage(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.BIDIRECTIONAL;
    }

    @Override
    public void encode(ByteVector buffer) {
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        int length = buffer.getInt();
        bytes = new byte[length];
        buffer.get(bytes);
    }

    @Override
    public void handle(INetworked game) {
        if (game instanceof NetworkedGame) {
            ((NetworkedGame)game).handledTestMessages.add(this);
        }
    }
}
