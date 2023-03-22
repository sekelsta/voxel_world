package sekelsta.game.entity;

import sekelsta.engine.entity.EntityType;
import sekelsta.game.entity.*;
import sekelsta.game.render.entity.*;

public class Entities {
    public static final EntityType PAWN = EntityType.create(Pawn::new, PawnRenderer::new);

    public static void init() {}
}
