package sekelsta.game;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import sekelsta.engine.Gameloop;
import sekelsta.engine.Particle;
import sekelsta.engine.entity.*;
import sekelsta.game.entity.*;
import sekelsta.game.network.*;
import sekelsta.game.terrain.*;
import shadowfox.math.Matrix3f;
import shadowfox.math.Vector3f;

public class World implements IEntitySpace {
    private final int TICKS_PER_DAY = 36 * 60 * Gameloop.TICKS_PER_SECOND;
    private final int TICKS_PER_LUNAR_MONTH = 13 * TICKS_PER_DAY;
    private final int TICKS_PER_YEAR = 28 * TICKS_PER_DAY;

    public final boolean authoritative;
    private long tick;
    private boolean paused;

    private final Random random;
    private final Terrain terrain;
    private final Set<Pawn> players = new HashSet<>();
    private final List<Entity> mobs = new ArrayList<>();

    // Mobs to add/remove, to avoid concurrent modififation while updating
    private final List<Entity> killed = new ArrayList<>();
    private final List<Entity> spawned = new ArrayList<>();

    private final List<Particle> particles = new ArrayList<>();

    // Players that died and haven't respawned yet
    private final List<Pawn> limbo = new ArrayList<>();

    public Pawn localPlayer;

    private int nextID = 0;

    private Game game;

    private Map<Integer, List<Consumer<Entity>>> onSpawnFunctions = new HashMap<>();

    private List<List<Runnable>> delayedActions = new ArrayList<>();

