package sekelsta.game.render.entity;

import java.util.Scanner;

import sekelsta.engine.render.*;
import sekelsta.engine.render.entity.EntityRenderer;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.entity.Pawn;

import sekelsta.tools.ObjParser;

public final class PawnRenderer extends EntityRenderer<Pawn> {
    private Texture[] skins = new Texture[Pawn.NUM_SKINS];

    public PawnRenderer() {
        this.mesh = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/player.obj"))));
        skins[0] = Textures.WHITE;
        skins[1] = Textures.BLACK;
        skins[2] = Textures.TRANSPARENT;
    }

    @Override
    public void render(Pawn entity, float lerp, MatrixStack stack, MaterialShader shader) {
        this.texture = skins[entity.skin];
        super.render(entity, lerp, stack, shader);
    }
}
