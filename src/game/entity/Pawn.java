package sekelsta.game.entity;

import java.nio.ByteBuffer;
import java.util.Random;
import sekelsta.engine.Particle;
import sekelsta.engine.entity.*;
import sekelsta.engine.network.ByteVector;
import sekelsta.game.Input;
import sekelsta.game.RemotePlayer;
import sekelsta.game.World;
import shadowfox.math.Vector3f;

public class Pawn extends Entity {
    public static final int NUM_SKINS = 3;

    public int skin;

    public Pawn() {
        this(0, 0, 0, null);
    }

    public Pawn(int x, int y, int z) {
        this(x, y, z, null);
    }

    public Pawn(IController controller) {
        this(0, 0, 0, controller);
    }


    public Pawn(int x, int y, int z, IController controller) {
        super(x, y, z);
        this.controller = controller;
        drag = 0.8f;
        angularDrag = 0.7f;
    }

    public Pawn(ByteBuffer buffer) {
        super(buffer);
        skin = buffer.getInt();
    }

    public boolean isLocalPlayer() {
        return controller instanceof Input;
    }

    public boolean isControlledBy(Long connectionID) {
        if (connectionID == null) {
            return controller instanceof Input;
        }

        if (controller instanceof RemotePlayer) {
            return ((RemotePlayer)controller).connectionID == connectionID;
        }

        return false;
    }

    @Override
    public boolean mayDespawn() {
        return false;
    }

    @Override
    public void encode(ByteVector buffer) {
        super.encode(buffer);
        buffer.putInt(skin);
    }

    @Override
    public EntityType getType() {
        return Entities.PAWN;
    }
}
