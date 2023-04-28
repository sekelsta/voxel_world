package sekelsta.game.render.gui;

import java.awt.Color;

import sekelsta.engine.render.gui.*;
import sekelsta.game.Game;
import sekelsta.game.SaveGame;

public class DeleteGameScreen extends Screen {
    public DeleteGameScreen(Overlay overlay, SaveGame[] saves) {
        Game game = overlay.getGame();
        addItem(new TextElement(Fonts.getTitleFont(), "Delete Game"));

        for (int i = 0; i < LoadGameScreen.MAX_SAVES && i < saves.length; ++i) {
            SaveGame save = saves[i];
            addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), save.getName()),
                () -> overlay.pushScreen(new ConfirmDeleteScreen(overlay, save))), 1);
            addItem(new TextElement(Fonts.getTextFont(), save.getFileName(), Color.LIGHT_GRAY));
        }

        addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), "Cancel"), () -> game.escape()));
    }
}
