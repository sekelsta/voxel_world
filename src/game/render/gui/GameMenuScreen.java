package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.GuiElement;
import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.gui.TextElement;
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
        addSelectableItem(new TextButton(font, "Resume", () -> game.escape()));
        if (game.isNetworked()) {
            addItem(new TextElement(font, "Host LAN game", GuiElement.GRAY));
        }
        else {
            addSelectableItem(new TextButton(font, "Host LAN game",
                        () -> overlay.pushScreen(new HostScreen(overlay))));
        }
        addSelectableItem(new TextButton(font, "Options", () -> overlay.pushScreen(new OptionsScreen(overlay))));
        addSelectableItem(new TextButton(font, "Quit", () -> game.exitWorld())); 
    }

    @Override
    public boolean pausesGame() {
        return true;
    }
}