    public World(Game game, SaveGame saveGame) {
        saveGame.writeIfNew();
        this.game = game;
        this.authoritative = saveGame != null;
        this.random = new Random();
        this.terrain = new Terrain(game);
        this.tick = saveGame.getTick();
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void moveToSpawnPoint(Entity entity) {
        entity.setVelocity(0, 0, 0);
        entity.scaleAngularVelocity(0);
        float angle = random.nextFloat() * 2 * (float)Math.PI;
        float dist = 0;
        entity.teleport(dist * (float)Math.cos(angle), dist * (float)Math.sin(angle), 0);
        float yaw = random.nextFloat() * Entity.TAU;
        entity.snapToAngle(yaw, 0, 0);
    }

    public void spawnLocalPlayer(IController playerController) {
        this.localPlayer = new Pawn(playerController);
        moveToSpawnPoint(localPlayer);
        localPlayer.skin = random.nextInt(Pawn.NUM_SKINS);
        this.spawn(this.localPlayer);
    }

    public Pawn respawn(Long connectionID) {
        assert(authoritative);
        Pawn pawn = null;
        for (Pawn deadPawn : limbo) {
            if (deadPawn.isControlledBy(connectionID)) {
                pawn = deadPawn;
                break;
            }
        }
        moveToSpawnPoint(pawn);
        if (connectionID != null) {
            pawn.setController(new RemotePlayer(pawn, connectionID));
        }
        limbo.remove(pawn);
        spawn(pawn);
        return pawn;
    }

    public void togglePaused() {
        paused = !paused;
        if (game.getNetworkManager() != null) {
            paused = false;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public long getSeed() {
        return 0;
    }

    public void runDelayed(Runnable runnable, int delayTicks) {
        while (delayTicks >= delayedActions.size()) {
            delayedActions.add(new ArrayList<Runnable>());
        }
        delayedActions.get(delayTicks).add(runnable);
    }

    public void update() {
        if (paused) {
            return;
        }

        if (delayedActions.size() > 0) {
            for (Runnable action : delayedActions.get(0)) {
                action.run();
            }
            delayedActions.remove(0);
        }

        // Update particles
        ArrayList<Particle> removedParticles = new ArrayList<>();
        for (Particle particle : particles) {
            particle.update();
            if (particle.isDead()) {
                removedParticles.add(particle);
            }
        }
        particles.removeAll(removedParticles);
        removedParticles.clear();

        // Spawn
        mobs.addAll(spawned);
        if (isNetworkServer()) {
            for (Entity mob : spawned) {
                ServerSpawnEntity spawnMessage = new ServerSpawnEntity(mob);
                game.getNetworkManager().queueBroadcast(spawnMessage);
            }
        }
        spawned.clear();

        // TOOD: Spawn mobs

        // Update
        for (Entity mob : mobs) {
            mob.update();
            if (isNetworkServer()) {
                EntityUpdate message = new EntityUpdate(mob);
                game.getNetworkManager().queueBroadcast(message);
            }
        }

        if (authoritative) {
            // TODO: Collide

            // TODO: Despawn
        }

        mobs.removeAll(killed);
        if (isNetworkServer()) {
            for (Entity mob : killed) {
                ServerRemoveEntity message = new ServerRemoveEntity(mob.getID());
                game.getNetworkManager().queueBroadcast(message);
                mob.enterWorld(null);
            }
        }
        killed.clear();

        // TODO: Unload extra terrain
        for (Pawn player : players) {
            int blockX = (int)(player.getX() * terrain.blockSize);
            int blockY = (int)(player.getY() * terrain.blockSize);
            int blockZ = (int)(player.getZ() * terrain.blockSize);
            terrain.loadNear(blockX, blockY, blockZ, player.getChunkLoadRadius());
        }

        tick += 1;
    }

    public List<Entity> getMobs() {
        return mobs;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public Pawn getLocalPlayer() {
        return localPlayer;
    }

    public <T extends Entity> T spawn(T entity) {
        if (entity instanceof Pawn) {
            players.add((Pawn)entity);
        }

        this.spawned.add(entity);
        entity.enterWorld(this);
        if (authoritative) {
            entity.setID(nextID);
            nextID += 1;
        }

        if (onSpawnFunctions.containsKey(entity.getID())) {
            for (Consumer<Entity> function : onSpawnFunctions.get(entity.getID())) {
                function.accept(entity);
            }
            onSpawnFunctions.remove(entity.getID());
        }

        return entity;
    }

    public void disconnectPlayer(Pawn pawn) {
        players.remove(pawn);
        this.killed.add(pawn);
    }

    public Entity remove(Entity entity) {
        if (entity instanceof Pawn && ((Pawn)entity).isLocalPlayer()) {
            game.onLocalPawnKilled();
        }

        this.killed.add(entity);
        return entity;
    }

    public Particle addParticle(Particle particle) {
        particles.add(particle);
        return particle;
    }

    public void runWhenEntitySpawns(Consumer<Entity> function, int id) {
        for (Entity mob : mobs) {
            if (mob.getID() == id) {
                function.accept(mob);
                return;
            }
        }
        for (Entity mob : spawned) {
            if (mob.getID() == id) {
                function.accept(mob);
                return;
            }
        }

        if (onSpawnFunctions.containsKey(id)) {
            onSpawnFunctions.get(id).add(function);
        }
        else {
            List<Consumer<Entity>> f = new ArrayList<>();
            f.add(function);
            onSpawnFunctions.put(id, f);
        }
    }

    public Entity getEntityByID(int id) {
        for (Entity mob : mobs) {
            if (mob.getID() == id) {
                return mob;
            }
        }
        return null;
    }

    public Random getRandom() {
        return random;
    }

    @Override
    public long getCurrentTick() {
        return tick;
    }

    public void setTickIfClient(long tick) {
        if (authoritative) {
            return;
        }
        if (game.getNetworkManager() == null) {
            return;
        }
        this.tick = tick;
    }

    public void killPawn(Pawn pawn) {
        assert(authoritative);

        remove(pawn);
        limbo.add(pawn);
    }

    public boolean isDead(Entity entity) {
        if (killed.contains(entity)) {
            return true;
        }
        if (spawned.contains(entity)) {
            return false;
        }
        if (mobs.contains(entity)) {
            return false;
        }
        return true;
    }

    public void addBlock(Ray ray) {
        RaycastResult r = terrain.findHit(ray);
        if (r != null) {
            terrain.setBlock(r.x() + r.direction().x, r.y() + r.direction().y, r.z() + r.direction().z, Block.GRASS);
        }
    }

    public void removeBlock(Ray ray) {
        RaycastResult r = terrain.findHit(ray);
        if (r != null) {
            terrain.setBlock(r.x(), r.y(), r.z(), Block.EMPTY);
        }
    }

    // 0 is noon durning the northern hemisphere vernal equinox, or midnight during the N. fall equinox
    public float getPlanetaryRotation(float lerp) {
        return (tick % TICKS_PER_DAY + lerp) / (float)TICKS_PER_DAY;
    }

    // 0 is a full moon during the N vernal equinox, or a new moon during the fall
    private float getAbsoluteMoonCycle(float lerp) {
        return (tick % TICKS_PER_LUNAR_MONTH + lerp) / (float)TICKS_PER_LUNAR_MONTH;
    }

    // 0 is the vernal equinox in the northern hemisphere
    private float getSeason(float lerp) {
        return (tick % TICKS_PER_YEAR + lerp) / (float)TICKS_PER_YEAR;
    }

    public Matrix3f getMoonRotation(float lerp) {
        float lunarOrbitAngle = 18 * (float)Math.PI / 180;
        Vector3f axis = new Vector3f(0, -1, 0).rotate(lunarOrbitAngle, 0, 0, 1);
        axis.rotate((float)Math.PI * 2 * getPlanetaryRotation(lerp), 0, -1, 0);

        Matrix3f result = new Matrix3f();
        result.rotate(-1 * (float)Math.PI * 2 * getAbsoluteMoonCycle(lerp), axis.x, axis.y, axis.z);
        result.rotate((float)Math.PI * 2 * getPlanetaryRotation(lerp), 0, -1, 0);
        return result;
    }

    public Vector3f getSunPosition(float lerp) {
        // Roughly 1% of the real-life distance, 150 million km
        float sunDistance = 1500000000;
        float axialTilt = 23 * (float)Math.PI / 180;
        Vector3f axis = new Vector3f(0, -1, 0).rotate(axialTilt, 0, 0, 1);
        axis.rotate((float)Math.PI * 2 * getPlanetaryRotation(lerp), 0, -1, 0);

        Vector3f pos = new Vector3f(0, 0, sunDistance);
        pos.rotate((float)Math.PI * 2 * getSeason(lerp), axis.x, axis.y, axis.z);
        pos.rotate((float)Math.PI * 2 * getPlanetaryRotation(lerp), 0, -1, 0);
        return pos;
    }

    public Vector3f getMoonPosition(float lerp) {
        // Roughly 1% of the real-life distance, 385,000 km
        float moonDistance = 3850000;
        Vector3f pos = new Vector3f(0, 0, -1 * moonDistance);
        Matrix3f rotation = getMoonRotation(lerp);
        return rotation.transform(pos);
    }

    private boolean isNetworkServer() {
        return authoritative && game.getNetworkManager() != null;
    }
}
