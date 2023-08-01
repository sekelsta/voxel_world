package sekelsta.game.render.gui;

import shadowfox.render.gui.*;
import shadowfox.render.text.BitmapFont;
import sekelsta.game.Game;

public class ConnectingScreen extends Screen {
    private Overlay overlay;

    public ConnectingScreen(Overlay overlay, String address) {
        this.overlay = overlay;
        Game game = overlay.getGame();
        addItem(new TextElement(Fonts.getTitleFont(), "Connecting..."));
        BitmapFont font = Fonts.getButtonFont();
        addItem(new TextElement(font, address));
        this.addSelectableItem(new Button(new TextElement(font, "Cancel"), () -> {
            game.cancelConnecting();
            overlay.popScreen();
        }));
    }

    @Override
    public void onEscape() {
        Game game = overlay.getGame();
        game.cancelConnecting();
    }
}
