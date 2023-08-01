package shadowfox.entity;

import java.util.Random;

import shadowfox.Particle;

public interface IEntitySpace {
    <T extends Entity> T spawn(T entity);
    Entity remove(Entity mob);
    Random getRandom();
    Particle addParticle(Particle particle);
    long getCurrentTick();
}
