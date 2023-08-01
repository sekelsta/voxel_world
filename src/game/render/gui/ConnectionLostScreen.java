package sekelsta.game.render.gui;

import shadowfox.render.gui.*;
import shadowfox.render.text.BitmapFont;
import sekelsta.game.Game;

public class ConnectionLostScreen extends Screen {
    public ConnectionLostScreen(Game game) {        
        addItem(new TextElement(Fonts.getTitleFont(), "Connection lost"));
        addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), "Okay"), () -> game.escape()));
    }
}
