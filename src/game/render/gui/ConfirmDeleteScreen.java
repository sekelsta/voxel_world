package sekelsta.game.render.gui;

import java.awt.Color;

import sekelsta.engine.render.gui.*;
import sekelsta.game.Game;
import sekelsta.game.SaveGame;

public class ConfirmDeleteScreen extends Screen {
    public ConfirmDeleteScreen(Overlay overlay, SaveGame saveToDelete) {
        Game game = overlay.getGame();
        addItem(new TextElement(Fonts.getTitleFont(), "Delete Game"));
        addItem(new TextElement(Fonts.getButtonFont(), "Are you sure you want to delete this save?"));


        addItem(new TextElement(Fonts.getButtonFont(), saveToDelete.getName()), 1);
        addItem(new TextElement(Fonts.getTextFont(), saveToDelete.getFileName(), Color.LIGHT_GRAY));

        addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), "Yes, delete"), () -> {
            saveToDelete.deleteSave();
            overlay.popScreenIfEquals(this);
            overlay.popScreen();
        }));
        addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), "No, cancel"), () -> game.escape()));
    }
}
