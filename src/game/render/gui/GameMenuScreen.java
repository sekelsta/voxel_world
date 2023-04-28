package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class GameMenuScreen extends Screen {
    private Overlay overlay;

    public GameMenuScreen(Overlay overlay) {
        this.overlay = overlay;
        setup();
    }

    @Override
    public void refresh() {
        selectable.clear();
        items.clear();
        setup();
    }

    private void setup() {
        Game game = overlay.getGame();
        BitmapFont font = Fonts.getButtonFont();
        addSelectableItem(new Button(new TextElement(font, "Resume"), () -> game.escape()));
        if (game.isNetworked()) {
            addItem(new TextElement(font, "Host LAN game", GuiElement.GRAY));
        }
        else {
            addSelectableItem(new Button(new TextElement(font, "Host LAN game"),
                        () -> overlay.pushScreen(new HostScreen(overlay, null))));
        }
        addSelectableItem(new Button(new TextElement(font, "Options"), 
            () -> overlay.pushScreen(new OptionsScreen(overlay))));
        addSelectableItem(new Button(new TextElement(font, "Quit"), () -> game.exitWorld())); 
    }

    @Override
    public boolean pausesGame() {
        return true;
    }
}
