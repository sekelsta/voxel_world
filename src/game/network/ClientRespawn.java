package sekelsta.game.network;

import java.nio.ByteBuffer;
import shadowfox.network.*;
import sekelsta.game.Game;
import sekelsta.game.World;
import sekelsta.game.entity.Pawn;

public class ClientRespawn extends Message {
    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.CLIENT_TO_SERVER;
    }

    @Override
    public void encode(ByteVector buffer) {
        // Nothing to do
    }

    @Override
    public void decode(ByteBuffer buffer) {
        // Nothing to do
    }

    @Override
    public void handle(INetworked game) {
        World world = ((Game)game).getWorld();
        Pawn pawn = world.respawn(sender.getID());
        ServerGivePawn message = new ServerGivePawn(pawn.getID());
        game.getNetworkManager().queueMessage(sender, message);
    }
}
