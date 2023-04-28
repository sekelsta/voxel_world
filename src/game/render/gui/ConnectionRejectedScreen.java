package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class ConnectionRejectedScreen extends Screen {
    public ConnectionRejectedScreen(Game game, String reason) {        
        addItem(new TextElement(Fonts.getTitleFont(), "Connection rejected"));
        String[] reasonLines = reason.split("\n");
        for (String line : reasonLines) {
            addItem(new TextElement(Fonts.getButtonFont(), line, Fonts.ERROR_COLOR));
        }
        addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), "Okay"), () -> game.escape()));
    }
}
