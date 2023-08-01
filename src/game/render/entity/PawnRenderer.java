package sekelsta.game.render.entity;

import java.util.Scanner;

import shadowfox.render.*;
import shadowfox.render.entity.EntityRenderer;
import shadowfox.render.mesh.RigidMesh;
import sekelsta.game.entity.Pawn;

import shadowfox.tools.ObjParser;

public final class PawnRenderer extends EntityRenderer<Pawn> {
    private Texture[] skins = new Texture[Pawn.NUM_SKINS];

    public PawnRenderer() {
        this.mesh = new RigidMesh(ObjParser.parse(new Scanner(EntityRenderer.class.getResourceAsStream("/assets/obj/player.obj"))));
        skins[0] = Textures.WHITE;
    }

    @Override
    public void render(Pawn entity, float lerp, MatrixStack stack, MaterialShader shader) {
        this.texture = skins[entity.skin];
        super.render(entity, lerp, stack, shader);
    }
}
