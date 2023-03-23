package sekelsta.game.network;

import java.nio.ByteBuffer;

import sekelsta.engine.Log;
import sekelsta.engine.network.ByteVector;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkDirection;
import sekelsta.game.Game;
import sekelsta.game.RemotePlayer;
import sekelsta.game.World;
import sekelsta.game.entity.Pawn;

public class ClientJoinGame extends Message {
    private int skin;

    public ClientJoinGame() {}

    public ClientJoinGame(int skin) {
        this.skin = skin;
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.CLIENT_TO_SERVER;
    }

    @Override
    public void encode(ByteVector buffer) {
        buffer.putInt(skin);
    }

    @Override
    public void decode(ByteBuffer buffer) {
        skin = buffer.getInt();
    }

    @Override
    public void handle(INetworked game) {
        World world = ((Game)game).getWorld();

        // Spawn new player
        // TODO: Double check that moving the entity right away won't cause bugs
        Pawn newPlayer = new Pawn();
        RemotePlayer controller = new RemotePlayer(newPlayer, sender.getID());
        newPlayer.setController(controller);
        newPlayer.skin = skin;
        world.moveToSpawnPoint(newPlayer);
        world.spawn(newPlayer);
        ServerGivePawn message = new ServerGivePawn(newPlayer.getID());
        game.getNetworkManager().queueMessage(sender, message);
    }

}
