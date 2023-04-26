package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.game.Game;

public class DeathScreen extends Screen {   
    private TextElement title;

    public DeathScreen(Game game, Overlay overlay) {
        title = new TextElement(Fonts.getTitleFont(), "You died");
        addItem(title);
        addSelectableItem(new TextButton(Fonts.getButtonFont(), "Respawn", () -> respawn(game, overlay)));
    }

    private void respawn(Game game, Overlay overlay) {
        game.respawn();
        overlay.popScreenIfEquals(this);
    }
}
